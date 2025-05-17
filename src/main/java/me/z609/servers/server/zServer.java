package me.z609.servers.server;

import com.sun.jdi.InvalidModuleException;
import me.z609.servers.CallbackRun;
import me.z609.servers.api.event.player.*;
import me.z609.servers.api.zEventExecutor;
import me.z609.servers.api.zEventHandler;
import me.z609.servers.api.zListener;
import me.z609.servers.api.zServersEvent;
import me.z609.servers.connect.ConnectionException;
import me.z609.servers.host.Host;
import me.z609.servers.server.command.bundled.zServerBundledCommand;
import me.z609.servers.server.command.zServerCommand;
import me.z609.servers.server.command.zServerCommandExecutor;
import me.z609.servers.server.module.IllegalModuleDescriptionException;
import me.z609.servers.server.module.zModule;
import me.z609.servers.server.module.zModuleDescription;
import me.z609.servers.server.world.zWorld;
import me.z609.servers.server.world.zWorldData;
import me.z609.servers.zServers;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import redis.clients.jedis.Jedis;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;

public class zServer implements Listener {

    private zServers plugin;
    private zServerManager manager;
    private zServerData data;
    private Map<UUID, Player> players = new HashMap<>();
    private boolean busy;
    private BukkitBridge bukkitBridge;
    private final File modulesContainer;
    private URLClassLoader moduleClassLoader;
    private zServerRedisBridge redisBridge;

    private Map<zModule, Set<Listener>> listeners = new HashMap<>();
    private final Map<Class<? extends zServersEvent>, List<zEventExecutor<? extends zServersEvent>>> customListeners = new HashMap<>();
    private final Map<zListener, Map<Class<? extends zServersEvent>, List<zEventExecutor<?>>>> listenerBindings = new HashMap<>();
    private Map<zModule, Set<zServerTask>> moduleTasks = new ConcurrentHashMap<>();
    private Map<zServerCommand, zServerCommandExecutor> commandMap = new ConcurrentHashMap<>();

    private Map<String, zWorld> worlds = new ConcurrentHashMap<>();
    private Set<String> bukkitWorldNames = ConcurrentHashMap.newKeySet();
    private Map<String, zModule> modules = new HashMap<>();
    private zWorld mainWorld;

    private final Set<UUID> invisiblePlayers = ConcurrentHashMap.newKeySet();

    public zServer(zServerManager manager, zServerData data) {
        this.manager = manager;
        this.plugin = manager.getPlugin();
        this.data = data;

        this.modulesContainer = new File(manager.getModulesContainer(data.getTemplate()), data.getName());
        modulesContainer.mkdirs();
    }

    public File getModulesContainer() {
        return modulesContainer;
    }

    public zServers getPlugin() {
        return plugin;
    }

    public zServerData getData() {
        return data;
    }

    boolean updateServer(Jedis jedis){
        final String key = "server:" + data.getName();

        final Map<String, String> fields = new HashMap<>();
        fields.put("name", data.getName());
        fields.put("template", data.getTemplate().getName());
        fields.put("host", data.getHostName());
        fields.put("heartbeat", String.valueOf(System.currentTimeMillis()));
        fields.put("players", Host.encapsulatePlayerNames(players.values()));
        fields.put("busy", String.valueOf(busy));

        jedis.hmset(key, fields);
        boolean added = jedis.sadd("servers", data.getName()) == 1;
        if(added){
            plugin.getRedisBridge().sendMessage("servers", "add", data.getName());
        }
        return added;
    }

    void startup() {
        final long started = System.currentTimeMillis();

        redisBridge = new zServerRedisBridge(this);
        logInfo("Redis Bridge with zServers has been initialized.");

        int loaded = 0;
        for(String bundledName : data.getTemplate().getBundledCommands()){
            zServerBundledCommand bundled = manager.getBundledCommand(bundledName);
            if(bundled != null){
                try {
                    zServerCommand command = bundled.getCommand().getDeclaredConstructor().newInstance();
                    zServerCommandExecutor executor = bundled.getExecutor().getDeclaredConstructor().newInstance();
                    registerCommand(command, executor);
                    loaded++;
                } catch (Exception e) {
                    logWarning("Could not load BundledCommand-" + bundledName + ":");
                    e.printStackTrace();
                }
            }
            else {
                logWarning("Could not load BundledCommand-" + bundledName + " as it is not available.");
            }
        }
        logInfo("Loaded " + loaded + " BundledCommands.");

        updateModules();
        startModuleManager();
        List<zModule> sortedModules = topologicalSortModules(modules);
        for(zModule module : sortedModules) {
            loadModule(module);
        }

        CallbackRun<zWorld> callback = new CallbackRun<zWorld>() {
            @Override
            public void callback(zWorld zWorld) {
                if(zServer.this.worlds.size() == zServer.this.data.getTemplate().getWorlds().length + 1){
                    manager.updateMaxPlayerCount();

                    List<zModule> sortedModules = topologicalSortModules(modules);
                    for(zModule module : sortedModules)
                        enableModule(module);
                    bukkitBridge = new BukkitBridge(zServer.this).register();

                    logInfo("Successfully started server! (" +
                            new DecimalFormat("#.###").format((System.currentTimeMillis() - started) / 1000L) + "s)");
                    started();
                }
            }
        };

        openWorld(data.getTemplate().getMainWorld(), null, callback, true);
        for(String worldName : data.getTemplate().getWorlds()) {
            openWorld(worldName, null, callback);
        }



    }

