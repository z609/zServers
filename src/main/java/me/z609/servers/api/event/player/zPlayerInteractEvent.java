package me.z609.servers.api.event.player;

import me.z609.servers.api.zServersCancellableEvent;
import me.z609.servers.api.zServersPlayerEvent;
import me.z609.servers.server.zServer;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class zPlayerInteractEvent extends zServersPlayerEvent implements zServersCancellableEvent {

    private final Action action;
    private final ItemStack item;
    private final Block clickedBlock;
    private BlockFace blockFace;
    private Event.Result useClickedBlock;
    private Event.Result useItemInHand;
    private EquipmentSlot hand;
    private boolean cancelled;

    public zPlayerInteractEvent(zServer server, Player player, Action action, ItemStack item, Block clickedBlock, BlockFace blockFace, Event.Result useClickedBlock, Event.Result useItemInHand, EquipmentSlot hand, boolean cancelled) {
        super(server, player);
        this.action = action;
        this.item = item;
        this.clickedBlock = clickedBlock;
        this.blockFace = blockFace;
        this.useClickedBlock = useClickedBlock;
        this.useItemInHand = useItemInHand;
        this.hand = hand;
        this.cancelled = cancelled;
    }

    public Action getAction() { return action; }
    public ItemStack getItem() { return item; }
    public Block getClickedBlock() { return clickedBlock; }

    public BlockFace getBlockFace() {
        return blockFace;
    }

    public void setUseItemInHand(Event.Result useItemInHand) {
        this.useItemInHand = useItemInHand;
    }

    public void setUseClickedBlock(Event.Result useClickedBlock) {
        this.useClickedBlock = useClickedBlock;
    }

    public EquipmentSlot getHand() {
        return hand;
    }

    public Event.Result getUseClickedBlock() {
        return useClickedBlock;
    }

    public Event.Result getUseItemInHand() {
        return useItemInHand;
    }

    public Material getMaterial() {
        return !this.hasItem() ? Material.AIR : this.item.getType();
    }

    public boolean hasBlock() {
        return this.clickedBlock != null;
    }

    public boolean hasItem() {
        return this.item != null;
    }

    public boolean isBlockInHand() {
        return this.hasItem() && this.item.getType().isBlock();
    }

    @Override public boolean isCancelled() { return cancelled; }
    @Override public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
        this.useClickedBlock = cancelled ? Event.Result.DENY : Event.Result.ALLOW;
        this.useItemInHand = cancelled ? Event.Result.DENY : Event.Result.ALLOW;
    }
}

