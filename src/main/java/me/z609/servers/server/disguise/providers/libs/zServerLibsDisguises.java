package me.z609.servers.server.disguise.providers.libs;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.utilities.parser.DisguiseParseException;
import me.z609.servers.api.event.player.zPlayerJoinEvent;
import me.z609.servers.api.event.player.zPlayerQuitEvent;
import me.z609.servers.server.disguise.DisguiseProvider;
import me.z609.servers.server.disguise.zServerDisguise;
import me.z609.servers.server.disguise.zServerPlayerDisguise;
import me.z609.servers.server.zServer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
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
    public zServerDisguise<zServerLibsDisguises> getCustomDisguise(String disguiseName) {
        return LibsDisguise.asLibsDisguise(DisguiseAPI.getCustomDisguise(disguiseName));
    }

    @Override
    public void removeCustomDisguise(String disguiseName) {
        DisguiseAPI.removeCustomDisguise(disguiseName);
    }

    @Override
    public zServerDisguise<zServerLibsDisguises> constructDisguise(Entity entity, boolean doEquipment, boolean displayExtraAnimations) {
        return LibsDisguise.asLibsDisguise(DisguiseAPI.constructDisguise(entity, doEquipment, displayExtraAnimations));
    }

    @Override
    public zServerDisguise<zServerLibsDisguises> constructDisguise(Entity entity) {
        return LibsDisguise.asLibsDisguise(DisguiseAPI.constructDisguise(entity));
    }

    @Override
    public zServerDisguise<zServerLibsDisguises> buildDisguise(EntityType type) {
        return new LibsDisguise(type);
    }

    @Override
    public zServerPlayerDisguise buildPlayerDisguise(String name) {
        return new LibsPlayerDisguise(name);
    }

    @Override
    public void disguiseEntity(Entity entity, zServerDisguise<?> disguise) {
        DisguiseAPI.disguiseEntity(entity, ((LibsDisguise) disguise).getDisguise());
    }

    @Override
    public void disguiseEntity(CommandSender sender, Entity entity, zServerDisguise<?> disguise) {
        DisguiseAPI.disguiseEntity(sender, entity, ((LibsDisguise) disguise).getDisguise());
    }

    @Override
    public void disguiseIgnorePlayers(Entity entity, zServerDisguise<?> disguise, Collection playersToNotSeeDisguise) {
        DisguiseAPI.disguiseIgnorePlayers(entity, ((LibsDisguise) disguise).getDisguise(), playersToNotSeeDisguise);
    }

    @Override
    public void disguiseIgnorePlayers(Entity entity, zServerDisguise<?> disguise, Player... playersToNotSeeDisguise) {
        DisguiseAPI.disguiseIgnorePlayers(entity, ((LibsDisguise) disguise).getDisguise(), playersToNotSeeDisguise);
    }

    @Override
    public void disguiseIgnorePlayers(Entity entity, zServerDisguise<?> disguise, String... playersToNotSeeDisguise) {
        DisguiseAPI.disguiseIgnorePlayers(entity, ((LibsDisguise) disguise).getDisguise(), playersToNotSeeDisguise);
    }

    @Override
    public int disguiseNextEntity(zServerDisguise<?> disguise) {
        return DisguiseAPI.disguiseNextEntity(((LibsDisguise) disguise).getDisguise());
    }

    @Override
    public void disguiseToAll(Entity entity, zServerDisguise<?> disguise) {
        DisguiseAPI.disguiseToAll(entity, ((LibsDisguise) disguise).getDisguise());
    }

    @Override
    public void disguiseToPlayers(Entity entity, zServerDisguise<?> disguise, Collection playersToViewDisguise) {
        DisguiseAPI.disguiseToPlayers(entity, ((LibsDisguise) disguise).getDisguise(), playersToViewDisguise);
    }

    @Override
    public void disguiseToPlayers(Entity entity, zServerDisguise<?> disguise, Player... playersToViewDisguise) {
        DisguiseAPI.disguiseToPlayers(entity, ((LibsDisguise) disguise).getDisguise(), playersToViewDisguise);
    }

    @Override
    public void disguiseToPlayers(Entity entity, zServerDisguise<?> disguise, String... playersToViewDisguise) {
        DisguiseAPI.disguiseToPlayers(entity, ((LibsDisguise) disguise).getDisguise(), playersToViewDisguise);
    }

    @Override
    public zServerDisguise<zServerLibsDisguises> getDisguise(Entity disguised) {
        return LibsDisguise.asLibsDisguise(DisguiseAPI.getDisguise(disguised));
    }

    @Override
    public zServerDisguise<zServerLibsDisguises> getDisguise(Player observer, Entity disguised) {
        return LibsDisguise.asLibsDisguise(DisguiseAPI.getDisguise(observer, disguised));
    }

    @Override
    public zServerDisguise[] getDisguises(Entity disguised) {
        List<zServerDisguise<zServerLibsDisguises>> list = new ArrayList<>();
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
    public boolean isDisguiseInUse(zServerDisguise<?> disguise) {
        return DisguiseAPI.isDisguiseInUse(((LibsDisguise)disguise).getDisguise());
    }

    @Override
    public void undisguiseToAll(Entity entity) {
        DisguiseAPI.undisguiseToAll(entity);
    }

    @Override
    public void undisguiseToAll(CommandSender sender, Entity entity) {
        DisguiseAPI.undisguiseToAll(sender, entity);
    }
}
