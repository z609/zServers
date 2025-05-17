package me.z609.servers;

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
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, this::updateConfig, 1200, 1200); //60*20=1200
    }

    private void updateConfig(){
        plugin.getRedisBridge().connect(jedis -> {
            GlobalConfig.this.config = jedis.hgetAll("global-config");
            GlobalConfig.this.fallbackGroup = config.getOrDefault("fallbackGroup", "Hub");
            GlobalConfig.this.networkName = config.getOrDefault("networkName", "Another zServers Network");
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
