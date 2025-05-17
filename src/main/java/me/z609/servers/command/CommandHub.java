package me.z609.servers.command;

import me.z609.servers.server.zServer;
import me.z609.servers.server.zServerData;
import me.z609.servers.zServers;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class CommandHub implements CommandExecutor {

    private final zServers plugin;

    public CommandHub(zServers plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }
        
        zServer server = plugin.getServerManager().getLocalServer(player);
        zServerData target = plugin.getConnectionManager().getBestFallback();
        if(target != null){
            if(target.equals(server.getData())) {
                // Already on the best server, but since they're executing it from the best one we'll send them to a random one.
                List<zServerData> hubs = new ArrayList<>(plugin.getServerManager().getServersByGroup(plugin.getGlobalConfig().getFallbackGroup()));
                hubs.remove(target);
                if(!hubs.isEmpty()){
                    target = hubs.get(zServers.RANDOM.nextInt(hubs.size()));
                }
            }
        }

        if(target == null || target.equals(server.getData())){
            sender.sendMessage(ChatColor.RED + "There are no servers to connect to at this time.");
            return true;
        }

        // Dispatch transfer
        sender.sendMessage(ChatColor.GREEN + "Connecting to " + target.getName() + "...");
        plugin.getConnectionManager().transferServer(player, target);
        return true;
    }

}