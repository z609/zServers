package me.z609.servers.api.event.player;

import me.z609.servers.api.zServersCancellableEvent;
import me.z609.servers.api.zServersPlayerEvent;
import me.z609.servers.server.zServer;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;

public class zPlayerDropItemEvent extends zServersPlayerEvent implements zServersCancellableEvent {

    private final Item itemDrop;
    private boolean cancelled;

    public zPlayerDropItemEvent(zServer server, Player player, Item itemDrop) {
        super(server, player);
        this.itemDrop = itemDrop;
    }

    public Item getItemDrop() { return itemDrop; }

    @Override public boolean isCancelled() { return cancelled; }
    @Override public void setCancelled(boolean cancelled) { this.cancelled = cancelled; }
}

