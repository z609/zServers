package me.z609.servers.server.disguise;

import me.z609.servers.server.disguise.providers.libs.zServerLibsDisguises;
import me.z609.servers.server.zServer;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;

public class zServerDisguises {
    private final zServer server;
    private final DisguiseProvider<?> provider;

    public zServerDisguises(zServer server) {
        this.server = server;

        Plugin libsDisguises = server.getPlugin().getServer().getPluginManager().getPlugin("LibsDisguises");
        if(libsDisguises != null){
            provider = new zServerLibsDisguises();
            provider.hook(server, libsDisguises);
            server.logInfo("Found LibsDisguises - hooked in successfully.");
        }
        else{
            provider = null;
            server.logWarning("Warning - No Disguise provider found (e.g. LibsDisguises). It is not required unless you have modules depending on it.");
        }
    }

    public void enable(){
        if(provider == null)
            return;
        provider.onEnable();
        server.getPlugin().getServer().getPluginManager().registerEvents(provider, server.getPlugin());
        server.logInfo("Disguise bridge(provider=" + provider.getProvidingPlugin().getName() + ") has been enabled.");
    }

    public void disable(){
        if(provider == null)
            return;
        provider.onDisable();
        HandlerList.unregisterAll(provider);
        server.logInfo("Disguise bridge(provider=" + provider.getProvidingPlugin().getName() + ") has been disabled.");
    }

    public zServer getServer() {
        return server;
    }

    public DisguiseProvider<?> getProvider() {
        return provider;
    }

    public boolean isAvailable(){
        return provider != null;
    }
}
