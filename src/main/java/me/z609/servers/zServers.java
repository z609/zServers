package me.z609.servers;

import me.z609.servers.cloud.Cloud;
import me.z609.servers.command.*;
import me.z609.servers.connect.ConnectionManager;
import me.z609.servers.host.Host;
import me.z609.servers.host.HostManager;
import me.z609.servers.mojang.MojangAPI;
import me.z609.servers.redis.RedisBridge;
import me.z609.servers.server.zServerManager;
import org.bukkit.configuration.Configuration;
import org.bukkit.plugin.java.JavaPlugin;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.util.Random;
import java.util.logging.Level;

public class zServers extends JavaPlugin {

    public static final Random RANDOM = new Random();
    private Configuration config;
    private boolean shutdown = false;

    private RedisBridge redisBridge;
    private GlobalConfig globalConfig;
    private MojangAPI mojangAPI;
    private Cloud cloud;
    private HostManager hostManager;
    private zServerManager serverManager;
    private ConnectionManager connectionManager;


    @Override
    public void onLoad() {
        config = getConfig();
        getLogger().log(Level.INFO, "Loading zServers configuration...");
        if (!config.contains("redis.host") || config.getString("redis.host").isEmpty()) {
            config.set("redis.host", "127.0.0.1");
        }
        if (!config.contains("redis.port")) {
            config.set("redis.port", 6379);
        }
        if (!config.contains("redis.username")) {
            config.set("redis.username", "default");
        }
        if (!config.contains("redis.password")) {
            config.set("redis.password", "");
        }
        if(!config.contains("message.no-permission")) {
            config.set("message.no-permission", "§cYou don't have permission to use this command!");
        }
        saveConfig();
        reloadConfig();

        getLogger().log(Level.INFO, "Starting Redis bridge [" + config.getString("redis.host") + ":" + config.getInt("redis.port") + "]");
        try{
            this.redisBridge = new RedisBridge(this, config);
            getLogger().log(Level.INFO, "Redis bridge has been successfully completed.");
        } catch (JedisConnectionException ex) {
            throw new RuntimeException("Cannot connect to Redis.", ex);
        }
    }

    @Override
    public void onEnable(){
        this.globalConfig = new GlobalConfig(this);
        getLogger().log(Level.INFO, "Global Config has been initialized - Network Name: " + globalConfig.getNetworkName());

        this.mojangAPI = new MojangAPI(this);
        getLogger().log(Level.INFO, "Mojang API has been initialized.");

        this.cloud = new Cloud(this);
        getLogger().log(Level.INFO, "Cloud bridge has been successfully completed. (provider=" + cloud.getProvider().getProviderName() + ")");

        this.hostManager = new HostManager(this);
        getLogger().log(Level.INFO, "Host Manager has been successfully started.");

        this.serverManager = new zServerManager(this);
        getLogger().log(Level.INFO, "Server Manager has been successfully started.");

        getServer().getPluginManager().registerEvents(this.connectionManager = new ConnectionManager(this), this);
        getLogger().log(Level.INFO, "Connection Manager has been successfully started.");

        getCommand("server").setExecutor(new CommandServer(this));
        getCommand("glist").setExecutor(new CommandGList(this));
        getCommand("find").setExecutor(new CommandFind(this));
        getCommand("send").setExecutor(new CommandSend(this));
        getCommand("hub").setExecutor(new CommandHub(this));
        getCommand("whereami").setExecutor(new CommandWhereami(this));
        getCommand("remoteExec").setExecutor(new CommandRemoteExec(this));
    }

    @Override
    public void onDisable() {
        shutdown = true;
        serverManager.close();
        hostManager.shutdown();
    }

    public boolean isShutdown() {
        return shutdown;
    }

    public RedisBridge getRedisBridge() {
        return redisBridge;
    }

    public HostManager getHostManager() {
        return hostManager;
    }

    public zServerManager getServerManager() {
        return serverManager;
    }

    public GlobalConfig getGlobalConfig() {
        return globalConfig;
    }

    public Cloud getCloud() {
        return cloud;
    }

    public ConnectionManager getConnectionManager() {
        return connectionManager;
    }

    public MojangAPI getMojangAPI() {
        return mojangAPI;
    }

    public Host getHost(){
        return hostManager.getHost();
    }
}
