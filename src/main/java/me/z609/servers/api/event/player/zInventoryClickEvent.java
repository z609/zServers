package me.z609.servers.api.event.player;

import me.z609.servers.api.zServersCancellableEvent;
import me.z609.servers.api.zServersPlayerEvent;
import me.z609.servers.server.zServer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class zInventoryClickEvent extends zServersPlayerEvent implements zServersCancellableEvent {

    private final InventoryAction action;
    private final ClickType clickType;
    private final Inventory inventory;
    private final Inventory clickedInventory;
    private InventoryType.SlotType slotType;
    private int slot;
    private int rawSlot;
    private ItemStack currentItem;
    private int hotbarKey;
    private boolean cancelled;

    public zInventoryClickEvent(zServer server, Player player, InventoryAction action, ClickType clickType, Inventory inventory,
                                Inventory clickedInventory, InventoryType.SlotType slotType, int slot, int rawSlot,
                                ItemStack currentItem, int hotbarKey, boolean cancelled) {
        super(server, player);
        this.action = action;
        this.clickType = clickType;
        this.inventory = inventory;
        this.clickedInventory = clickedInventory;
        this.slotType = slotType;
        this.slot = slot;
        this.rawSlot = rawSlot;
        this.currentItem = currentItem;
        this.hotbarKey = hotbarKey;
        this.cancelled = cancelled;
    }

    public InventoryAction getAction() {
        return action;
    }

    public ClickType getClickType() {
        return clickType;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public Inventory getClickedInventory() {
        return clickedInventory;
    }

    public InventoryType.SlotType getSlotType() {
        return slotType;
    }

    public void setSlotType(InventoryType.SlotType slotType) {
        this.slotType = slotType;
    }

    public int getSlot() {
        return slot;
    }

    public int getRawSlot() {
        return rawSlot;
    }

    public void setRawSlot(int rawSlot) {
        this.rawSlot = rawSlot;
    }

    public ItemStack getCurrentItem() {
        return currentItem;
    }

    public void setCurrentItem(ItemStack currentItem) {
        this.currentItem = currentItem;
    }

    public int getHotbarKey() {
        return hotbarKey;
    }

    public void setHotbarKey(int hotbarKey) {
        this.hotbarKey = hotbarKey;
    }

    @Override public boolean isCancelled() { return cancelled; }
    @Override public void setCancelled(boolean cancelled) { this.cancelled = cancelled; }
}

