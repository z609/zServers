package me.z609.servers.connect;

import me.z609.servers.server.zServer;

import java.util.Collections;
import java.util.Map;

public class InboundConnection {
    private final zServer target;
    private final Map<String, String> data;
    private final String sessionId;

    public InboundConnection(zServer target, Map<String, String> data) {
        this.target = target;
        this.data = Collections.unmodifiableMap(data);
        this.sessionId = data.get("session");
    }

    public zServer getTarget() {
        return target;
    }

    public Map<String, String> getData() {
        return data;
    }

    public String getSessionId() {
        return sessionId;
    }

    public boolean isNewSession() {
        return !"true".equalsIgnoreCase(data.get("online")) &&
                target.getPlugin().getHost().getName().equalsIgnoreCase(data.get("host"));
    }
}
