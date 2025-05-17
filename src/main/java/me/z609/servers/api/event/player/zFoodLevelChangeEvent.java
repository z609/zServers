package me.z609.servers.api.event.player;

import me.z609.servers.api.zServersCancellableEvent;
import me.z609.servers.api.zServersPlayerEvent;
import me.z609.servers.server.zServer;
import org.bukkit.entity.Player;

public class zFoodLevelChangeEvent extends zServersPlayerEvent implements zServersCancellableEvent {
    private int foodLevel;
    private boolean cancelled;

    public zFoodLevelChangeEvent(zServer server, Player player, int foodLevel, boolean cancelled) {
        super(server, player);
        this.foodLevel = foodLevel;
        this.cancelled = cancelled;
    }

    public int getFoodLevel() {
        return foodLevel;
    }

    public void setFoodLevel(int foodLevel) {
        this.foodLevel = foodLevel;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