    private void started(){
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        for(zModule module : modules.values()) {
            module.start();
        }
    }

    private List<zModule> topologicalSortModules(Map<String, zModule> modules) {
        List<zModule> sorted = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        Set<String> visiting = new HashSet<>();

        for (zModule module : modules.values()) {
            visitModule(module, modules, sorted, visited, visiting);
        }

        return sorted;
    }

    private void visitModule(zModule module,
                             Map<String, zModule> moduleMap,
                             List<zModule> sorted,
                             Set<String> visited,
                             Set<String> visiting) {
        String name = module.getDescription().getName();

        if (visited.contains(name)) {
            return;
        }

        if (visiting.contains(name)) {
            throw new IllegalStateException("Circular dependency detected involving " + name);
        }

        visiting.add(name);
        for (String depName : module.getDescription().getDepends()) {
            zModule dep = moduleMap.get(depName);
            if (dep == null) {
                throw new IllegalStateException("Missing required dependency '" + depName + "' for module '" + name + "'");
            }
            visitModule(dep, moduleMap, sorted, visited, visiting);
        }
        visiting.remove(name);
        visited.add(name);
        sorted.add(module);
    }

    private void loadModule(zModule module){
        zModuleDescription description = module.getDescription();
        logInfo("Loading zModule " + description.getName() + " v" + description.getVersion() + "...");
        module.load();
        logInfo("Loaded zModule " + description.getName() + "!");
    }

    private void enableModule(zModule module){
        zModuleDescription description = module.getDescription();
        logInfo("Enabling zModule " + description.getName() + " v" + description.getVersion() + "...");
        module.enable();
        logInfo("Enabled zModule " + description.getName() + "!");
    }

    private void disableModule(zModule module){
        zModuleDescription description = module.getDescription();
        logInfo("Disabling zModule " + description.getName() + " v" + description.getVersion() + "...");
        unregisterAllListeners(module);
        module.disable();
        logInfo("Disabled zModule " + description.getName() + "!");
    }

    private void cancelAllTasks(zModule module){
        Set<zServerTask> tasks = moduleTasks.get(module);
        if(tasks == null || tasks.isEmpty()) {
            return;
        }
        Iterator<zServerTask> iterator = tasks.iterator();
        while(iterator.hasNext()){
            zServerTask task = iterator.next();
            task.cancel();
            iterator.remove();
        }
        if(tasks.isEmpty()) {
            moduleTasks.remove(module);
        }
    }

