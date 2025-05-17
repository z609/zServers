package me.z609.servers.server.command.bundled;

import me.z609.servers.server.command.zServerCommand;
import me.z609.servers.server.command.zServerCommandExecutor;
import me.z609.servers.server.zServer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;

public class zServerBundledCommandList implements zServerCommand {

    @Override
    public String getBaseCommand() {
        return "list";
    }

    @Override
    public String[] getAliases() {
        return new String[0];
    }

    @Override
    public String getDescription() {
        return "Lists all online players";
    }

    @Override
    public String getUsage() {
        return "";
    }

    @Override
    public String getBasePermissionNode() {
        return "bukkit.command.list";
    }

    public static class Executor implements zServerCommandExecutor {
        @Override
        public void execute(zServer server, zServerCommand command, CommandSender sender, String label, String[] args) {
            StringBuilder online = new StringBuilder();

            final Collection<? extends Player> players = server.getOnlinePlayers();

            for (Player player : players) {
                // If a player is hidden from the sender don't show them in the list
                if (sender instanceof Player && !((Player) sender).canSee(player)) {
                    continue;
                }

                if (online.length() > 0) {
                    online.append(", ");
                }

                online.append(player.getDisplayName());
            }

            sender.sendMessage("There are " + players.size() + "/" + server.getMaxPlayers() + " players online:\n" + online.toString());
        }
    }
}
