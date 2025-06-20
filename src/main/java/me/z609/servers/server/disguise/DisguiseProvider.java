package me.z609.servers.server.disguise;

import com.comphenix.protocol.wrappers.WrappedGameProfile;
import me.z609.servers.api.event.player.zPlayerJoinEvent;
import me.z609.servers.api.event.player.zPlayerQuitEvent;
import me.z609.servers.server.zServer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.util.Collection;

public interface DisguiseProvider<T extends DisguiseProvider<T>> extends Listener {
    void onEnable();
    void onDisable();
    void onJoin(zPlayerJoinEvent event);
    void onQuit(zPlayerQuitEvent event);
    T hook(zServer server, Plugin plugin);
    zServer getServer();
    Plugin getProvidingPlugin();

    void addCustomDisguise(String disguiseName, String disguiseInfo);
    String getRawCustomDisguise(String disguiseName);
    zServerDisguise getCustomDisguise(String disguiseName);
    void removeCustomDisguise(String disguiseName);
    zServerDisguise constructDisguise(Entity entity, boolean doEquipment, boolean displayExtraAnimations);
    zServerDisguise constructDisguise(Entity entity);
    zServerDisguise buildDisguise(EntityType type);
    zServerPlayerDisguise buildPlayerDisguise(String name);
    zServerPlayerDisguise buildPlayerDisguise(WrappedGameProfile profile);
    void disguiseEntity(Entity entity, zServerDisguise disguise);
    void disguiseEntity(CommandSender sender, Entity entity, zServerDisguise disguise);
    void disguiseIgnorePlayers(Entity entity, zServerDisguise disguise, Collection playersToNotSeeDisguise);
    void disguiseIgnorePlayers(Entity entity, zServerDisguise disguise, Player... playersToNotSeeDisguise);
    void disguiseIgnorePlayers(Entity entity, zServerDisguise disguise, String... playersToNotSeeDisguise);
    int disguiseNextEntity(zServerDisguise disguise);
    void disguiseToAll(Entity entity, zServerDisguise disguise);
    void disguiseToPlayers(Entity entity, zServerDisguise disguise, Collection playersToViewDisguise);
    void disguiseToPlayers(Entity entity, zServerDisguise disguise, Player... playersToViewDisguise);
    void disguiseToPlayers(Entity entity, zServerDisguise disguise, String... playersToViewDisguise);
    zServerDisguise getDisguise(Entity disguised);
    zServerDisguise getDisguise(Player observer, Entity disguised);
    zServerDisguise[] getDisguises(Entity disguised);
    boolean isDisguised(Entity disguised);
    boolean isDisguised(Player observer, Entity disguised);
    boolean isDisguiseInUse(zServerDisguise disguise);
    void undisguiseToAll(Entity entity);
    void undisguiseToAll(CommandSender sender, Entity entity);
}
