package me.z609.servers.api.event.player;

import me.z609.servers.api.zServersCancellableEvent;
import me.z609.servers.api.zServersPlayerEvent;
import me.z609.servers.server.zServer;
import me.z609.servers.server.zServerData;
import org.bukkit.entity.Player;

public class zPlayerPreTransferEvent extends zServersPlayerEvent implements zServersCancellableEvent {
    private String cancelled;
    private final zServerData to;

    public zPlayerPreTransferEvent(zServer server, Player player, zServerData to) {
        super(server, player);
        this.to = to;
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

    public zServerData getTo() {
        return to;
    }

    public boolean isCrossHost(){
        return to.equals(getServer().getData());
    }
}
