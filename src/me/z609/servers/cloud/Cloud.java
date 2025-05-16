package me.z609.servers.cloud;

import me.z609.servers.cloud.timocloud.TimoCloudBridge;
import me.z609.servers.zServers;
import org.bukkit.plugin.Plugin;

public class Cloud {

    private CloudProvider provider;
    private zServers plugin;

    private boolean full = false;

    public Cloud(zServers plugin) {
        this.plugin = plugin;

        Plugin timoCloud = plugin.getServer().getPluginManager().getPlugin("TimoCloud");
        if(timoCloud != null && timoCloud.isEnabled()){
            loadTimoCloud(timoCloud);
            return;
        }

        plugin.getLogger().warning("Warning - There is no Cloud Provider to bridge for. This is it - this is the only Host you get. Use zServers in conjunction with a cloud provider like TimoCloud to expand your network further.");
    }

    private void loadTimoCloud(Plugin plugin){
        this.provider = new TimoCloudBridge(plugin);
    }

    /**
     * To be called when the host is full (typically, isSpaceHere in HostManager)
     */
    public void full(){
        if(full)
            return;

        full = true;
        provider.markFull(); // Bridge to Cloud API
    }

    /**
     * To be called when the host WAS full, but no longer is (typically isSpaceHere in HostManager)
     */
    public void notFull(){
        full = false;
        provider.markNotFull(); // Bridge to Cloud API
    }

    public void empty(){
        provider.markEmpty(); // Bridge to Cloud API
    }

    public boolean isFull() {
        return full;
    }

    public CloudProvider getProvider() {
        return provider;
    }

    public String getProviderName(){
        return provider != null ? provider.getProviderName() : "none";
    }
}
