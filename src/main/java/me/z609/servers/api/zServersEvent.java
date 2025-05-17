package me.z609.servers.api;

import me.z609.servers.host.Host;
import me.z609.servers.server.zServer;

public abstract class zServersEvent {
    private zServer server;
    private Host host;

    public zServersEvent(zServer server) {
        this.server = server;
        this.host = server.getPlugin().getHostManager().getHost();
    }

    public zServer getServer() {
        return server;
    }

    public Host getHost() {
        return host;
    }
}
