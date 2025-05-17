package me.z609.servers.server;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.*;

/**
 * This class does sanity checks to ensure that everything is set correctly. (no conflicts)
 */
public class BukkitLimiters implements Listener {
    private BukkitBridge bridge;
    private zServer server;
    private zServerManager manager;

    public BukkitLimiters(BukkitBridge bridge) {
        this.bridge = bridge;
        this.server = bridge.getServer();
        this.manager = server.getManager();
    }

    private boolean isInvalid(Player player) {
        return !bridge.isHere(player) && bridge.isHere(player.getLocation()) ||
                !isAssigned(player);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMove(PlayerMoveEvent event){
        Player player = event.getPlayer();
        if(!bridge.isHere(player) && bridge.isHere(event.getTo())){
            event.setTo(event.getFrom());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRespawnHighest(PlayerRespawnEvent event) {
        onRespawn(event);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onRespawnLowest(PlayerRespawnEvent event) {
        onRespawn(event);
    }

    private void onRespawn(PlayerRespawnEvent event){
        Player player = event.getPlayer();
        if (bridge.isHere(player) && !bridge.isHere(event.getRespawnLocation())) {
            event.setRespawnLocation(server.getSpawnpoint());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent event) {
        if (isInvalid(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent event) {
        event.setJoinMessage(null);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onQuit(PlayerQuitEvent event) {
        event.setQuitMessage(null);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onWorldChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        if (bridge.isHere(player) && !bridge.isHere(event.getPlayer().getLocation())) {
            player.teleport(server.getSpawnpoint());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        if (bridge.isHere(player) && !bridge.isHere(event.getTo())) {
            event.setTo(server.getSpawnpoint());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteract(PlayerInteractEvent event) {
        if (isInvalid(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player && isInvalid(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player && isInvalid(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDrop(PlayerDropItemEvent event) {
        if (isInvalid(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPickup(PlayerPickupItemEvent event) {
        if (isInvalid(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        if (isInvalid(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (isInvalid(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player player && isInvalid(player)) {
            event.setCancelled(true);
        }
    }

    public zServer getServer(Player player){
        return manager.getServer(player);
    }

    public boolean isSameServer(Player player, Location comparingTo){
        return manager.getServer(player).equals(manager.getServer(comparingTo));
    }

    public zServer getServer() {
        return server;
    }

    public boolean isAssigned(Player player) {
        return manager.getLocalServer(player) != null;
    }

}
