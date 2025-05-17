package me.z609.servers.command;

import me.z609.servers.server.zServerData;
import me.z609.servers.zServers;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CommandGList implements CommandExecutor  {

    private final zServers plugin;

    public CommandGList(zServers plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!sender.hasPermission("zservers.command.glist")){
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }

        boolean showEmpty = args.length > 0 && args[0].equalsIgnoreCase("all");

        Collection<zServerData> servers = plugin.getServerManager().getServers();
        int totalPlayers = 0;

        for (zServerData server : servers) {
            List<String> players = new ArrayList<>(server.getPlayerNames());
            if (!showEmpty && players.isEmpty()) {
                continue;
            }

            totalPlayers += players.size();

            players.sort(String.CASE_INSENSITIVE_ORDER);
            String playerList = String.join(ChatColor.RESET + ", ", players);

            sender.sendMessage(ChatColor.GREEN + "[" + server.getName() + "]" + ChatColor.YELLOW +
                    " (" + players.size() + "): " + ChatColor.RESET + playerList);
        }

        sender.sendMessage(ChatColor.GOLD + "Total players online: " + totalPlayers);
        return true;
    }
}
