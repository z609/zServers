package me.z609.servers.mojang;

import me.z609.servers.zServers;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class MojangAPI {

    private zServers plugin;
    private Set<MojangProfile> profileCache = new HashSet<>();

    public MojangAPI(zServers plugin) {
        this.plugin = plugin;
    }

    public MojangProfile getProfile(UUID uniqueId){
        return getProfile(uniqueId, null);
    }

    public MojangProfile getProfile(String name){
        return getProfile(null, name);
    }

    private MojangProfile getProfile(UUID uniqueId, String name){
        for(MojangProfile profile : profileCache) {
            if(profile.getName().equals(name) || uniqueId != null && profile.getUniqueId().toString().equals(uniqueId.toString())) {
                return profile;
            }
        }
        MojangProfile profile = new MojangProfile(uniqueId, name);
        profileCache.add(profile);
        return profile;
    }

}
