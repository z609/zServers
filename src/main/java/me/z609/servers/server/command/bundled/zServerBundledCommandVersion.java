package me.z609.servers.server.command.bundled;

import me.z609.servers.server.command.zServerCommand;
import me.z609.servers.server.command.zServerCommandExecutor;
import me.z609.servers.server.module.zModule;
import me.z609.servers.server.module.zModuleDescription;
import me.z609.servers.server.zServer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;

import java.util.List;

public class zServerBundledCommandVersion implements zServerCommand {
    @Override
    public String getBaseCommand() {
        return "version";
    }

    @Override
    public String[] getAliases() {
        return new String[]{
                "ver",
                "about",
                "icanhasbukkit"
        };
    }

    @Override
    public String getDescription() {
        return "Gets the version of this server including any plugins or zModules in use";
    }

    @Override
    public String getUsage() {
        return "[plugin name|module:<module name>]";
    }

    @Override
    public String getBasePermissionNode() {
        return "bukkit.command.version";
    }

    public static class Executor implements zServerCommandExecutor {
        @Override
        public void execute(zServer server, zServerCommand command, CommandSender sender, String label, String[] args) {
            if (args.length == 0) {
                PluginDescriptionFile zServersDescription = server.getPlugin().getDescription();
                sender.sendMessage("This server is running " + zServersDescription.getName() + " version " +
                        zServersDescription.getVersion() + " by " + zServersDescription.getAuthors().toString() + " " +
                        "on top of " + Bukkit.getVersion() + " (Implementing API version " + Bukkit.getBukkitVersion() + ")");
            } else {
                StringBuilder name = new StringBuilder();

                for (String arg : args) {
                    if (!name.isEmpty()) {
                        name.append(' ');
                    }

                    name.append(arg);
                }

                String pluginName = name.toString();
                zModule module = server.getModule(pluginName);
                if(module != null){
                    sender.sendMessage(ChatColor.RED + "Are you trying to get the information of a module?");
                    sender.sendMessage(ChatColor.RED + "Try using /" + label + " module:" + module.getDescription().getName());
                }

                if(pluginName.toLowerCase().startsWith("module:")){
                    pluginName = pluginName.substring("module:".length());
                    module = server.getModule(pluginName);
                    if(module != null){
                        describeToSender(module, sender);
                        return;
                    }
                }

                Plugin exactPlugin = Bukkit.getPluginManager().getPlugin(pluginName);
                if (exactPlugin != null) {
                    describeToSender(exactPlugin, sender);
                    return;
                }

                boolean found = false;
                pluginName = pluginName.toLowerCase();
                for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
                    if (plugin.getName().toLowerCase().contains(pluginName)) {
                        describeToSender(plugin, sender);
                        found = true;
                    }
                }

                if (!found) {
                    sender.sendMessage("This server is not running any plugin by that name.");
                    sender.sendMessage("Use /plugins to get a list of plugins.");
                }
            }
            return;
        }

        private void describeToSender(Plugin plugin, CommandSender sender) {
            PluginDescriptionFile desc = plugin.getDescription();
            sender.sendMessage(ChatColor.GREEN + desc.getName() + ChatColor.WHITE + " version " + ChatColor.GREEN + desc.getVersion());

            if (desc.getDescription() != null) {
                sender.sendMessage(desc.getDescription());
            }

            if (desc.getWebsite() != null) {
                sender.sendMessage("Website: " + ChatColor.GREEN + desc.getWebsite());
            }

            if (!desc.getAuthors().isEmpty()) {
                if (desc.getAuthors().size() == 1) {
                    sender.sendMessage("Author: " + getAuthors(desc));
                } else {
                    sender.sendMessage("Authors: " + getAuthors(desc));
                }
            }
        }

        private String getAuthors(final PluginDescriptionFile desc) {
            StringBuilder result = new StringBuilder();
            List<String> authors = desc.getAuthors();

            for (int i = 0; i < authors.size(); i++) {
                if (result.length() > 0) {
                    result.append(ChatColor.WHITE);

                    if (i < authors.size() - 1) {
                        result.append(", ");
                    } else {
                        result.append(" and ");
                    }
                }

                result.append(ChatColor.GREEN);
                result.append(authors.get(i));
            }

            return result.toString();
        }

        private void describeToSender(zModule module, CommandSender sender) {
            zModuleDescription desc = module.getDescription();
            sender.sendMessage(ChatColor.GREEN + desc.getName() + ChatColor.WHITE + " version " + ChatColor.GREEN + desc.getVersion());

            if (desc.getDescription() != null) {
                sender.sendMessage(desc.getDescription());
            }

            if (desc.getWebsite() != null) {
                sender.sendMessage("Website: " + ChatColor.GREEN + desc.getWebsite());
            }

            if (!desc.getAuthors().isEmpty()) {
                if (desc.getAuthors().size() == 1) {
                    sender.sendMessage("Author: " + getAuthors(desc));
                } else {
                    sender.sendMessage("Authors: " + getAuthors(desc));
                }
            }
        }

        private String getAuthors(final zModuleDescription desc) {
            StringBuilder result = new StringBuilder();
            List<String> authors = desc.getAuthors();

            for (int i = 0; i < authors.size(); i++) {
                if (result.length() > 0) {
                    result.append(ChatColor.WHITE);

                    if (i < authors.size() - 1) {
                        result.append(", ");
                    } else {
                        result.append(" and ");
                    }
                }

                result.append(ChatColor.GREEN);
                result.append(authors.get(i));
            }

            return result.toString();
        }
    }
}
