package me.z609.servers;

import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.Map;

public class GlobalConfig {

    private zServers plugin;

    private Map<String, String> config = new HashMap<>();
    private String fallbackGroup;
    private String networkName;

    public GlobalConfig(zServers plugin) {
        this.plugin = plugin;

        updateConfig();
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                updateConfig();
            }
        }, 60*20, 60*20);
    }

    private void updateConfig(){
        plugin.getRedisBridge().connect(new CallbackRun<Jedis>() {
            @Override
            public void callback(Jedis jedis) {
                GlobalConfig.this.config = jedis.hgetAll("global-config");
                GlobalConfig.this.fallbackGroup = config.getOrDefault("fallbackGroup", "Hub");
                GlobalConfig.this.networkName = config.getOrDefault("networkName", "Another zServers Network");
            }
        });
    }

    public zServers getPlugin() {
        return plugin;
    }

    public Map<String, String> getConfig() {
        return config;
    }

    public String getFallbackGroup() {
        return fallbackGroup;
    }

    public String getNetworkName() {
        return networkName;
    }
}
