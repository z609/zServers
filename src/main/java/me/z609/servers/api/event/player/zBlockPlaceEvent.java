package me.z609.servers.api.event.player;

import me.z609.servers.api.zServersCancellableEvent;
import me.z609.servers.api.zServersPlayerEvent;
import me.z609.servers.server.zServer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class zBlockPlaceEvent extends zServersPlayerEvent implements zServersCancellableEvent {
    private Block block;
    private boolean cancelled;
    private boolean canBuild;
    private EquipmentSlot hand;
    private ItemStack itemInHand;
    private Block placedAgainst;
    private BlockState replacedBlockState;

    public zBlockPlaceEvent(zServer server, Player player, Block block, boolean cancelled, boolean canBuild,
                            EquipmentSlot hand, ItemStack itemInHand, Block placedAgainst, BlockState replacedBlockState) {
        super(server, player);
        this.block = block;
        this.cancelled = cancelled;
        this.canBuild = canBuild;
        this.hand = hand;
        this.itemInHand = itemInHand;
        this.placedAgainst = placedAgainst;
        this.replacedBlockState = replacedBlockState;
    }

    public Block getBlock() {
        return block;
    }

    public void setCanBuild(boolean canBuild) {
        this.canBuild = canBuild;
    }

    public boolean isCanBuild() {
        return canBuild;
    }

    public EquipmentSlot getHand() {
        return hand;
    }

    public ItemStack getItemInHand() {
        return itemInHand;
    }

    public Block getPlacedAgainst() {
        return placedAgainst;
    }

    public BlockState getReplacedBlockState() {
        return replacedBlockState;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
