package me.z609.servers.server.disguise.providers.libs;

import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.comphenix.protocol.wrappers.WrappedSignedProperty;
import com.github.retrooper.packetevents.protocol.player.TextureProperty;
import com.github.retrooper.packetevents.protocol.player.UserProfile;
import com.google.common.collect.Multimap;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import me.z609.servers.server.disguise.zServerPlayerDisguise;

import java.util.UUID;

public class LibsPlayerDisguise extends zServerPlayerDisguise<zServerLibsDisguises> {
    private final PlayerDisguise playerDisguise;
    private final WrappedGameProfile gameProfile;

    LibsPlayerDisguise(String name) {
        this.playerDisguise = new PlayerDisguise(name);

        UserProfile libsProfile = playerDisguise.getUserProfile();
        this.gameProfile = new WrappedGameProfile(libsProfile.getUUID(), libsProfile.getName());

        Multimap<String, WrappedSignedProperty> properties = gameProfile.getProperties();
        properties.clear();

        for(TextureProperty property : libsProfile.getTextureProperties()){
            properties.put(property.getName(), new WrappedSignedProperty(property.getName(),
                    property.getValue(),
                    property.getSignature()));
        }
    }

    @Override
    public String getName() {
        return playerDisguise.getName();
    }

    public PlayerDisguise getPlayerDisguise(){
        return playerDisguise;
    }

    public UUID getUUID() {
        return playerDisguise.getUUID();
    }

    @Override
    public WrappedGameProfile getProfile() {
        return gameProfile;
    }

    public UserProfile getSkin(){
        return playerDisguise.getUserProfile();
    }

    @Override
    public Object getGenericDisguise() {
        return playerDisguise;
    }
}
