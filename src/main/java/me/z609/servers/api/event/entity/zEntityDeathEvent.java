package me.z609.servers.api.event.entity;

import me.z609.servers.api.zServersCancellableEvent;
import me.z609.servers.api.zServersEvent;
import me.z609.servers.server.zServer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class zEntityDeathEvent extends zServersEvent implements zServersCancellableEvent {

    private final LivingEntity entity;
    private final List<ItemStack> drops;
    private int droppedExp;
    private boolean cancelled;

    public zEntityDeathEvent(zServer server, LivingEntity entity, List<ItemStack> drops, int droppedExp, boolean cancelled) {
        super(server);
        this.entity = entity;
        this.drops = drops;
        this.droppedExp = droppedExp;
        this.cancelled = cancelled;
    }

    public LivingEntity getEntity() { return entity; }

    public List<ItemStack> getDrops() { return drops; }
    public int getDroppedExp() { return droppedExp; }
    public void setDroppedExp(int droppedExp) {
        this.droppedExp = droppedExp;
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

