package me.z609.servers.server.disguise;

import org.bukkit.entity.EntityType;

public abstract class zServerDisguise<T extends DisguiseProvider<T>> {
    protected EntityType type;

    public zServerDisguise(EntityType type){
        this.type = type;
    }

    public abstract Object getGenericDisguise();

    public EntityType getType() {
        return type;
    }
}
