package me.z609.servers.server.command;

public interface zServerCommand {

    String getBaseCommand();

    String[] getAliases();

    String getDescription();

    String getUsage();

    String getBasePermissionNode();

}
