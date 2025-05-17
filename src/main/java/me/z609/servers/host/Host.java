package me.z609.servers.host;

import me.z609.servers.CallbackRun;
import me.z609.servers.zServers;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import redis.clients.jedis.Jedis;

import java.util.*;

public class Host {

    private HostData data;
    private zServers plugin;

    public Host(zServers plugin, HostData data) {
        this.plugin = plugin;
        this.data = data;
    }

    public HostData getData() {
        return data;
    }

    public zServers getPlugin() {
        return plugin;
    }

    void sendHeartbeat(Jedis jedis){
        Map<String, String> heartbeat = new HashMap<>();
        heartbeat.put("heartbeat", String.valueOf(System.currentTimeMillis()));
        heartbeat.put("online", "true");
        heartbeat.put("players", encapsulatePlayerNames(plugin.getServer().getOnlinePlayers()));
        jedis.hmset("host:" + data.getName(), heartbeat);
    }

    public void shutdown(){
        plugin.getRedisBridge().connect(new CallbackRun<Jedis>() {
            @Override
            public void callback(Jedis jedis) {
                jedis.hset("host:" + data.getName(), "online", "false");
            }
        });
    }

    public int getMaxServers(){
        return data.getMaxServers();
    }

    public String getName(){
        return data.getName();
    }

    public static String encapsulatePlayerNames(Server server) {
        return encapsulatePlayerNames(server.getOnlinePlayers());
    }

    public static String encapsulatePlayerNames(Collection<? extends Player> playerList) {
        List<? extends Player> players = new ArrayList<>(playerList);
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < players.size(); i++){
            Player player = players.get(i);
            builder.append(player.getUniqueId() + ":" + player.getDisplayName());
            if (i != players.size() - 1) {
                builder.append(",");
            }
        }
        return builder.toString().trim();
    }

    public static Map<UUID, String> deEncapsulatePlayerNames(String playerString) {
        String[] playerArray = playerString.split(",");
        Map<UUID, String> players = new HashMap<>();
        for(String player : playerArray){
            String[] playerArgs = player.split(":");
            if(playerArgs.length > 1){
                try {
                    UUID uuid = UUID.fromString(playerArgs[0]);
                    String playerName = playerArgs[1];
                    players.put(uuid, playerName);
                } catch (IllegalArgumentException ignored){
                }
            }
        }
        return players;
    }

    public void updateData(HostData data) {
        this.data = data;
    }
}
