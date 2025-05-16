package me.z609.servers.command;

import me.z609.servers.server.zServer;
import me.z609.servers.server.zServerData;
import me.z609.servers.zServers;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.stream.Collectors;

public class CommandServer implements CommandExecutor {

    private final zServers plugin;

    public CommandServer(zServers plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;
        zServer currentServer = plugin.getServerManager().getLocalServer(player);

        if (args.length == 0) {
            sender.sendMessage(ChatColor.GOLD + "You are currently connected to " + currentServer.getName());
            String available = plugin.getServerManager().getServers().stream()
                    .map(zServerData::getName)
                    .sorted()
                    .collect(Collectors.joining(", "));
            sender.sendMessage(ChatColor.GOLD + "You may connect to the following servers at this time: " + available);
            return true;
        }

        String targetName = args[0];
        zServerData target = plugin.getServerManager().getServerByName(targetName);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "The specified server does not exist.");
            return true;
        }

        if (target.getName().equalsIgnoreCase(currentServer.getName())) {
            sender.sendMessage(ChatColor.GRAY + "You are already connected to this server!");
            return true;
        }

        // Dispatch transfer
        sender.sendMessage(ChatColor.GREEN + "Connecting to " + target.getName() + "...");
        plugin.getConnectionManager().transferServer(player, target);
        return true;
    }

}
