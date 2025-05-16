package me.z609.servers.api.event.player;

import me.z609.servers.api.zServersCancellableEvent;
import me.z609.servers.api.zServersPlayerEvent;
import me.z609.servers.server.zServer;

public class zPlayerLoginEvent extends zServersPlayerEvent implements zServersCancellableEvent {
    private String cancelled;
    private final zAsyncPlayerPreloginEvent preloginEvent;

    public zPlayerLoginEvent(zServer server, zAsyncPlayerPreloginEvent preloginEvent) {
        super(server, preloginEvent.getPlayer());
        this.preloginEvent = preloginEvent;
    }

    @Override
    public boolean isCancelled() {
        return cancelled != null;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = "Login disallowed.";
    }

    public void setCancelled(String cancelReason){
        this.cancelled = cancelReason;
    }

    public String getCancelReason(){
        return cancelled;
    }

    public boolean isNewSession() {
        return preloginEvent.isNewSession();
    }

    public zAsyncPlayerPreloginEvent getPreloginEvent() {
        return preloginEvent;
    }
}
