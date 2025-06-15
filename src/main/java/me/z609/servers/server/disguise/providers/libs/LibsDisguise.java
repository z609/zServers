package me.z609.servers.server.disguise.providers.libs;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.MobDisguise;
import me.z609.servers.server.disguise.zServerDisguise;
import org.bukkit.entity.EntityType;

public class LibsDisguise extends zServerDisguise<zServerLibsDisguises> {
    private final Disguise disguise;

    LibsDisguise(EntityType type) {
        super(type);
        this.disguise = new MobDisguise(getType(type));
    }

    LibsDisguise(Disguise disguise) {
        super(disguise.getType().getEntityType());
        this.disguise = disguise;
    }

    public Disguise getDisguise() {
        return disguise;
    }

    @Override
    public Object getGenericDisguise() {
        return disguise;
    }

    public static LibsDisguise asLibsDisguise(Disguise disguise){
        return new LibsDisguise(disguise);
    }

    private static DisguiseType getType(EntityType type){
        return switch(type){
            // Any special cases (where Libs didn't match the exact Bukkit enum value) can be put here
            default -> DisguiseType.valueOf(type.name());
        };
    }
}
