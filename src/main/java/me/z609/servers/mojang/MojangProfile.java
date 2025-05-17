package me.z609.servers.mojang;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

public class MojangProfile {

    private long fetched = 0;
    private UUID uniqueId;
    private String name;

    public MojangProfile(UUID uniqueId, String name) {
        this.uniqueId = uniqueId;
        this.name = name;
        fetch();
    }

    public void fetch(){
        if(System.currentTimeMillis() - fetched < 86400000) { // 24 * 60 * 60 * 1000=86400000
            return;
        }

        fetched = System.currentTimeMillis();

        OfflinePlayer player = null;
        if(uniqueId != null) {
            player = Bukkit.getOfflinePlayer(uniqueId);
        } else if(name != null) {
            player = Bukkit.getOfflinePlayer(name);
        }

        this.uniqueId = player.getUniqueId();
        this.name = player.getName();
    }

    public UUID getUniqueId() {
        return uniqueId;
    }

    public String getName() {
        return name;
    }
}
