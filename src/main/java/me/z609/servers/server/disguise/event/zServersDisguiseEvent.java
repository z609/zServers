package me.z609.servers.server.disguise.event;

import me.z609.servers.api.zServersCancellableEvent;
import me.z609.servers.api.zServersEvent;
import me.z609.servers.server.disguise.DisguiseProvider;
import me.z609.servers.server.disguise.zServerDisguise;
import org.bukkit.entity.Entity;

public class zServersDisguiseEvent extends zServersEvent implements zServersCancellableEvent {
    private final DisguiseProvider<?> provider;
    private final Entity entity;
    private final zServerDisguise disguise;
    private boolean cancelled = false;

    public zServersDisguiseEvent(DisguiseProvider<?> provider, Entity entity, zServerDisguise disguise, boolean cancelled) {
        super(provider.getServer());
        this.provider = provider;
        this.entity = entity;
        this.disguise = disguise;
        this.cancelled = cancelled;
    }

    public DisguiseProvider<?> getProvider() {
        return provider;
    }

    public Entity getEntity() {
        return entity;
    }

    public zServerDisguise getDisguise() {
        return disguise;
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
