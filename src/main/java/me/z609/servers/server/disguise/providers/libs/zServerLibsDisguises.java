package me.z609.servers.server.disguise.providers.libs;

import com.comphenix.protocol.wrappers.WrappedGameProfile;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.events.DisguiseEvent;
import me.libraryaddict.disguise.events.UndisguiseEvent;
import me.libraryaddict.disguise.utilities.parser.DisguiseParseException;
import me.z609.servers.api.event.player.zPlayerJoinEvent;
import me.z609.servers.api.event.player.zPlayerQuitEvent;
import me.z609.servers.server.disguise.DisguiseProvider;
import me.z609.servers.server.disguise.event.zServersDisguiseEvent;
import me.z609.servers.server.disguise.event.zServersUndisguiseEvent;
import me.z609.servers.server.disguise.zServerDisguise;
import me.z609.servers.server.disguise.zServerPlayerDisguise;
import me.z609.servers.server.zServer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class zServerLibsDisguises implements DisguiseProvider<zServerLibsDisguises> {
    private zServer server;
    private LibsDisguises libsDisguises;

    @Override
    public void onEnable() {

    }

    @Override
    public void onDisable() {

    }

    @Override
    public void onJoin(zPlayerJoinEvent event) {

    }

    @Override
    public void onQuit(zPlayerQuitEvent event) {
        DisguiseAPI.undisguiseToAll(event.getPlayer());
    }

    @Override
    public zServerLibsDisguises hook(zServer server, Plugin plugin) {
        this.server = server;
        this.libsDisguises = (LibsDisguises) plugin;
        return this;
    }

    @Override
    public zServer getServer() {
        return server;
    }

    @Override
    public Plugin getProvidingPlugin() {
        return libsDisguises;
    }

    @Override
    public void addCustomDisguise(String disguiseName, String disguiseInfo) {
        try {
            DisguiseAPI.addCustomDisguise(disguiseName, disguiseInfo);
        } catch (DisguiseParseException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getRawCustomDisguise(String disguiseName) {
        return DisguiseAPI.getRawCustomDisguise(disguiseName);
    }

    @Override
    public zServerDisguise getCustomDisguise(String disguiseName) {
        return LibsDisguise.asLibsDisguise(DisguiseAPI.getCustomDisguise(disguiseName));
    }

    @Override
    public void removeCustomDisguise(String disguiseName) {
        DisguiseAPI.removeCustomDisguise(disguiseName);
    }

    @Override
    public zServerDisguise constructDisguise(Entity entity, boolean doEquipment, boolean displayExtraAnimations) {
        return LibsDisguise.asLibsDisguise(DisguiseAPI.constructDisguise(entity, doEquipment, displayExtraAnimations));
    }

    @Override
    public zServerDisguise constructDisguise(Entity entity) {
        return LibsDisguise.asLibsDisguise(DisguiseAPI.constructDisguise(entity));
    }

    @Override
    public zServerDisguise buildDisguise(EntityType type) {
        return new LibsDisguise(type);
    }

    @Override
    public zServerPlayerDisguise buildPlayerDisguise(String name) {
        return new LibsPlayerDisguise(name);
    }

    @Override
    public zServerPlayerDisguise buildPlayerDisguise(WrappedGameProfile profile) {
        return new LibsPlayerDisguise(profile);
    }

    @Override
    public void disguiseEntity(Entity entity, zServerDisguise disguise) {
        DisguiseAPI.disguiseEntity(entity, (Disguise) disguise.getGenericDisguise());
    }

    @Override
    public void disguiseEntity(CommandSender sender, Entity entity, zServerDisguise disguise) {
        DisguiseAPI.disguiseEntity(sender, entity, (Disguise) disguise.getGenericDisguise());
    }

    @Override
    public void disguiseIgnorePlayers(Entity entity, zServerDisguise disguise, Collection playersToNotSeeDisguise) {
        DisguiseAPI.disguiseIgnorePlayers(entity, (Disguise) disguise.getGenericDisguise(), playersToNotSeeDisguise);
    }

    @Override
    public void disguiseIgnorePlayers(Entity entity, zServerDisguise disguise, Player... playersToNotSeeDisguise) {
        DisguiseAPI.disguiseIgnorePlayers(entity, (Disguise) disguise.getGenericDisguise(), playersToNotSeeDisguise);
    }

    @Override
    public void disguiseIgnorePlayers(Entity entity, zServerDisguise disguise, String... playersToNotSeeDisguise) {
        DisguiseAPI.disguiseIgnorePlayers(entity, (Disguise) disguise.getGenericDisguise(), playersToNotSeeDisguise);
    }

    @Override
    public int disguiseNextEntity(zServerDisguise disguise) {
        return DisguiseAPI.disguiseNextEntity((Disguise) disguise.getGenericDisguise());
    }

    @Override
    public void disguiseToAll(Entity entity, zServerDisguise disguise) {
        DisguiseAPI.disguiseToAll(entity, (Disguise) disguise.getGenericDisguise());
    }

    @Override
    public void disguiseToPlayers(Entity entity, zServerDisguise disguise, Collection playersToViewDisguise) {
        DisguiseAPI.disguiseToPlayers(entity, (Disguise) disguise.getGenericDisguise(), playersToViewDisguise);
    }

    @Override
    public void disguiseToPlayers(Entity entity, zServerDisguise disguise, Player... playersToViewDisguise) {
        DisguiseAPI.disguiseToPlayers(entity, (Disguise) disguise.getGenericDisguise(), playersToViewDisguise);
    }

    @Override
    public void disguiseToPlayers(Entity entity, zServerDisguise disguise, String... playersToViewDisguise) {
        DisguiseAPI.disguiseToPlayers(entity, (Disguise) disguise.getGenericDisguise(), playersToViewDisguise);
    }

    @Override
    public zServerDisguise getDisguise(Entity disguised) {
        return LibsDisguise.asLibsDisguise(DisguiseAPI.getDisguise(disguised));
    }

    @Override
    public zServerDisguise getDisguise(Player observer, Entity disguised) {
        return LibsDisguise.asLibsDisguise(DisguiseAPI.getDisguise(observer, disguised));
    }

    @Override
    public zServerDisguise[] getDisguises(Entity disguised) {
        List<zServerDisguise> list = new ArrayList<>();
        Disguise[] disguises = DisguiseAPI.getDisguises(disguised);
        for (Disguise disguise : disguises)
            list.add(LibsDisguise.asLibsDisguise(disguise));
        return list.toArray(new zServerDisguise[0]);
    }

    @Override
    public boolean isDisguised(Entity disguised) {
        return DisguiseAPI.isDisguised(disguised);
    }

    @Override
    public boolean isDisguised(Player observer, Entity disguised) {
        return DisguiseAPI.isDisguised(observer, disguised);
    }

    @Override
    public boolean isDisguiseInUse(zServerDisguise disguise) {
        return DisguiseAPI.isDisguiseInUse((Disguise) disguise.getGenericDisguise());
    }

    @Override
    public void undisguiseToAll(Entity entity) {
        DisguiseAPI.undisguiseToAll(entity);
    }

    @Override
    public void undisguiseToAll(CommandSender sender, Entity entity) {
        DisguiseAPI.undisguiseToAll(sender, entity);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onUndisguise(UndisguiseEvent event) {
        Entity entity = event.getEntity();
        zServer server = this.server.getManager().getServer(entity.getLocation());
        if(entity instanceof Player player){
            server = this.server.getManager().getServer(player);
        }
        if(server == null || !server.equals(this.server))
            return;

        zServerDisguise disguise = LibsDisguise.asLibsDisguise(event.getDisguise());
        zServersUndisguiseEvent undisguiseEvent = new zServersUndisguiseEvent(this, entity, disguise);
        server.callEvent(undisguiseEvent);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDisguise(DisguiseEvent event) {
        Entity entity = event.getEntity();
        zServer server = this.server.getManager().getServer(entity.getLocation());
        if(entity instanceof Player player){
            server = this.server.getManager().getServer(player);
        }
        if(server == null || !server.equals(this.server))
            return;

        zServerDisguise disguise = LibsDisguise.asLibsDisguise(event.getDisguise());
        zServersDisguiseEvent disguiseEvent = new zServersDisguiseEvent(this, entity, disguise, event.isCancelled());
        server.callEvent(disguiseEvent);
        event.setCancelled(disguiseEvent.isCancelled());
    }
}
