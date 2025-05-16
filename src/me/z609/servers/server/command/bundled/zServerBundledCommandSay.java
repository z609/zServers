package me.z609.servers.server.command.bundled;

import me.z609.servers.server.command.zServerCommand;
import me.z609.servers.server.command.zServerCommandExecutor;
import me.z609.servers.server.zServer;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public class zServerBundledCommandSay implements zServerCommand {

    @Override
    public String getBaseCommand() {
        return "say";
    }

    @Override
    public String[] getAliases() {
        return new String[0];
    }

    @Override
    public String getDescription() {
        return "Broadcasts the given message as the sender";
    }

    @Override
    public String getUsage() {
        return "<message ...>";
    }

    @Override
    public String getBasePermissionNode() {
        return "bukkit.command.say";
    }

    public static class Executor implements zServerCommandExecutor {
        @Override
        public void execute(zServer server, zServerCommand command, CommandSender sender, String label, String[] args) {
            if (args.length == 0)  {
                sender.sendMessage(ChatColor.RED + "Usage: /" + label + " " + command.getUsage());
                return;
            }

            StringBuilder message = new StringBuilder();
            message.append(ChatColor.LIGHT_PURPLE).append("[");
            if (sender instanceof ConsoleCommandSender) {
                message.append("Server");
            } else if (sender instanceof Player) {
                message.append(((Player) sender).getDisplayName());
            } else {
                message.append(sender.getName());
            }
            message.append(ChatColor.LIGHT_PURPLE).append("] ");

            message.append(args[0]);
            if(args.length > 1){
                for (int i = 1; i < args.length; i++) {
                    message.append(" ").append(args[i]);
                }
            }

            server.broadcastMessage(message.toString());
        }
    }
}
