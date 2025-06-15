package me.z609.servers.server.disguise;

import com.comphenix.protocol.wrappers.WrappedGameProfile;

import java.util.UUID;

public interface zServerPlayerDisguise<T extends DisguiseProvider<T>> {
    String getName();

    UUID getUUID();

    WrappedGameProfile getProfile();
}
