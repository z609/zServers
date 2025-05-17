package me.z609.servers.api.event.player;

import me.z609.servers.api.zServersCancellableEvent;
import me.z609.servers.api.zServersPlayerEvent;
import me.z609.servers.server.zServer;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;

public class zPlayerPickupItemEvent extends zServersPlayerEvent implements zServersCancellableEvent {

    private final Item item;
    private boolean flyAtPlayer = true;
    private final int remaining;
    private boolean cancelled;

    public zPlayerPickupItemEvent(zServer server, Player player, Item item, int remaining, boolean flyAtPlayer, boolean cancelled) {
        super(server, player);
        this.item = item;
        this.remaining = remaining;
        this.flyAtPlayer = flyAtPlayer;
        this.cancelled = cancelled;
    }

    public Item getItem() { return item; }

    public int getRemaining() {
        return remaining;
    }

    public boolean isFlyAtPlayer() {
        return flyAtPlayer;
    }

    public void setFlyAtPlayer(boolean flyAtPlayer) {
        this.flyAtPlayer = flyAtPlayer;
    }

    @Override public boolean isCancelled() { return cancelled; }
    @Override public void setCancelled(boolean cancelled) { this.cancelled = cancelled; }
}
