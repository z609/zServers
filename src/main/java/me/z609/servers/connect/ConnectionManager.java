package me.z609.servers.connect;

import me.z609.servers.CallbackRun;
import me.z609.servers.api.event.player.zPlayerPreTransferEvent;
import me.z609.servers.host.HostData;
import me.z609.servers.server.zServer;
import me.z609.servers.server.zServerConnectHandler;
import me.z609.servers.server.zServerData;
import me.z609.servers.zServers;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import redis.clients.jedis.Jedis;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class ConnectionManager implements Listener {

    private zServers plugin;

    // Players that are in the process of connecting, and where to send them during PlayerJoinEvent.
    private Map<UUID, InboundConnection> connecting = new ConcurrentHashMap <UUID, InboundConnection>();
    private Map<UUID, zServerData> transferring = new ConcurrentHashMap<UUID, zServerData>();

    public ConnectionManager(zServers plugin) {
        this.plugin = plugin;
    }



    @EventHandler
    public void onLogin(AsyncPlayerPreLoginEvent event){
        final UUID uuid = event.getUniqueId();
        final String player = event.getName();

        plugin.getRedisBridge().connect(new CallbackRun<Jedis>(){
            @Override
            public void callback(Jedis jedis) {
                Map<String, String> session = jedis.hgetAll("onlinePlayers:" + uuid.toString());
                String connectName = session.get("server");
                String claimedHost = session.get("host");
                String currentHost = plugin.getHost().getName();

                zServer connect = null;
                if(connectName != null){
                    // Connect name is not null! They are already on the network, and are likely transferring between two hosts, and
                    // attempting to connect to a server on another host. The sending host should already know the host the server is located
                    // on, so the likelihood of connect == null is unlikely unless the server was taken down in the past .5 seconds.
                    connect = plugin.getServerManager().getLocalServer(connectName);

                    if (connect == null) {
                        connectName = null;
                    }

                    if(connect != null && claimedHost != null && !claimedHost.equals(plugin.getHost().getName())){
                        // This is expected behavior when Timo routes to a random host that doesn't have the best fallback server.
                        //plugin.getLogger().warning("Host mismatch: " + player + " expected on " + claimedHost + ", but arrived at " + currentHost + ". Recovering session.");
                        session.put("host", currentHost);
                        jedis.hset("onlinePlayers:"+ uuid.toString(), "host", plugin.getHost().getName());
                        connecting.put(uuid, new InboundConnection(connect, session));
                        plugin.getLogger().log(Level.INFO, "Player " + event.getName() + "(" + uuid.toString() + ") is connecting to " + connect.getName() + ".");
                        return;
                    }
                }
                if(connectName == null){
                    // In this block, we must assume the player is attempting to fallback (either new session or missing server)
                    // We will try to connect them to the best hub on the network.
                    // If we cannot find a hub, we will disallow the login.
                    final zServerData fallback = getBestFallback();

                    if(fallback == null){
                        plugin.getLogger().log(Level.WARNING, "Player " + event.getName() + "(" + uuid.toString() + ") failed to login as there is no fallback server. You can ignore this if you aren't debugging.");
                        event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, ChatColor.RED + "There are no servers to connect to at this time.");
                        return;
                    }

                    final HostData fallbackHost = fallback.getHost();
                    // Since we found a suitable server, we must update the Redis cache (to signify that the player is online), even
                    // if we don't use the server name data in any meaningful way beyond this point.

                    // Set the player as online, and connecting to fallback
                    UUID sessionId = UUID.randomUUID();
                    session = new HashMap<String, String>();
                    session.put("name", player);
                    session.put("server", fallback.getName());
                    session.put("session", sessionId.toString());
                    session.put("host", plugin.getHost().getName());
                    session.put("started", String.valueOf(System.currentTimeMillis()));
                    jedis.hmset("onlinePlayers:" + uuid.toString(), session);

                    if(fallbackHost.equals(plugin.getHost().getData())){
                        // The host the fallback server is on happens to be this one... we just have to complete the event as normal

                        // Mark a "new session" (joining the network right now - not cross server transfer)
                        zServer server = ConnectionManager.this.plugin.getServerManager().getLocalServer(fallback);
                        connecting.put(uuid, new InboundConnection(server, session));
                        plugin.getLogger().log(Level.INFO, "Player " + event.getName() + "(" + uuid.toString() + ") is connecting to " + server.getName() + ".");
                        return;
                    }

                    // Beyond this point we can assume that the fallback is on a different host.
                    // The target server is already set in redis, so just send them to the other server.
                    event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, ChatColor.RED + "There are no servers to connect to at this time.");

                    /*
                    No good way to do this yet - for now Timo will just try every host until it gets it right.
                    boolean routed = plugin.getCloud().getProvider().sendToServer(event.getName(), fallbackHost.getName());
                    if(routed)
                        plugin.getLogger().info("Player " + event.getName() + " was successfully re-routed to " + fallback.getName() + " on " + fallbackHost.getName());
                    else
                        plugin.getLogger().warning("Player " + event.getName() + " could not be re-routed to " + fallback.getName() + " on " + fallbackHost.getName());
                     */
                    return;
                }

                connecting.put(uuid, new InboundConnection(connect, session));
                plugin.getLogger().log(Level.INFO, "Player " + event.getName() + "(" + uuid.toString() + ") is connecting to " + connect.getName() + ".");
            }
        });
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        InboundConnection connection = connecting.remove(uuid);
        if(connection == null){
            player.kickPlayer(ChatColor.DARK_RED + "An internal server error occurred.");
            return;
        }

        String sessionId = connection.getSessionId();
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                plugin.getRedisBridge().connect(new CallbackRun<Jedis>() {
                    @Override
                    public void callback(Jedis jedis) {
                        String redisSession = jedis.hget("onlinePlayers:" + uuid.toString(), "session");
                        plugin.getServer().getScheduler().runTask(plugin, new Runnable() {
                            @Override
                            public void run() {
                                if(!Objects.equals(sessionId, redisSession)){
                                    // This isn't a true throttle but in all honesty, this is probably why Bukkit
                                    // (by default) has a 4000ms throttle in bukkit.yml.
                                    player.kickPlayer("Connection throttled! Please wait some time and try again.");
                                    return;
                                }
                                zServer target = connection.getTarget();

                                target.join(player, true, connection.isNewSession(), null, new zServerConnectHandler() {
                                    @Override
                                    public void onFailure(Exception ex) {
                                        ex.printStackTrace();
                                        onCancelled(ex.getMessage());
                                    }

                                    @Override
                                    public void onSuccess() {
                                        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                                            @Override
                                            public void run() {
                                                plugin.getRedisBridge().connect(new CallbackRun<Jedis>() {
                                                    @Override
                                                    public void callback(Jedis jedis) {
                                                        jedis.hset("onlinePlayers:" + uuid.toString(), "online", "true");
                                                    }
                                                });
                                            }
                                        });
                                    }

                                    @Override
                                    public void onCancelled(String message) {
                                        if(target.isFallbackServer() || plugin.getServerManager().getServer(player) == null){
                                            player.kickPlayer(message);
                                        }
                                        else{
                                            player.sendMessage(ChatColor.RED + message);
                                            transferToFallback(player);
                                        }
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event){
        Player player = event.getPlayer();
        zServer server = plugin.getServerManager().getLocalServer(player);
        if(server == null) {
            return;
        }
        UUID uuid = player.getUniqueId();

        zPlayerPreTransferEvent preTransferEvent = null;
        if(!transferring.containsKey(uuid)){
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                @Override
                public void run() {
                    plugin.getRedisBridge().connect(new CallbackRun<Jedis>() {
                        @Override
                        public void callback(Jedis jedis) {
                            jedis.del("onlinePlayers:" + uuid.toString());
                        }
                    });
                }
            });
        }
        else{
            zServerData to = transferring.remove(uuid);
            preTransferEvent = new zPlayerPreTransferEvent(server, player, to);
            server.callEvent(preTransferEvent);
        }

        server.quit(player, preTransferEvent);
        connecting.remove(uuid);
    }

    public zServerData getBestServer(String group, boolean filterOutBusy) {
        Collection<zServerData> servers = plugin.getServerManager().getServersByGroup(group);
        if (group.isEmpty()) {
            return null;
        }
        return servers.stream()
                .filter(server -> !filterOutBusy || !server.isBusy())
                .sorted(Comparator
                        .comparingInt(zServerData::getPlayerCount).reversed() // most players first
                        .thenComparingInt(zServerData::getNumber))             // lowest number wins tie
                .findFirst()
                .orElse(null);
    }

    public zServerData getBestFallback(){
        return getBestServer(plugin.getGlobalConfig().getFallbackGroup(), false);
    }

    public zServerData transferToFallback(Player player){
        return transferServer(player, getBestFallback());
    }

    public zServerData transferToFallback(String player){
        return transferServer(player, getBestFallback());
    }

    public zServerData transferServer(Player player, zServerData data) {
        if (data == null) {
            return null;
        }

        HostData host = data.getHost();
        HostData currentHost = plugin.getHostManager().getHost().getData();

        if (!currentHost.equals(host)) {
            // Cross-host transfer (no join needed)
            zServer current = plugin.getServerManager().getServer(player);
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                plugin.getRedisBridge().connect(jedis -> {
                    Map<String, String> update = new HashMap<>();
                    update.put("server", data.getName());
                    update.put("host", data.getHost().getName());
                    jedis.hmset("onlinePlayers:" + player.getUniqueId().toString(), update);
                    transferring.put(player.getUniqueId(), data);
                    plugin.getHostManager().sendToHost(player, host);
                });
            });
            return data;
        }

        // Local join (same host)
        zServer target = plugin.getServerManager().getLocalServer(data);
        if (target == null) {
            player.kickPlayer(ChatColor.RED + "There are no servers to connect to at this time.");
            return data;
        }

        zServer current = plugin.getServerManager().getServer(player);
        if(current == null) {
            // They were never on a server... meaning just kick them.
            player.kickPlayer(ChatColor.RED + "There are no servers to connect to at this time.");
            return data;
        }

        zPlayerPreTransferEvent pretransfer = new zPlayerPreTransferEvent(current, player, data);
        current.callEvent(pretransfer);
        if(pretransfer.isCancelled()){
            player.sendMessage(pretransfer.getCancelReason());
            return data;
        }

        // Use async join
        target.join(player, false, false, pretransfer, new zServerConnectHandler() {
            @Override
            public void onFailure(Exception ex) {
                ex.printStackTrace();
                onCancelled(ex.getMessage());
            }

            @Override
            public void onSuccess() {
                // Now update Redis and finalize transfer
                plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                    plugin.getRedisBridge().connect(jedis -> {
                        Map<String, String> update = new HashMap<>();
                        update.put("server", data.getName());
                        update.put("host", data.getHost().getName());
                        update.put("online", "true");
                        jedis.hmset("onlinePlayers:" + player.getUniqueId().toString(), update);
                    });
                });
            }

            @Override
            public void onCancelled(String message) {
                if (target.isFallbackServer()) {
                    player.kickPlayer(message);
                } else {
                    player.sendMessage(ChatColor.RED + message);
                    transferToFallback(player);
                }
            }
        });

        return data;
    }


    public zServerData transferServer(String player, zServerData data){
        if(data == null) {
            return data;
        }
        Player bukkitPlayer = plugin.getServer().getPlayer(player);
        if(bukkitPlayer != null && bukkitPlayer.isOnline()){
            // They are local, so fall back to the local logic scope.
            transferServer(bukkitPlayer, data);
            return data;
        }
        zServerData server = plugin.getServerManager().getServerByPlayer(player);
        HostData host = server.getHost();
        // Channel : transferRequest:hostName
        // Message: - playerName
        //            targetServerName
        plugin.getRedisBridge().sendMessage("transferRequest:" + host.getName(), player, data.getName());
        return data;
    }

}
