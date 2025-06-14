package me.z609.servers.command;

import me.z609.servers.mojang.MojangProfile;
import me.z609.servers.zServers;
import org.bukkit.ChatColor;
import org.bukkit.command.*;

public class CommandFind implements CommandExecutor {

    private final zServers plugin;

    public CommandFind(zServers plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!sender.hasPermission("zservers.command.find")){
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /find <player>");
            return true;
        }

        MojangProfile profile = plugin.getMojangAPI().getProfile(args[0]);
        String location = plugin.getRedisBridge().connect(jedis -> {
            return jedis.hget("onlinePlayers", profile.getUniqueId().toString());
        });

        if (location == null) {
            sender.sendMessage(ChatColor.RED + "Player " + profile.getName() + " is not online.");
        } else {
            sender.sendMessage(ChatColor.YELLOW + "Player " + profile.getName() + " is currently on " + ChatColor.GOLD + location);
        }

        return true;
    }
}
