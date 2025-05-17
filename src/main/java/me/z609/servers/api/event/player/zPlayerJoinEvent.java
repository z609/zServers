package me.z609.servers.api.event.player;

import me.z609.servers.api.zServersPlayerEvent;
import me.z609.servers.server.zServer;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class zPlayerJoinEvent extends zServersPlayerEvent {
    private Location spawnpoint;
    private String message;

    public zPlayerJoinEvent(zServer server, Player player, Location spawnpoint, String message) {
        super(server, player);
        this.spawnpoint = spawnpoint;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Location getSpawnpoint() {
        return spawnpoint;
    }

    public void setSpawnpoint(Location spawnpoint) {
        this.spawnpoint = spawnpoint;
    }
}
