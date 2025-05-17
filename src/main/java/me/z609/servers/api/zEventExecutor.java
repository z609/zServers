package me.z609.servers.api;

@FunctionalInterface
public interface zEventExecutor<T extends zServersEvent> {
    void execute(T event);
}
