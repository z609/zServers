package me.z609.servers.api.event.player;

import me.z609.servers.api.zServersCancellableEvent;
import me.z609.servers.api.zServersPlayerEvent;
import me.z609.servers.server.zServer;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class zPlayerMoveEvent extends zServersPlayerEvent implements zServersCancellableEvent {

    private Location from;
    private Location to;
    private boolean cancelled;

    public zPlayerMoveEvent(zServer server, Player player, Location from, Location to, boolean cancelled) {
        super(server, player);
        this.from = from;
        this.to = to;
        this.cancelled = cancelled;
    }

    public Location getFrom() { return from; }
    public Location getTo() { return to; }
    public void setTo(Location to) { this.to = to; }

    public void setFrom(Location from) {
        this.from = from;
    }

    @Override public boolean isCancelled() { return cancelled; }
    @Override public void setCancelled(boolean cancelled) { this.cancelled = cancelled; }
}
