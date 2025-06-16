package me.z609.servers.api;

import org.bukkit.event.EventPriority;

public interface zEventExecutor<T extends zServersEvent> {
    void execute(T event);
    EventPriority priority();
}
