package me.z609.servers.server.disguise;

import com.comphenix.protocol.wrappers.WrappedGameProfile;

import java.util.UUID;

public interface zServerPlayerDisguise extends zServerDisguise {
    String getName();

    UUID getUUID();

    WrappedGameProfile getProfile();
}
