package me.z609.servers.command;

import me.z609.servers.server.zServer;
import me.z609.servers.server.zServerData;
import me.z609.servers.zServers;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class CommandSend implements CommandExecutor {

    private final zServers plugin;

    public CommandSend(zServers plugin) {
        this.plugin = plugin;
        plugin.getCommand("send").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!sender.hasPermission("zservers.command.send")){
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }

        if (args.length != 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /send <player|all|current|group:groupName> <server>");
            return true;
        }

        String targetServerName = args[1];
        zServerData target = plugin.getServerManager().getServerByName(targetServerName);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Target server not found.");
            return true;
        }

        List<String> playerNames = new ArrayList<>();

        switch (args[0].toLowerCase()) {
            case "all":
                playerNames.addAll(plugin.getServerManager().getServers().stream()
                        .flatMap(z -> z.getPlayerNames().stream())
                        .toList());
                break;

            case "current":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "Only players can use /send current");
                    return true;
                }
                zServer localServer = plugin.getServerManager().getLocalServer((Player) sender);
                if (localServer == null) {
                    sender.sendMessage(ChatColor.RED + "You are not in a zServer.");
                    return true;
                }
                playerNames.addAll(localServer.getPlayerNames());
                break;

            default:
                if (args[0].toLowerCase().startsWith("group:")) {
                    String group = args[0].substring("group:".length());
                    playerNames.addAll(plugin.getServerManager().getServersByGroup(group).stream()
                            .flatMap(z -> z.getPlayerNames().stream())
                            .toList());
                } else {
                    playerNames.add(args[0]);
                }
                break;
        }

        if (playerNames.isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "No players to send.");
            return true;
        }

        for (String name : playerNames) {
            plugin.getConnectionManager().transferServer(name, target);
        }

        sender.sendMessage(ChatColor.GREEN + "Sent " + playerNames.size() + " player(s) to " + target.getName());
        return true;
    }
}
