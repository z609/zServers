package me.z609.servers.api.event.player;

import me.z609.servers.api.zServersCancellableEvent;
import me.z609.servers.api.zServersPlayerEvent;
import me.z609.servers.server.zServer;
import org.bukkit.entity.Player;

public class zAsyncPlayerPreloginEvent extends zServersPlayerEvent implements zServersCancellableEvent {
    private String cancelled;
    private final boolean newSession;

    public zAsyncPlayerPreloginEvent(zServer server, Player player, boolean newSession) {
        super(server, player);
        this.newSession = newSession;
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
        return newSession;
    }
}
