package me.z609.servers.api.event.entity;

import me.z609.servers.api.zServersCancellableEvent;
import me.z609.servers.api.zServersEvent;
import me.z609.servers.server.zServer;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDamageEvent;

public class zEntityDamageEvent extends zServersEvent implements zServersCancellableEvent {

    private final Entity entity;
    private final EntityDamageEvent.DamageCause cause;
    private double damage;
    private boolean cancelled;

    public zEntityDamageEvent(zServer server, Entity entity, EntityDamageEvent.DamageCause cause, double damage, boolean cancelled) {
        super(server);
        this.entity = entity;
        this.cause = cause;
        this.damage = damage;
        this.cancelled = cancelled;
    }

    public Entity getEntity() { return entity; }
    public EntityDamageEvent.DamageCause getCause() { return cause; }
    public double getDamage() { return damage; }
    public void setDamage(double damage) { this.damage = damage; }

    @Override public boolean isCancelled() { return cancelled; }
    @Override public void setCancelled(boolean cancelled) { this.cancelled = cancelled; }
}
