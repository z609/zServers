package me.z609.servers.server.command.bundled;

import me.z609.servers.server.command.zServerCommand;
import me.z609.servers.server.command.zServerCommandExecutor;
import me.z609.servers.server.module.zModule;
import me.z609.servers.server.zServer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import java.util.Collection;

public class zServerBundledCommandPlugins implements zServerCommand {
    @Override
    public String getBaseCommand() {
        return "plugins";
    }

    @Override
    public String[] getAliases() {
        return new String[]{
                "pl",
                "modules",
                "mods"
        };
    }

    @Override
    public String getDescription() {
        return "Gets a list of plugins and zModules running on the server";
    }

    @Override
    public String getUsage() {
        return "";
    }

    @Override
    public String getBasePermissionNode() {
        return "bukkit.command.plugins";
    }

    public static class Executor implements zServerCommandExecutor {
        @Override
        public void execute(zServer server, zServerCommand command, CommandSender sender, String label, String[] args) {
            sender.sendMessage("Plugins " + getPluginList());
            sender.sendMessage("Modules " + getModuleList(server));
        }

        private String getPluginList() {
            StringBuilder pluginList = new StringBuilder();
            Plugin[] plugins = Bukkit.getPluginManager().getPlugins();

            for (Plugin plugin : plugins) {
                if (pluginList.length() > 0) {
                    pluginList.append(ChatColor.WHITE);
                    pluginList.append(", ");
                }

                pluginList.append(plugin.isEnabled() ? ChatColor.GREEN : ChatColor.RED);
                pluginList.append(plugin.getDescription().getName());
            }

            return "(" + plugins.length + "): " + pluginList.toString();
        }

        private String getModuleList(zServer server) {
            StringBuilder moduleList = new StringBuilder();
            Collection<zModule> modules = server.getModules();

            for (zModule module : modules) {
                if (moduleList.length() > 0) {
                    moduleList.append(ChatColor.WHITE);
                    moduleList.append(", ");
                }

                moduleList.append(module.isEnabled() ? ChatColor.GREEN : ChatColor.RED);
                moduleList.append(module.getDescription().getName());
            }

            return "(" + modules.size() + "): " + moduleList.toString();
        }
    }
}
