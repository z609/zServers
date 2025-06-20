package me.z609.servers.api.event.player;

import me.z609.servers.api.zServersCancellableEvent;
import me.z609.servers.api.zServersPlayerEvent;
import me.z609.servers.server.zServer;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class zBlockBreakEvent extends zServersPlayerEvent implements zServersCancellableEvent {
    private Block block;
    private boolean cancelled;
    private boolean dropItems;
    public zBlockBreakEvent(zServer server, Player player, Block block, boolean cancelled, boolean dropItems) {
        super(server, player);
        this.block = block;
        this.cancelled = cancelled;
        this.dropItems = dropItems;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public boolean isDropItems() {
        return dropItems;
    }

    public void setDropItems(boolean dropItems) {
        this.dropItems = dropItems;
    }

    public Block getBlock() {
        return block;
    }
}
