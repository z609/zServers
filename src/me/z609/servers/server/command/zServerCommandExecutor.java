package me.z609.servers.server.command;

import me.z609.servers.server.zServer;
import org.bukkit.command.CommandSender;

public interface zServerCommandExecutor {
    void execute(zServer server, zServerCommand command, CommandSender sender, String label, String[] args);
}