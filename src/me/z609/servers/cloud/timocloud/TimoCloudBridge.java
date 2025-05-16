package me.z609.servers.cloud.timocloud;

import cloud.timo.TimoCloud.api.*;
import cloud.timo.TimoCloud.api.async.APIRequestFuture;
import cloud.timo.TimoCloud.api.objects.PlayerObject;
import me.z609.servers.cloud.CloudProvider;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.UUID;

public class TimoCloudBridge implements CloudProvider {
    private Plugin plugin;

    private TimoCloudUniversalAPI universalAPI;
    private TimoCloudBukkitAPI bukkitAPI;
    private TimoCloudEventAPI eventAPI;
    private TimoCloudMessageAPI messageAPI;

    public TimoCloudBridge(Plugin plugin) {
        this.plugin = plugin;

        this.universalAPI = TimoCloudAPI.getUniversalAPI();
        this.bukkitAPI = TimoCloudAPI.getBukkitAPI();
        this.eventAPI = TimoCloudAPI.getEventAPI();
        this.messageAPI = TimoCloudAPI.getMessageAPI();
        bukkitAPI.getThisServer().setState("OPEN");
    }

    public boolean sendToServer(String name, String serverName){
        return sendToServer(universalAPI.getPlayer(name), serverName);
    }

    public boolean sendToServer(UUID uniqueId, String serverName){
        return sendToServer(universalAPI.getPlayer(uniqueId), serverName);
    }

    public boolean sendToServer(Player player, String serverName){
        return sendToServer(player.getUniqueId(), serverName);
    }

    private boolean sendToServer(PlayerObject player, String serverName){
        APIRequestFuture<Boolean> request = player.sendToServer(universalAPI.getServer(serverName));
        return request.isSuccess();
    }

    @Override
    public Plugin getPlugin() {
        return plugin;
    }

    @Override
    public String getProviderName() {
        return "TimoCloud";
    }

    @Override
    public boolean markFull() {
        bukkitAPI.getThisServer().setState("FULL");
        return true;
    }

    @Override
    public boolean markNotFull() {
        bukkitAPI.getThisServer().setState("OPEN");
        return true;
    }

    @Override
    public boolean markEmpty() {
        bukkitAPI.getThisServer().setState("EMPTY");
        return true;
    }

    public TimoCloudUniversalAPI getUniversalAPI() {
        return universalAPI;
    }

    public TimoCloudBukkitAPI getBukkitAPI() {
        return bukkitAPI;
    }

    public TimoCloudEventAPI getEventAPI() {
        return eventAPI;
    }

    public TimoCloudMessageAPI getMessageAPI() {
        return messageAPI;
    }
}
