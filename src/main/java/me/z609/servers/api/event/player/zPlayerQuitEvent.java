package me.z609.servers.api.event.player;

import me.z609.servers.api.zServersPlayerEvent;
import me.z609.servers.server.zServer;
import org.bukkit.entity.Player;

public class zPlayerQuitEvent extends zServersPlayerEvent {
    private String message;
    private zPlayerPreTransferEvent preTransferEvent;

    public zPlayerQuitEvent(zServer server, Player player, zPlayerPreTransferEvent preTransferEvent, String message) {
        super(server, player);
        this.preTransferEvent = preTransferEvent;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public zPlayerPreTransferEvent getPreTransferEvent() {
        return preTransferEvent;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
