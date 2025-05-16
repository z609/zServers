package me.z609.servers.command;

import me.z609.servers.Callback;
import me.z609.servers.mojang.MojangProfile;
import me.z609.servers.zServers;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import redis.clients.jedis.Jedis;

import java.util.UUID;

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
        String location = plugin.getRedisBridge().connect(new Callback<String, Jedis>() {
            @Override
            public String callback(Jedis jedis) {
                return jedis.hget("onlinePlayers", profile.getUniqueId().toString());
            }
        });

        if (location == null) {
            sender.sendMessage(ChatColor.RED + "Player " + profile.getName() + " is not online.");
        } else {
            sender.sendMessage(ChatColor.YELLOW + "Player " + profile.getName() + " is currently on " + ChatColor.GOLD + location);
        }

        return true;
    }
}
