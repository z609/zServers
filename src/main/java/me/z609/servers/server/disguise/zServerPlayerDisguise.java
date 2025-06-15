package me.z609.servers.server.disguise;

import com.comphenix.protocol.wrappers.WrappedGameProfile;

import java.util.UUID;

public abstract class zServerPlayerDisguise<T extends DisguiseProvider<T>> extends zServerDisguise<T> {
    public abstract String getName();

    public abstract UUID getUUID();

    public abstract WrappedGameProfile getProfile();
}
