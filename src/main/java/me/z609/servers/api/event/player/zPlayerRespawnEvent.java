package me.z609.servers.api.event.player;

import me.z609.servers.api.zServersPlayerEvent;
import me.z609.servers.server.zServer;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class zPlayerRespawnEvent extends zServersPlayerEvent {
    private Location respawnLocation;

    public zPlayerRespawnEvent(zServer server, Player player, Location respawnLocation) {
        super(server, player);
        this.respawnLocation = respawnLocation;
    }

    public Location getRespawnLocation() {
        return respawnLocation;
    }

    public void setRespawnLocation(Location respawnLocation) {
        this.respawnLocation = respawnLocation;
    }
}
