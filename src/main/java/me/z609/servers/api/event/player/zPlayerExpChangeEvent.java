package me.z609.servers.api.event.player;

import me.z609.servers.api.zServersCancellableEvent;
import me.z609.servers.api.zServersPlayerEvent;
import me.z609.servers.server.zServer;
import org.bukkit.entity.Player;

public class zPlayerExpChangeEvent extends zServersPlayerEvent implements zServersCancellableEvent {
    private int amount;
    private boolean cancelled;

    public zPlayerExpChangeEvent(zServer server, Player player, int amount) {
        super(server, player);
        this.amount = amount;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
        if(this.cancelled)
            this.amount = 0;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
        this.cancelled = amount == 0;
    }
}
