package me.z609.servers.server.disguise;

import me.z609.servers.api.event.player.zPlayerJoinEvent;
import me.z609.servers.api.event.player.zPlayerQuitEvent;
import me.z609.servers.server.zServer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Collection;

public interface DisguiseProvider<T extends DisguiseProvider<T>> {
    void onEnable();
    void onDisable();
    void onJoin(zPlayerJoinEvent event);
    void onQuit(zPlayerQuitEvent event);
    T hook(zServer server, Plugin plugin);
    zServer getServer();
    Plugin getProvidingPlugin();

    void addCustomDisguise(String disguiseName, String disguiseInfo);
    String getRawCustomDisguise(String disguiseName);
    zServerDisguise<T> getCustomDisguise(String disguiseName);
    void removeCustomDisguise(String disguiseName);
    zServerDisguise<T> constructDisguise(Entity entity, boolean doEquipment, boolean displayExtraAnimations);
    zServerDisguise<T> constructDisguise(Entity entity);
    void disguiseEntity(Entity entity, zServerDisguise<T> disguise);
    void disguiseEntity(CommandSender sender, Entity entity, zServerDisguise<T> disguise);
    void disguiseIgnorePlayers(Entity entity, zServerDisguise<T> disguise, Collection playersToNotSeeDisguise);
    void disguiseIgnorePlayers(Entity entity, zServerDisguise<T> disguise, Player... playersToNotSeeDisguise);
    void disguiseIgnorePlayers(Entity entity, zServerDisguise<T> disguise, String... playersToNotSeeDisguise);
    int disguiseNextEntity(zServerDisguise<T> disguise);
    void disguiseToAll(Entity entity, zServerDisguise<T> disguise);
    void disguiseToPlayers(Entity entity, zServerDisguise<T> disguise, Collection playersToViewDisguise);
    void disguiseToPlayers(Entity entity, zServerDisguise<T> disguise, Player... playersToViewDisguise);
    void disguiseToPlayers(Entity entity, zServerDisguise<T> disguise, String... playersToViewDisguise);
    zServerDisguise<T> getDisguise(Entity disguised);
    zServerDisguise<T> getDisguise(Player observer, Entity disguised);
    zServerDisguise[] getDisguises(Entity disguised);
    boolean isDisguised(Entity disguised);
    boolean isDisguised(Player observer, Entity disguised);
    boolean isDisguiseInUse(zServerDisguise<T> disguise);
    void undisguiseToAll(Entity entity);
    void undisguiseToAll(CommandSender sender, Entity entity);
}
