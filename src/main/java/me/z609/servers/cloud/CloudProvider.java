package me.z609.servers.cloud;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.UUID;

public interface CloudProvider {

    Plugin getPlugin();

    String getProviderName();

    boolean markFull();

    boolean markNotFull();

    boolean markEmpty();

    boolean sendToServer(String name, String serverName);

    boolean sendToServer(UUID uniqueId, String serverName);

    boolean sendToServer(Player player, String serverName);

}
