package me.z609.servers.server.disguise.providers.libs;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.MobDisguise;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import me.z609.servers.server.disguise.zServerDisguise;
import org.bukkit.entity.EntityType;

public class LibsDisguise implements zServerDisguise {
    protected EntityType type;
    protected Disguise disguise;

    LibsDisguise(){

    }

    LibsDisguise(EntityType type) {
        this.disguise = new MobDisguise(getType(type));
    }

    LibsDisguise(Disguise disguise) {
        this.type = disguise.getEntity().getType();
        this.disguise = disguise;
    }

    public Disguise getDisguise() {
        return disguise;
    }

    @Override
    public Object getGenericDisguise() {
        return disguise;
    }

    @Override
    public EntityType getType() {
        return type;
    }

    public static LibsDisguise asLibsDisguise(Disguise disguise){
        if(disguise instanceof PlayerDisguise playerDisguise)
            return LibsPlayerDisguise.asLibsDisguise(playerDisguise);
        return new LibsDisguise(disguise);
    }

    private static DisguiseType getType(EntityType type){
        return switch(type){
            // Any special cases (where Libs didn't match the exact Bukkit enum value) can be put here
            default -> DisguiseType.valueOf(type.name());
        };
    }
}
