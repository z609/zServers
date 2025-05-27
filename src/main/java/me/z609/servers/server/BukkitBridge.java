package me.z609.servers.server;

import me.z609.servers.api.event.entity.zEntityDamageByEntityEvent;
import me.z609.servers.api.event.entity.zEntityDamageEvent;
import me.z609.servers.api.event.entity.zEntityDeathEvent;
import me.z609.servers.api.event.player.*;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.*;

import java.util.HashSet;
import java.util.Set;

/**
 * This superclass is what not only separates zServers from each other, but also bridges certain
 * parts of Bukkit's API so that zServer can push down its own APIs.
 */
public class BukkitBridge implements Listener {
    private zServer server;
    private zServerManager manager;
    private BukkitLimiters limiters;

    public BukkitBridge(zServer server) {
        this.server = server;
        this.manager = server.getManager();
        this.limiters = new BukkitLimiters(this);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onRespawn(PlayerRespawnEvent event){
        Player player = event.getPlayer();
        if(!isHere(player)) {
            return;
        }
        zPlayerRespawnEvent zevent = new zPlayerRespawnEvent(
                server, player, event.getRespawnLocation()
        );
        server.callEvent(zevent);
        event.setRespawnLocation(zevent.getRespawnLocation());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onChat(AsyncPlayerChatEvent event){
        Player player = event.getPlayer();
        if(!isHere(player)) {
            return;
        }

        Set<Player> recipients = new HashSet<>(server.getOnlinePlayers());
        String message = event.getMessage();
        zPlayerChatEvent zevent = new zPlayerChatEvent(server, player, event.isCancelled(), message, recipients);
        server.callEvent(zevent);
        if(!zevent.isCancelled()){
            if(zevent.getFormat() != null){
                String output = zevent.getOutput();
                if(output == null)
                    output = String.format(zevent.getFormat(), player.getDisplayName(), message);
                for(Player recipient : zevent.getRecipients()){
                    recipient.sendMessage(output);
                }
                server.sendConsoleMessage(output);
            }
        }

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDeath(PlayerDeathEvent event){
        Player player = event.getEntity();
        if(!isHere(player)) {
            return;
        }

        zPlayerDeathEvent zevent = new zPlayerDeathEvent(server,
                player, event.getDrops(),
                event.getDroppedExp(),
                event.getReviveHealth(),
                event.shouldPlayDeathSound(),
                event.getDeathSound(),
                event.getDeathSoundCategory(),
                event.getDeathSoundVolume(),
                event.getDeathSoundPitch(),
                event.getNewExp(),
                event.getDeathMessage(),
                event.getNewLevel(),
                event.getNewTotalExp(),
                event.getKeepLevel(),
                event.getKeepInventory());
        server.callEvent(zevent);
        event.setDeathMessage(null);
        event.setKeepInventory(zevent.isKeepInventory());
        event.setKeepLevel(zevent.isKeepLevel());
        event.setNewExp(zevent.getNewExp());
        event.setNewLevel(zevent.getNewLevel());
        event.setNewTotalExp(zevent.getNewTotalExp());
        event.setDroppedExp(zevent.getDropExp());
        if(zevent.getDeathMessage() != null) {
            server.broadcastMessage(zevent.getDeathMessage());
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBreak(BlockBreakEvent event){
        Player player = event.getPlayer();
        if(!isHere(player)) {
            return;
        }

        zBlockBreakEvent zevent =
                new zBlockBreakEvent(server, player, event.isCancelled(), event.isDropItems());
        server.callEvent(zevent);
        event.setCancelled(zevent.isCancelled());
        event.setDropItems(zevent.isDropItems());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlace(BlockPlaceEvent event){
        Player player = event.getPlayer();
        if(!isHere(player)) {
            return;
        }

        zBlockPlaceEvent zevent =
                new zBlockPlaceEvent(server,
                        player,
                        event.isCancelled(),
                        event.canBuild(),
                        event.getHand(),
                        event.getItemInHand(),
                        event.getBlockAgainst(),
                        event.getBlockReplacedState());
        server.callEvent(zevent);
        event.setCancelled(zevent.isCancelled());
        event.setBuild(zevent.isCanBuild());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!isHere(event.getEntity().getLocation())) {
            return;
        }

        zEntityDamageEvent zEvent;
        if(event instanceof EntityDamageByEntityEvent damageByEntityEvent){
            zEvent = new zEntityDamageByEntityEvent(
                    server,
                    event.getEntity(),
                    damageByEntityEvent.getDamager(),
                    event.getCause(),
                    event.getDamage(),
                    event.isCancelled()
            );
        }
        else{
            zEvent = new zEntityDamageEvent(
                    server,
                    event.getEntity(),
                    event.getCause(),
                    event.getDamage(),
                    event.isCancelled()
            );
        }

        server.callEvent(zEvent);

        event.setDamage(zEvent.getDamage());
        event.setCancelled(zEvent.isCancelled());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!isHere(event.getPlayer())) {
            return;
        }

        zPlayerMoveEvent zEvent = new zPlayerMoveEvent(
                server,
                event.getPlayer(),
                event.getFrom(),
                event.getTo(),
                event.isCancelled()
        );

        server.callEvent(zEvent);
        event.setFrom(zEvent.getFrom());
        event.setTo(zEvent.getTo());
        event.setCancelled(zEvent.isCancelled());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!isHere(event.getPlayer())) {
            return;
        }

        zPlayerInteractEvent zEvent = new zPlayerInteractEvent(
                server,
                event.getPlayer(),
                event.getAction(),
                event.getItem(),
                event.getClickedBlock(),
                event.getBlockFace(),
                event.useInteractedBlock(),
                event.useItemInHand(),
                event.getHand()
        );

        server.callEvent(zEvent);
        event.setUseInteractedBlock(zEvent.getUseClickedBlock());
        event.setUseItemInHand(zEvent.getUseItemInHand());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (!isHere(event.getWhoClicked().getLocation())) {
            return;
        }
        
        zInventoryClickEvent zEvent = new zInventoryClickEvent(
                server,
                player,
                event.getAction(),
                event.getClick(),
                event.getInventory(),
                event.getClickedInventory(),
                event.getSlotType(),
                event.getSlot(),
                event.getRawSlot(),
                event.getCurrentItem(),
                event.getHotbarButton(),
                event.isCancelled()
        );

        server.callEvent(zEvent);
        event.setCurrentItem(zEvent.getCurrentItem());
        event.setCancelled(zEvent.isCancelled());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClose(InventoryCloseEvent event) {
        if(!(event.getPlayer() instanceof Player player)) {
            return;
        }
        if (!isHere(player)) {
            return;
        }

        zInventoryCloseEvent zEvent = new zInventoryCloseEvent(server,
                player,
                event.getView(),
                event.getReason());
        server.callEvent(zEvent);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDeath(EntityDeathEvent event) {
        if (!isHere(event.getEntity().getLocation())) {
            return;
        }

        zEntityDeathEvent zEvent = new zEntityDeathEvent(server,
                event.getEntity(),
                event.getDrops(),
                event.getDroppedExp(),
                event.isCancelled());
        server.callEvent(zEvent);
        event.setCancelled(zEvent.isCancelled());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (!isHere(event.getPlayer())) {
            return;
        }

        zPlayerDropItemEvent zEvent = new zPlayerDropItemEvent(server, event.getPlayer(), event.getItemDrop());
        server.callEvent(zEvent);
        event.setCancelled(zEvent.isCancelled());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        if (!isHere(event.getPlayer())) {
            return;
        }

        zPlayerPickupItemEvent zEvent = new zPlayerPickupItemEvent(
                server,
                event.getPlayer(),
                event.getItem(),
                event.getRemaining(),
                event.getFlyAtPlayer(),
                event.isCancelled()
        );

        server.callEvent(zEvent);
        event.setCancelled(zEvent.isCancelled());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onExp(PlayerExpChangeEvent event){
        if(!isHere(event.getPlayer()))
            return;

        zPlayerExpChangeEvent zEvent = new zPlayerExpChangeEvent(
                server,
                event.getPlayer(),
                event.getAmount()
        );

        server.callEvent(zEvent);
        event.setAmount(zEvent.getAmount());
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event){
        if(!(event.getEntity() instanceof Player player)) {
            return;
        }
        if(!isHere(event.getEntity().getLocation())) {
            return;
        }
        
        zFoodLevelChangeEvent zEvent = new zFoodLevelChangeEvent(
                server,
                player,
                event.getFoodLevel(),
                event.isCancelled()
        );
        server.callEvent(zEvent);
        event.setFoodLevel(zEvent.getFoodLevel());
        event.setCancelled(zEvent.isCancelled());
    }

    BukkitBridge register(){
        server.getPlugin().getServer().getPluginManager().registerEvents(this, server.getPlugin());
        server.getPlugin().getServer().getPluginManager().registerEvents(limiters, server.getPlugin());
        return this;
    }

    void unregister(){
        HandlerList.unregisterAll(this);
        HandlerList.unregisterAll(limiters);
    }

    public boolean isHere(Location location){
        return server.isHere(location);
    }

    public boolean isHere(Player player){
        return server.isHere(player);
    }

    public zServer getServer() {
        return server;
    }

    public void setManager(zServerManager manager) {
        this.manager = manager;
    }
}
