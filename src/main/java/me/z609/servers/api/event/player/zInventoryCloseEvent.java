package me.z609.servers.api.event.player;

import me.z609.servers.api.zServersPlayerEvent;
import me.z609.servers.server.zServer;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;

import java.util.List;

public class zInventoryCloseEvent extends zServersPlayerEvent {
    private static final HandlerList handlers = new HandlerList();

    private final InventoryCloseEvent.Reason reason;
    private final InventoryView transaction;

    public zInventoryCloseEvent(zServer server, Player player, InventoryView transaction) {
        this(server, player, transaction, InventoryCloseEvent.Reason.UNKNOWN);
    }

    public zInventoryCloseEvent(zServer server, Player player, InventoryView transaction, InventoryCloseEvent.Reason reason) {
        super(server, player);
        this.transaction = transaction;
        this.reason = reason;
    }

    public InventoryCloseEvent.Reason getReason() {
        return this.reason;
    }

    public Inventory getInventory() {
        return this.transaction.getTopInventory();
    }

    public List<HumanEntity> getViewers() {
        return this.transaction.getTopInventory().getViewers();
    }

    public InventoryView getView() {
        return this.transaction;
    }
}
