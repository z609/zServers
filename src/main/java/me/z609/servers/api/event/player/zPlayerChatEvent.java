package me.z609.servers.api.event.player;

import me.z609.servers.api.zServersCancellableEvent;
import me.z609.servers.api.zServersPlayerEvent;
import me.z609.servers.server.zServer;
import org.bukkit.entity.Player;

import java.util.Set;

public class zPlayerChatEvent extends zServersPlayerEvent implements zServersCancellableEvent {
    private boolean cancelled;
    private String message;
    private Set<Player> recipients;
    private String format = "<%1$s> %2$s";

    public zPlayerChatEvent(zServer server, Player player, boolean cancelled, String message, Set<Player> recipients) {
        super(server, player);
        this.cancelled = cancelled;
        this.message = message;
        this.recipients = recipients;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Set<Player> getRecipients() {
        return recipients;
    }

    public void setRecipients(Set<Player> recipients) {
        this.recipients = recipients;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
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