    private void updateModules(){
        File template = manager.getModulesContainer(data.getTemplate());

        // Get list of all JARs in the template directory
        File[] files = template.listFiles(new JarFilter());

        // Loop through each of JAR in the template directory and (re)place them if they are newer
        for(File global : files){
            File module = new File(modulesContainer, global.getName()); // Matching names
            if(!module.exists() || global.lastModified() > module.lastModified()){
                try {
                    Files.copy(global.toPath(), module.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    logInfo("[Deployment] Updated " + module.getName() + " from template directory " +
                            "[" + global.getAbsolutePath() + "]");
                } catch (IOException e) {
                    logSevere("[Deployment] Failed to update " + module.getName() + " from template directory " +
                            "[" + global.getAbsolutePath() + "]: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    private void startModuleManager(){
        File[] jars = modulesContainer.listFiles(new JarFilter());
        if(jars == null || jars.length == 0) {
            return;
        }
        URL[] jarUrls = Arrays.stream(jars).map(file -> {
            try {
                return file.toURI().toURL();
            } catch (MalformedURLException ex) {
                logSevere("[Module Loader] Could not load module \"" + file.getName() + "\": " + ex.getMessage());
                return null;
            }
        }).filter(Objects::nonNull).toArray(URL[]::new);

        moduleClassLoader = new URLClassLoader(jarUrls, getClass().getClassLoader());
        Set<zModule> modules = instantiateModules(jars);
        for(zModule module : modules){
            zModuleDescription description = module.getDescription();
            String name = description.getName();
            this.modules.put(name, module);
        }
    }

    private Set<zModule> instantiateModules(File[] files) {
        Set<zModule> modules = new HashSet<>();
        if (files == null) {
            return modules;
        }

        for (File file : files) {
            try {
                zModuleDescription description = retrieveModuleDescription(file);
                zModule module = instantiateModule(description);
                modules.add(module);
            } catch (Exception ex) {
                logSevere("[Module Loader] Could not load module \"" + file.getName() + "\": " + ex.getMessage());
                ex.printStackTrace();
            }
        }

        return modules;
    }

    private zModule instantiateModule(zModuleDescription description)
            throws IllegalModuleDescriptionException,
            ClassNotFoundException,
            NoSuchMethodException,
            InvocationTargetException,
            InstantiationException,
            IllegalAccessException {
        Class<?> clazz = moduleClassLoader.loadClass(description.getMainClass());
        if(!zModule.class.isAssignableFrom(clazz)) {
            throw new IllegalModuleDescriptionException("Invalid module: Invalid `main` class path in module.yml - " +
                    "not assignable from " + zModule.class.getName() + " (Is it up to date?)");
        }

        zModule module = (zModule) clazz.getDeclaredConstructor().newInstance();
        module.hook(this, description);
        return module;
    }

    void shutdown(){
        // Kick all players out to hub using a central method
        for(Player player : getOnlinePlayers()){
            zServerData fallback = plugin.getConnectionManager().getBestFallback();
            if(fallback == null){
                quit(player, null); // Call this and then kick fully - continue iteration.
                player.kickPlayer(ChatColor.RED + "There are no servers to connect to at this time.");
                continue;
            }
            plugin.getConnectionManager().transferServer(player, fallback);
        }

        for (zWorld world : new ArrayList<>(this.worlds.values())) {
            closeWorld(world, world.isSaved(), world.isMainWorld());
        }

        List<zModule> sortedModules = topologicalSortModules(modules);
        Collections.reverse(sortedModules);
        for(zModule module : sortedModules) {
            disableModule(module);
        }
        modules.clear(); // All modules should be disabled, and subsequently unloaded.

        bukkitBridge.unregister();
        HandlerList.unregisterAll(this);

        removeServer();
        manager.updateMaxPlayerCount();
        log(Level.INFO, "Server has been shut down.");
    }

    private void removeServer(){
        final String key = "server:" + data.getName();

        plugin.getRedisBridge().connect(new CallbackRun<Jedis>(){
            @Override
            public void callback(Jedis jedis) {
                jedis.del("server:" + data.getName());
                jedis.srem("servers", data.getName());
            }
        });
    }

    public String getName(){
        return data.getName();
    }

    public boolean isBusy() {
        return busy;
    }

    public void setBusy(final boolean busy) {
        this.busy = busy;
        // Since this can be delayed when auto-assigning servers, update this ahead of the update tick.
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                plugin.getRedisBridge().connect(new CallbackRun<Jedis>() {
                    @Override
                    public void callback(Jedis jedis) {
                        jedis.hset("server:" + zServer.this.data.getName(), "busy", String.valueOf(busy));
                        plugin.getRedisBridge().getSubscriberManager()
                                .sendMessage("servers:busy", zServer.this.data.getName(), String.valueOf(busy));
                    }
                });
            }
        });
    }

    public boolean isEmpty(){
        return players.isEmpty();
    }

    public int getNumber(){
        return data.getNumber();
    }

    public int getPlayerCount(){
        return players.size();
    }

    public int getVisiblePlayerCount(){
        return players.size();
    }

    public boolean isOnline(Player player) {
        return players.containsKey(player.getUniqueId());
    }

    /**
     * Called when the player needs to have their health restored, inventory cleared, etc. This is
     * especially important at join (considering they can be coming from another zServer...)
     */
    public void resetPlayer(Player player, boolean clearInventory){
        player.closeInventory();
        player.setMaxHealth(20.0D);
        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);
        for(PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
        if(clearInventory){
            player.getInventory().setArmorContents(null);
            player.getInventory().setItemInOffHand(null);
            player.getInventory().clear();
            player.updateInventory();
        }
        player.setFallDistance(0);
        player.setExp(0);
        player.setLevel(0);
        player.setDisplayName(player.getName());
        player.resetPlayerTime();
        player.setKiller(null);
        player.setArrowsStuck(0);
        player.setWalkSpeed(0.2F);
        player.setFlySpeed(0.1F);
        player.setGliding(false);
        player.setGlowing(false);
        player.setLastDamageCause(null);
        player.setScoreboard(plugin.getServer().getScoreboardManager().getMainScoreboard());
        player.setFireTicks(0);
        player.setGameMode(data.getTemplate().getGameMode());
        player.setAllowFlight(player.getGameMode() == GameMode.CREATIVE);
        player.setFlying(false);
        player.setCompassTarget(player.getWorld().getSpawnLocation());
    }

    public void join(Player player,
                     boolean localJoin,
                     boolean newSession,
                     zPlayerPreTransferEvent preTransferEvent,
                     zServerConnectHandler handler){
        if(preTransferEvent != null && preTransferEvent.isCancelled()){
            handler.onCancelled(preTransferEvent.getCancelReason());
            return;
        }
        if(localJoin){
            player.teleport(mainWorld.getSpawnpoint());
            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 1000000, 255));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 1000000, 255));
            player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 1000000, -50));
            player.setWalkSpeed(0.0F);
            player.setFlySpeed(0.0F);
        }
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            zAsyncPlayerPreloginEvent preloginEvent = new zAsyncPlayerPreloginEvent(this, player, newSession);
            callEvent(preloginEvent); // run async-safe listeners only
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (!preloginEvent.isCancelled()) {
                    try {
                        join(player, preTransferEvent, preloginEvent, newSession);
                        handler.onSuccess();
                    } catch (Exception e) {
                        handler.onFailure(e);
                    }
                }
                else{
                    handler.onCancelled(preloginEvent.getCancelReason());
                }
            });
        });
    }

    private boolean join(Player player,
                         zPlayerPreTransferEvent preTransferEvent,
                         zAsyncPlayerPreloginEvent preloginEvent,
                         boolean newSession) throws ConnectionException {
        zPlayerLoginEvent login = new zPlayerLoginEvent(this, preloginEvent);
        callEvent(login);
        if(login.isCancelled()) {
            throw new ConnectionException(login.getCancelReason());
        }

        zServer previous = manager.getLocalServer(player);
        if(previous != null){
            previous.quit(player, preTransferEvent);
        }
        players.put(player.getUniqueId(), player);

        updateVisiblePlayersFor(player);
        updateVisibilityFor(player, true);

        resetPlayer(player, true);
        Location spawnpoint = mainWorld.getSpawnpoint();
        zPlayerJoinEvent join = new zPlayerJoinEvent(this, player, spawnpoint, player.getName() + " joined.");
        callEvent(join);
        if(join.getSpawnpoint() != null){
            spawnpoint = join.getSpawnpoint();
        }
        else if (spawnpoint != null){
            logWarning("The PlayerJoinEvent spawnpoint was set to null by a module listener. Please investigate." +
                    "Falling back to Template/World Spawn Location.");
        }
        else if (mainWorld.getSpawnpoint() != null){
            spawnpoint = mainWorld.getSpawnpoint();
        }
        else {
            // TODO Add logic for persistent servers (keeping player where they were last logged in)
        }

        if (spawnpoint == null) {
            throw new ConnectionException("The server cannot find a place to put the player.\n" +
                    "The spawnpoint for " + mainWorld.getName() + " is null - is the world even loaded?");
        }

        resetPlayer(player, false);
        player.teleport(spawnpoint);
        if(join.getMessage() != null) {
            broadcastMessage(join.getMessage());
        }

        //player.sendMessage(ChatColor.GOLD + "You have been connected to " + data.getName() + " on " + data.getHostName() + ".");
        return true;
    }

    private void updateVisibilityFor(Player player, boolean join) {
        if (join && !isOnline(player)) {
            return; // Only run join logic if player is online
        }

        boolean isInvisible = isInvisible(player);

        for (Player other : getOnlinePlayers()) {
            if (other.equals(player)) {
                continue;
            }

            // Player joining logic
            if (join) {
                // Other -> Player
                if (isInvisible(other)) {
                    player.hidePlayer(plugin, other);
                } else {
                    player.showPlayer(plugin, other);
                }

                // Player -> Other
                if (isInvisible) {
                    other.hidePlayer(plugin, player);
                } else {
                    other.showPlayer(plugin, player);
                }
            }

            // Player quitting logic
            else {
                other.hidePlayer(plugin, player);
                player.hidePlayer(plugin, other); // Optional: ensure full bidirectional hide
            }
        }
    }


    public void broadcastMessage(String... messages){
        for(String s : messages){
            for(Player player : players.values()) {
                player.sendMessage(s);
            }
        }
        sendConsoleMessage(messages);
    }

    public void callEvent(zServersEvent event){
        List<zEventExecutor<? extends zServersEvent>> executors = customListeners.get(event.getClass());
        if (executors != null) {
            for (zEventExecutor<? extends zServersEvent> executor : new ArrayList<>(executors)) {
                // Unsafe cast is okay since we validated class earlier
                zEventExecutor<zServersEvent> safeExecutor = (zEventExecutor<zServersEvent>) executor;
                safeExecutor.execute(event);
            }
        }
    }

    public void registerListener(zModule module, Listener listener) {
        Set<Listener> listeners = this.listeners.computeIfAbsent(module, m -> new HashSet<>());
        listeners.add(listener);
        plugin.getServer().getPluginManager().registerEvents(listener, plugin);

        if (!(listener instanceof zListener)) {
            return;
        }

        Map<Class<? extends zServersEvent>, List<zEventExecutor<?>>> bindings = new HashMap<>();

        Class<?> clazz = listener.getClass();
        while (clazz != null && zListener.class.isAssignableFrom(clazz)) {
            for (Method method : clazz.getDeclaredMethods()) {
                if (!method.isAnnotationPresent(zEventHandler.class)) {
                    continue;
                }
                if (method.getParameterCount() != 1) {
                    continue;
                }

                Class<?> param = method.getParameterTypes()[0];
                if (!zServersEvent.class.isAssignableFrom(param)) {
                    continue;
                }

                method.setAccessible(true);
                Class<? extends zServersEvent> eventClass = (Class<? extends zServersEvent>) param;
                
                zEventExecutor<? extends zServersEvent> wrapper = event -> {
                    try {
                        method.invoke(listener, event);
                    } catch (Exception e) {
                        e.printStackTrace(); // Or log
                    }
                };
                registerCustomListenerUnchecked(eventClass, wrapper);
                bindings.computeIfAbsent(eventClass, k -> new ArrayList<>()).add(wrapper);
            }
            clazz = clazz.getSuperclass(); // Move up the hierarchy
        }

        listenerBindings.put((zListener) listener, bindings);
    }

    private <T extends zServersEvent> void registerCustomListenerUnchecked(Class<T> clazz, zEventExecutor<?> executor) {
        registerCustomListener(clazz, executor);
    }

    private <T extends zServersEvent> void registerCustomListener(Class<T> eventClass, zEventExecutor<?> executor) {
        customListeners
                .computeIfAbsent(eventClass, k -> new ArrayList<>())
                .add(executor);
    }

    public void unregisterListener(zModule module, Listener listener) {
        Set<Listener> listeners = this.listeners.get(module);
        if (listeners != null) {
            Map<Class<? extends zServersEvent>, List<zEventExecutor<?>>> bindings = listenerBindings.remove(listener);
            if (bindings == null) {
                return;
            }

            for (Map.Entry<Class<? extends zServersEvent>, List<zEventExecutor<?>>> entry : bindings.entrySet()) {
                List<zEventExecutor<?>> registered = customListeners.get(entry.getKey());
                if (registered != null) {
                    registered.removeAll(entry.getValue());
                    if (registered.isEmpty()) {
                        customListeners.remove(entry.getKey());
                    }
                }
            }

            listeners.remove(listener);
            HandlerList.unregisterAll(listener);
            if (listeners.isEmpty()) {
                this.listeners.remove(module); // Optional: clean up empty sets
            }
        }
    }

    public Set<Listener> getListeners(zModule module){
        return Collections.unmodifiableSet(listeners.get(module));
    }

    public void unregisterAllListeners(zModule module) {
        Set<Listener> listeners = this.listeners.remove(module);
        if (listeners != null) {
            for (Listener listener : listeners) {
                HandlerList.unregisterAll(listener);
            }
        }
    }

    public void quit(Player player, zPlayerPreTransferEvent preTransferEvent){
        zPlayerQuitEvent quit = new zPlayerQuitEvent(this, player, preTransferEvent, player.getName() + " quit.");
        callEvent(quit);
        players.remove(player.getUniqueId());
        updateVisiblePlayersFor(player);
        updateVisibilityFor(player, false);
        if(quit.getMessage() != null) {
            broadcastMessage(quit.getMessage());
        }
    }

    public Collection<Player> getOnlinePlayers() {
        return Collections.unmodifiableCollection(players.values());
    }

    public Collection<String> getPlayerNames(){
        return players.values().stream().map(Player::getDisplayName).toList();
    }

    public zServerManager getManager() {
        return manager;
    }

    public int getMaxPlayers(){
        return data.getMaxPlayers();
    }

    public zWorld openWorld(String name){
        return openWorld(name, null, null);
    }

    public zWorld openWorld(String name,
                            CallbackRun<zWorld> callbackWhenWorldDownloaded){
        return openWorld(name, callbackWhenWorldDownloaded, null);
    }

    public zWorld openWorld(String name,
                            CallbackRun<zWorld> callbackWhenWorldDownloaded,
                            CallbackRun<zWorld> callbackWhenWorldFullyLoaded){
        return openWorld(name,
                callbackWhenWorldDownloaded,
                callbackWhenWorldFullyLoaded,
                false);
    }

    private zWorld openWorld(String name,
                             boolean main){
        return openWorld(name, null, null, main);
    }

    private zWorld openWorld(String name,
                             CallbackRun<zWorld> callbackWhenWorldDownloaded,
                             boolean main){
        return openWorld(name, callbackWhenWorldDownloaded, null, main);
    }

    private zWorld openWorld(String name,
                             CallbackRun<zWorld> callbackWhenWorldDownloaded,
                             CallbackRun<zWorld> callbackWhenWorldFullyLoaded,
                             boolean main){
        if(name == null && !main){
            throw new IllegalArgumentException("Cannot load world with no name, unless it is the main one.");
        }
        zWorldData data = manager.getWorldData(name); // May return null if not explicitly specified.
        if(main) {
            name = "main"; // Main world will always be called "main"
        }
        zWorld world = new zWorld(this, name, data);
        worlds.put(name, world); // Key will always match world name
        bukkitWorldNames.add(world.getBukkitName());
        world.loadWorld(callbackWhenWorldDownloaded, callbackWhenWorldFullyLoaded);
        if(main) {
            mainWorld = world;
        }
        return world;
    }

    public boolean closeWorld(zWorld world, boolean save){
        return closeWorld(world, save, false);
    }

    private boolean closeWorld(zWorld world, boolean save, boolean main){
        if(!worlds.containsValue(world)){
            return false;
        }

        if(world.getName().equals("main") && !main){
            throw new IllegalArgumentException("World to be unloaded \"" + world.getName() + "\" is the main world, " +
                    "and cannot be unloaded unless the server is shutting down.");
        }

        // If this is the main world, then the players would have already been kicked back to fallback.
        if(!main){
            for(Player player : world.getPlayers()){
                // Teleport all players to the main world of their server before closing.
                player.teleport(mainWorld.getSpawnpoint());
            }
        }
        worlds.remove(world.getName()); // World name and key in the map should always be the same.
        bukkitWorldNames.remove(world.getBukkitName());

        plugin.getServer().unloadWorld(world.getWorld(), save);

        if(!save){
            // Cleanup and delete (to prevent weird corruption if the namespace is used again)
            File container = new File(plugin.getServer().getWorldContainer(), world.getBukkitName());
            if(container.exists() && container.isDirectory()){
                try {
                    Files.walk(container.toPath())
                            .sorted(Comparator.reverseOrder()) // delete files before directories
                            .forEach(p -> {
                                try {
                                    Files.delete(p);
                                } catch (IOException e) {
                                    plugin.getLogger().warning("Failed to delete: " + p + " - " + e.getMessage());
                                }
                            });
                } catch (IOException e) {
                    logWarning("Warning - The world " + world.getName() + " found at " + container.getAbsolutePath() +
                            " could not be deleted: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        return true;
    }

    public void log(Level level, String... messages){
        for (String message : messages) {
            plugin.getServer().getLogger().log(level, "[Server Context - " + data.getName() + "] " + message);
        }
    }

    public void logWarning(String... messages){
        log(Level.WARNING, messages);
    }

    public void logInfo(String... messages){
        log(Level.INFO, messages);
    }

    public void logSevere(String... messages){
        log(Level.SEVERE, messages);
    }

    public boolean isFallbackServer(){
        return data.isFallbackServer();
    }

    public boolean isHere(Location location){
        return isWorld(location.getWorld());
    }

    public boolean isHere(Player player){
        return isHere(player.getLocation()) && isOnline(player);
    }

    public boolean isHere(World world){
        return isWorld(world);
    }

    public boolean isWorld(World world){
        return bukkitWorldNames.contains(world.getName());
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof zServer other)) {
            return false;
        }
        return other.getName().equals(getName()) && other.data.getHost().equals(data.getHost());
    }

    public Location getSpawnpoint() {
        return mainWorld.getSpawnpoint();
    }

    public void sendConsoleMessage(String... messages){
        for(String message : messages) {
            plugin.getServer().getConsoleSender().sendMessage("[Chat Context - " + data.getName() + "] " + message);
        }
    }

    public static zModuleDescription retrieveModuleDescription(File jarFile)
            throws IOException, IllegalModuleDescriptionException {
        try (JarFile jar = new JarFile(jarFile)) {
            JarEntry entry = jar.getJarEntry("module.yml");
            if (entry == null) {
                throw new InvalidModuleException("module.yml not found");
            }

            try (InputStream in = jar.getInputStream(entry)) {
                Configuration config = YamlConfiguration.loadConfiguration(new InputStreamReader(in));
                return new zModuleDescription(config);
            }
        }
    }

    public Server getBukkit(){
        return plugin.getServer();
    }

    public zServerRedisBridge getRedisBridge() {
        return redisBridge;
    }

    public zModule getModule(String name){
        if(modules.containsKey(name)) {
            return modules.get(name);
        }
        for(String moduleName : modules.keySet()) {
            if(moduleName.equalsIgnoreCase(name)) {
                return modules.get(moduleName);
            }
        }
        return null;
    }

    public <T> T getModuleForUse(String name){
        zModule module = modules.get(name);
        if(module == null) {
            throw new IllegalArgumentException("Module is not available: " + name);
        }
        return (T) module;
    }

    public boolean isInvisible(Player player) {
        return invisiblePlayers.contains(player.getUniqueId());
    }

    public void setInvisible(Player player, boolean invisible) {
        if (invisible) {
            invisiblePlayers.add(player.getUniqueId());
        } else {
            invisiblePlayers.remove(player.getUniqueId());
        }

        // Immediately update visibility for all others
        updateVisiblePlayersFor(player);
    }

    public void hidePlayer(Player hiding) {
        for (Player to : Bukkit.getOnlinePlayers()) {
            if (to.equals(hiding)) {
                continue;
            }
            if (isOnline(to)) {
                to.hidePlayer(plugin, hiding);
            }
        }
    }

    public void showPlayer(Player showing) {
        if (isInvisible(showing)) {
            return;
        }
        if (!isOnline(showing)) {
            return; // Only show players *from* this zServer
        }

        for (Player to : Bukkit.getOnlinePlayers()) {
            if (to.equals(showing)) {
                continue;
            }
            if (!isOnline(to)) {
                continue; // Only show *to* players in this zServer
            }

            to.showPlayer(plugin, showing);
        }
    }

    public void hidePlayer(Player hiding, Player to) {
        if (!hiding.equals(to) && isOnline(to)) {
            to.hidePlayer(plugin, hiding);
        }
    }


    public void showPlayer(Player showing, Player to) {
        if (!isOnline(showing)) {
            return; // Enforce source is part of this zServer
        }
        if (!isOnline(to)) {
            return;      // Enforce destination is part of this zServer
        }
        if (isInvisible(showing)) {
            return;
        }

        to.showPlayer(plugin, showing);
    }

    public void updateVisiblePlayersFor(Player player) {
        if (!isOnline(player)) {
            return;
        }

        boolean selfInvisible = isInvisible(player);

        for (Player other : Bukkit.getOnlinePlayers()) {
            if (other.equals(player)) {
                continue;
            }

            boolean otherOnline = isOnline(other);
            boolean otherInvisible = isInvisible(other);

            // Self's view of others
            if (otherOnline && !otherInvisible) {
                player.showPlayer(plugin, other);
            } else {
                player.hidePlayer(plugin, other);
            }

            // Other's view of self
            if (otherOnline && !selfInvisible) {
                other.showPlayer(plugin, player);
            } else {
                other.hidePlayer(plugin, player);
            }
        }
    }

    public zServerTask runTaskAsynchronously(zModule module, Runnable runnable){
        return new zServerTask(module, plugin.getServer().getScheduler().runTaskAsynchronously(plugin, runnable));
    }

    public zServerTask runTask(zModule module, Runnable runnable){
        return new zServerTask(module, plugin.getServer().getScheduler().runTask(plugin, runnable));
    }

    public zServerTask runTaskLater(zModule module, Runnable runnable, long delay){
        zServerTask task = new zServerTask(module, plugin.getServer().getScheduler().runTaskLater(plugin, runnable, delay));
        moduleTasks.computeIfAbsent(module, m -> ConcurrentHashMap.newKeySet()).add(task);
        return task;
    }

    public zServerTask runTaskTimer(zModule module, Runnable runnable, long delay, long frequency){
        zServerTask task = new zServerTask(module, plugin.getServer().getScheduler().runTaskTimer(plugin, runnable, delay, frequency));
        moduleTasks.computeIfAbsent(module, m -> ConcurrentHashMap.newKeySet()).add(task);
        return task;
    }

    public zServerTask runTaskLaterAsynchronously(zModule module, Runnable runnable, long delay){
        zServerTask task =new zServerTask(module, plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, runnable, delay));
        moduleTasks.computeIfAbsent(module, m -> ConcurrentHashMap.newKeySet()).add(task);
        return task;
    }

    public zServerTask runTaskTimerAsynchronously(zModule module, Runnable runnable, long delay, long frequency){
        zServerTask task =new zServerTask(module, plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, runnable, delay, frequency));
        moduleTasks.computeIfAbsent(module, m -> ConcurrentHashMap.newKeySet()).add(task);
        return task;
    }

    public void cancelTask(zServerTask task){
        task.cancelBukkitTask();
        if(!moduleTasks.containsKey(task.getModule())) {
            return;
        }
        moduleTasks.get(task.getModule()).remove(task);
    }

    public boolean isCommand(zServerCommand command){
        return commandMap.containsKey(command);
    }

    public void registerCommand(zServerCommand command, zServerCommandExecutor executor) {
        if(isCommand(command)) {
            return;
        }
        commandMap.put(command, executor);
    }

    public zServerCommand getCommand(String label){
        final String lower = label.toLowerCase();
        for(zServerCommand command : commandMap.keySet()){
            if(command.getBaseCommand().equalsIgnoreCase(label) ||
                    Arrays.stream(command.getAliases())
                            .map(String::toLowerCase)
                            .anyMatch(alias -> alias.equals(lower))){
                return command;
            }
        }
        return null;
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event){
        Player player = event.getPlayer();

        // Check if the player is in this server (per-zServer logic)
        if(!isOnline(player)) {
            return;
        }

        String message = event.getMessage();
        // Unlikely that the COMMAND wouldn't start with /, but I do this check because I'm going to be sub-stringing anyway.
        if(!message.startsWith("/")) {
            return;
        }

        message = message.substring(1);
        event.setCancelled(processCommand(player, message));
    }

    public boolean processCommand(CommandSender sender, String message) {
        if (message == null || message.trim().isEmpty()) {
            return false;
        }

        String[] split = message.trim().split("\\s+"); // splits on one or more spaces
        String label = split[0];
        String[] args = Arrays.copyOfRange(split, 1, split.length);

        // Check if the command is blocked
        if (Arrays.stream(data.getTemplate().getBlockedCommands())
                .map(String::toLowerCase)
                .anyMatch(cmd -> cmd.equalsIgnoreCase(label))) {

            // The command is blocked - whether the command originates from zServers or Bukkit, doesn't matter - BLOCKED
            // Here we will send the Bukkit/Spigot unknown command message

            sender.sendMessage(plugin.getServer().spigot().getSpigotConfig().getString("messages.unknown-command"));

            if (sender instanceof ConsoleCommandSender) {
                sender.sendMessage(ChatColor.YELLOW + "Your use of /" + label + " was not permitted " +
                        "because it was explicitly blocked by the server template.");
            }
            return true; // Return true to cancel the default Bukkit logic
        }

        zServerCommand command = getCommand(label);
        if (command == null) {
            // The Bukkit default message should be sent when returning false.
            return false;
        }

        if (command.getBasePermissionNode() != null && !sender.hasPermission(command.getBasePermissionNode())) {
            sender.sendMessage(getNoPermissionMessage());
            return true;
        }

        commandMap.get(command).execute(this, command, sender, label, args);
        return true;
    }

    public String getNoPermissionMessage(){
        return plugin.getConfig().getString("message.no-permission");
    }

    public Collection<zModule> getModules(){
        return Collections.unmodifiableCollection(modules.values());
    }

    public Player getPlayerExact(String name) {
        Player player = plugin.getServer().getPlayerExact(name);
        return player != null && isOnline(player) ? player : null;
    }

    public zWorld getMainWorld() {
        return mainWorld;
    }
}
