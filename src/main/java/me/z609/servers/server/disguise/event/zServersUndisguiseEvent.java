package me.z609.servers.server.disguise.event;

import me.z609.servers.api.zServersEvent;
import me.z609.servers.server.disguise.DisguiseProvider;
import me.z609.servers.server.disguise.zServerDisguise;
import org.bukkit.entity.Entity;

public class zServersUndisguiseEvent extends zServersEvent {
    private final DisguiseProvider<?> provider;
    private final Entity entity;
    private final zServerDisguise disguise;

    public zServersUndisguiseEvent(DisguiseProvider<?> provider, Entity entity, zServerDisguise disguise) {
        super(provider.getServer());
        this.provider = provider;
        this.entity = entity;
        this.disguise = disguise;
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
}
