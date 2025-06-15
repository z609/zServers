package me.z609.servers.server.disguise.providers.libs;

import com.comphenix.protocol.wrappers.WrappedGameProfile;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import me.z609.servers.server.disguise.zServerPlayerDisguise;
import org.bukkit.entity.EntityType;

import java.util.UUID;

public class LibsPlayerDisguise extends LibsDisguise implements zServerPlayerDisguise {
    private final PlayerDisguise playerDisguise;
    private final WrappedGameProfile gameProfile;

    LibsPlayerDisguise(String name){
        this(new PlayerDisguise(name));
    }

    LibsPlayerDisguise(PlayerDisguise playerDisguise) {
        this.playerDisguise = playerDisguise;
        this.gameProfile = playerDisguise.getGameProfile();
    }

    public String getName() {
        return playerDisguise.getName();
    }

    public PlayerDisguise getPlayerDisguise(){
        return playerDisguise;
    }

    public UUID getUUID() {
        return playerDisguise.getUUID();
    }

    public WrappedGameProfile getProfile() {
        return gameProfile;
    }

    public static LibsPlayerDisguise asLibsDisguise(PlayerDisguise playerDisguise) {
        return new LibsPlayerDisguise(playerDisguise);
    }

    @Override
    public Object getGenericDisguise() {
        return playerDisguise;
    }

    @Override
    public EntityType getType() {
        return EntityType.PLAYER;
    }
}
