package me.z609.servers.api;

import me.z609.servers.server.zServer;
import org.bukkit.entity.Player;

public class zServersPlayerEvent extends zServersEvent{
    private Player player;

    public zServersPlayerEvent(zServer server, Player player) {
        super(server);
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }
}
