package me.z609.servers.command;

import me.z609.servers.server.zServer;
import me.z609.servers.zServers;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandWhereami implements CommandExecutor {
    private zServers plugin;

    public CommandWhereami(zServers plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if(!(sender instanceof Player)){
            sender.sendMessage(ChatColor.GOLD + "This is a zServers Host: " + plugin.getHost().getName());
            return true;
        }

        zServer server = plugin.getServerManager().getLocalServer((Player) sender);
        sender.sendMessage(ChatColor.GOLD + "You are currently connected to " + server.getName());

        return true;
    }
}
