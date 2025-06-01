package me.z609.servers.api.event.entity;

import me.z609.servers.server.zServer;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDamageEvent;

public class zEntityDamageByEntityEvent extends zEntityDamageEvent{
    private final Entity damager;
    public zEntityDamageByEntityEvent(zServer server, Entity entity, Entity damager, EntityDamageEvent.DamageCause cause, double damage, double finalDamage, boolean cancelled) {
        super(server, entity, cause, damage, finalDamage, cancelled);
        this.damager = damager;
    }

    public Entity getDamager() {
        return damager;
    }
}
