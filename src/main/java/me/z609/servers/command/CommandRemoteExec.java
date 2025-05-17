package me.z609.servers.command;

import me.z609.servers.server.zServer;
import me.z609.servers.zServers;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class CommandRemoteExec implements CommandExecutor {
    private zServers plugin;

    public CommandRemoteExec(zServers plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(sender instanceof Player){
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }

        if(args.length < 2){
            sender.sendMessage(ChatColor.RED + "Correct Usage: /" + label + " <serverName> <command> [arguments]");
            return true;
        }

        String serverName = args[0];
        zServer server = plugin.getServerManager().getLocalServer(serverName);
        if(server == null){
            sender.sendMessage(ChatColor.RED + "Could not find that server. Is it on this host?");
            return true;
        }

        String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length)).trim();
        if(!server.processCommand(sender, message)){
            sender.sendMessage(ChatColor.RED + "Command " + args[1] + " not found in " + server.getName());
        }
        return true;
    }
}
