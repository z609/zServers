package me.z609.servers.server.command.bundled;

import me.z609.servers.server.command.zServerCommand;
import me.z609.servers.server.command.zServerCommandExecutor;

public class zServerBundledCommand {
    private final String name;
    private final Class<? extends zServerCommand> command;
    private final Class<? extends zServerCommandExecutor> executor;

    public zServerBundledCommand(String name, Class<? extends zServerCommand> command, Class<? extends zServerCommandExecutor> executor) {
        this.name = name;
        this.command = command;
        this.executor = executor;
    }

    public String getName() {
        return name;
    }

    public Class<? extends zServerCommand> getCommand() {
        return command;
    }

    public Class<? extends zServerCommandExecutor> getExecutor() {
        return executor;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof zServerBundledCommand cmd)) {
            return false;
        }
        return cmd.getName().equals(this.name);
    }
}
