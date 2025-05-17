package me.z609.servers.host;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.z609.servers.Callback;
import me.z609.servers.redis.RedisSubscriber;
import me.z609.servers.server.zServerData;
import me.z609.servers.zServers;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scheduler.BukkitTask;
import redis.clients.jedis.Jedis;

import java.util.*;
import java.util.logging.Level;

public class HostManager implements PluginMessageListener {

    public static final String BUNGEECORD_MSG_CHANNEL = "BungeeCord";

    private zServers plugin;
    private long lastUpdate = 0;
    private BukkitTask updater;

    private Map<String, HostData> hosts = new HashMap<String, HostData>();
    private Host host;

    public HostManager(zServers plugin) {
        this.plugin = plugin;

        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, BUNGEECORD_MSG_CHANNEL);
        plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, BUNGEECORD_MSG_CHANNEL, this);
        plugin.getLogger().log(Level.INFO, "Registered with BungeeCord plugin messaging channel.");

        plugin.getLogger().log(Level.INFO, "Compiling host data...");
        host = new Host(plugin, HostData.fromServer(plugin.getServer()));
        plugin.getLogger().log(Level.INFO, "Recognized this host as " + host.getData().getName() + " " +
                "[" + host.getData().getIp() + ":" + host.getData().getPort() + "]");
        if(addThisHost()) {
            plugin.getLogger().log(Level.INFO, "This host has been added!");
        }

        updateHosts();
        this.updater = plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                updateHosts();
            }
        }, 2*20, 2*20);
        plugin.getRedisBridge().getSubscriberManager().subscribe(new RedisSubscriber("transferRequest:" + host.getName()) {
            @Override
            public void onMessageReceived(String[] message) {
                if(message.length > 1){
                    String playerName = message[0];
                    Player player = plugin.getServer().getPlayer(playerName);
                    if(player != null && player.isOnline()){
                        String serverName = message[1];
                        zServerData server = plugin.getServerManager().getServerByName(serverName);
                        plugin.getConnectionManager().transferServer(player, server);
                    }
                }
            }
        });
    }

    private void updateHosts() {
        lastUpdate = System.currentTimeMillis();

        hosts = plugin.getRedisBridge().connect(new Callback<Map<String, HostData>, Jedis>() {
            @Override
            public Map<String, HostData> callback(Jedis jedis) {
                Set<String> hostNames = jedis.smembers("hosts");
                Map<String, HostData> hosts = new HashMap<String, HostData>();

                for(String name : hostNames){
                    HostData host = loadHost(jedis, name);
                    if(host != null) {
                        if(host.equals(HostManager.this.host)){
                            HostManager.this.host.updateData(host);
                        }
                        hosts.put(name, host);
                    }
                }

                host.sendHeartbeat(jedis);

                return hosts;
            }
        });
    }

    private HostData loadHost(Jedis jedis, String name) {
        String key = "host:" + name;
        Map<String, String> data = jedis.hgetAll(key);
        if(data == null || data.isEmpty()) {
            return null;
        }

        String ip = data.get("ip");
        int port;
        try {
            port = Integer.parseInt(data.get("port"));
        } catch (NumberFormatException ex) {
            return null;
        }
        long heartbeat;
        try {
            heartbeat = Long.parseLong(data.get("heartbeat"));
        } catch (NumberFormatException ex) {
            return null;
        }
        int maxServers = 8;
        try {
            maxServers = Integer.parseInt(data.getOrDefault("maxServers", "8"));
        } catch (NumberFormatException ignored) {
        }
        boolean online = Boolean.parseBoolean(data.get("online"));
        String[] players = data.getOrDefault("players", "").split(",");

        HostData host = hosts.getOrDefault(name, new HostData(name, ip, port));
        host.updateAddress(ip, port);
        host.online = online;
        host.heartbeat = heartbeat;
        host.maxServers = maxServers;
        host.players = Host.deEncapsulatePlayerNames(data.getOrDefault("players", ""));
        return host;
    }

    /**
     * We will only ever be adding the host that is running this plugin - i.e. this Spigot server.
     * This means that we will never have more than one Host per Spigot Server... so we will use Host::getData() in this method.
     * @return boolean - whether the host was added - will return false if already added
     */
    private boolean addThisHost(){

        final HostData host = this.host.getData();
        final String key = "host:" + host.getName();

        final Map<String, String> fields = new HashMap<>();
        fields.put("name", host.getName());
        fields.put("ip", host.getIp());
        fields.put("port", String.valueOf(host.getPort()));
        fields.put("heartbeat", String.valueOf(System.currentTimeMillis()));
        fields.put("players", Host.encapsulatePlayerNames(plugin.getServer()));
        fields.put("online", "true");

        return plugin.getRedisBridge().connect(new Callback<Boolean, Jedis>() {
            @Override
            public Boolean callback(Jedis jedis) {
                jedis.hmset(key, fields);
                boolean added = (jedis.sadd("hosts", host.getName()) == 1);
                if(added){
                    plugin.getRedisBridge().sendMessage("hosts", "add", host.getName());
                }
                return added;
            }
        });
    }

    public void shutdown(){
        host.shutdown();

        plugin.getServer().getMessenger().unregisterOutgoingPluginChannel(plugin);
        plugin.getServer().getMessenger().unregisterIncomingPluginChannel(plugin);
    }

    public Host getHost(){
        return host;
    }

    public HostData getHost(String name){
        return hosts.get(name);
    }

    public Collection<HostData> getHosts(){
        return Collections.unmodifiableCollection(hosts.values());
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {

    }

    public void sendToHost(Player player, HostData host){
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(host.getName());
        player.sendPluginMessage(plugin, BUNGEECORD_MSG_CHANNEL, out.toByteArray());
    }

    public void sendToHost(String player, HostData host){
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("ConnectOther");
        out.writeUTF(player);
        out.writeUTF(host.getName());
        plugin.getServer().sendPluginMessage(plugin, BUNGEECORD_MSG_CHANNEL, out.toByteArray());
    }
}
