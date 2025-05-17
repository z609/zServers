package me.z609.servers.server;

import me.z609.servers.CallbackRun;
import me.z609.servers.host.Host;
import me.z609.servers.host.HostData;
import me.z609.servers.redis.RedisSubscriber;
import me.z609.servers.server.command.bundled.*;
import me.z609.servers.server.command.zServerCommand;
import me.z609.servers.server.world.zWorldData;
import me.z609.servers.zServers;
import net.minecraft.server.v1_12_R1.PlayerList;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_12_R1.CraftServer;
import org.bukkit.entity.Player;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class zServerManager {

    private zServers plugin;
    private final File globalModulesContainer;

    private Set<zServerTemplate> templates = new HashSet<>();
    private Set<zServerData> allServers = new HashSet<zServerData>();
    private Map<String, zServer> servers = new HashMap<>(); // servers on this host instance
    private Map<String, zWorldData> availableWorlds = new HashMap<>();
    private Map<String, zServerBundledCommand> bundledCommands = new HashMap<>();

    private long lastOrchestration;

    public zServerManager(zServers plugin){
        this.plugin = plugin;
        this.globalModulesContainer = new File(plugin.getServer().getWorldContainer(), "modules");

        bundledCommands.put("plugins", new zServerBundledCommand("plugins",
                zServerBundledCommandPlugins.class,
                zServerBundledCommandPlugins.Executor.class));
        bundledCommands.put("version", new zServerBundledCommand("version",
                zServerBundledCommandVersion.class,
                zServerBundledCommandVersion.Executor.class));
        bundledCommands.put("list", new zServerBundledCommand("list",
                zServerBundledCommandList.class,
                zServerBundledCommandList.Executor.class));
        bundledCommands.put("tp", new zServerBundledCommand("tp",
                zServerBundledCommandTeleport.class,
                zServerBundledCommandTeleport.Executor.class));
        bundledCommands.put("say", new zServerBundledCommand("say",
                zServerBundledCommandSay.class,
                zServerBundledCommandSay.Executor.class));
        plugin.getLogger().log(Level.INFO, "Loaded " + bundledCommands.size()  + " bundled commands for use: " +
                String.join(", ", bundledCommands.keySet()));

        updateTemplates();
        plugin.getLogger().log(Level.INFO, "Loaded " + templates.size() + " templates into memory.");
        updateAllServers();
        plugin.getLogger().log(Level.INFO, "Loaded " + allServers.size() + " servers across the network into memory.");
        plugin.getRedisBridge().getSubscriberManager().subscribe(new RedisSubscriber("servers:busy") {
            @Override
            public void onMessageReceived(String[] message) {
                if(message.length > 1){
                    String serverName = message[0];
                    zServerData data = getServerByName(serverName);
                    if(data != null){
                        data.busy = Boolean.parseBoolean(message[1]);
                    }
                }
            }
        });
        plugin.getLogger().log(Level.INFO, "Now listening for busy states.");
        updateAllWorlds();
        plugin.getLogger().log(Level.INFO, "Loaded " + availableWorlds.size() + " pre-defined worlds into memory.");
        for(zServerTemplate template : templates) {
            updateModulesContainer(template);
        }

        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                updateAllServers();
                if(System.currentTimeMillis() - lastOrchestration > 5000){
                    lastOrchestration = System.currentTimeMillis();
                    updateTemplates();
                    for(zServerTemplate template : templates)
                        orchestrateTemplate(template);
                }
            }
        }, 2*20, 2*20);
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                updateAllWorlds();
                for(zServerTemplate template : templates){
                    updateModulesContainer(template);
                }
            }
        }, 5*20, 5*20);
    }

    public void close(){
        Iterator<Map.Entry<String, zServer>> iterator = servers.entrySet().iterator();
        while(iterator.hasNext()){
            Map.Entry<String, zServer> entry= iterator.next();
            String name = entry.getKey();
            zServer server = entry.getValue();
            iterator.remove();
            server.shutdown();
        }
    }

    private void updateAllServers() {
        plugin.getRedisBridge().connect(new CallbackRun<Jedis>() {
            @Override
            public void callback(Jedis jedis) {
                // Get ALL data of ALL servers in the network - so we can refer to any of them at any time.
                Set<String> serverNames = jedis.smembers("servers");
                Set<zServerData> remove = new HashSet<>();
                int lost = 0;

                for(String name : serverNames){

                    zServerData data = loadServer(jedis, name); // This will load and update all in one.

                    // Host data for old data cleanup.
                    HostData host = data.getHost();

                    // Since the server is loaded in Redis, and we typically clean up,
                    // we'll go based off if the host is actually online or not.
                    boolean online = host != null && host.isOnline();

                    // If the host is online & local.
                    if(online && host.equals(plugin.getHost().getData())){
                        // It is supposedly on this host.
                        // So, we will check if it is online here locally.
                        // If it isn't, we know this is old, uncleaned data.

                        zServer server = getLocalServer(data);
                        if(server == null){
                            // The server is from a previous time this host was hosting this server, and wasn't cleaned up properly.
                            // "Orphaned" server. Since it's not being hosted right now, but still "on this host" we have to treat it
                            // like it is a remote server, and remove it from Redis only. After removal, it should restart by
                            // orchestration automatically if needed. (The reason these checks are so important is so the orchestrator
                            // will not count it as an active server - without removing this, the server will be added to the count
                            // and we won't have enough running servers. Additionally, players will be able to connect to a server
                            // that doesn't exist, and *that don't make no damn sense*.

                            // Orchestrator will restart this server if it's necessary.
                            online = false;
                        }
                    }
                    // There is no logic here for an else block, either, because if the host is offline and the server is marked
                    // online, then obviously the server is there when the host shouldn't be.

                    if(!online){
                        // Offline, we must remove it.
                        remove.add(data);
                    }
                }

                {
                    Iterator<zServer> iterator = zServerManager.this.servers.values().iterator();
                    while(iterator.hasNext()){
                        zServer server = iterator.next();
                        if(!serverNames.contains(server.getName())) {
                            iterator.remove();
                            continue;
                        }
                        server.updateServer(jedis);
                    }
                }
                {
                    Iterator<zServerData> iterator = zServerManager.this.allServers.iterator();
                    while(iterator.hasNext()){
                        zServerData server = iterator.next();
                        if(!serverNames.contains(server.getName())) {
                            iterator.remove();
                            lost++;
                        }
                    }
                }

                if(!remove.isEmpty()){
                    Pipeline pipeline = jedis.pipelined();
                    for (zServerData removing : remove) {
                        pipeline.del("server:" + removing.getName());
                        pipeline.srem("servers", removing.getName());
                        allServers.remove(removing);
                    }
                    pipeline.sync();
                }
                if(!remove.isEmpty() || lost > 0)
                    plugin.getLogger().info("[Removed Servers] Cleaned up " + (remove.size() + lost) + " servers.");
            }
        });
    }

    private void updateAllWorlds() {
        plugin.getRedisBridge().connect(new CallbackRun<Jedis>() {
            @Override
            public void callback(Jedis jedis) {
                // This will load all worlds from Redis into memory.
                Set<String> worldNames = jedis.smembers("worlds");
                for(String name : worldNames){
                    // Get the existing object and update, or create new one and initialize it.
                    zWorldData data = availableWorlds.getOrDefault(name, new zWorldData(zServerManager.this, name));
                    data.update(jedis);

                    // If it wasn't in there (fell back to default), this should put it in there only if it is absent.
                    availableWorlds.putIfAbsent(name, data);
                }

                Iterator<String> iterator = availableWorlds.keySet().iterator();
                while(iterator.hasNext())
                    if(!worldNames.contains(iterator.next()))
                        iterator.remove();
            }
        });
    }

    private zServerData loadServer(Jedis jedis, String name) {
        String key = "server:" + name;
        Map<String, String> data = jedis.hgetAll(key);
        if(data == null || data.isEmpty()) {
            return null;
        }

        String host = data.get("host");
        if(host == null) {
            return null;
        }

        zServerTemplate template = getTemplateByName(data.getOrDefault("template", ""));
        zServerData server = getServerByName(name);
        if(server == null){
            server = new zServerData(this, host, name, template);
            allServers.add(server);
        }

        server.update(data);
        return server;
    }

    public zServer getLocalServer(String name){
        return servers.get(name);
    }

    public zServer getLocalServer(zServerData data){
        return getLocalServer(data.getName());
    }

    public zServerData getServerByName(String name){
        for(zServerData server : allServers) {
            if(server.getName().equalsIgnoreCase(name)) {
                return server;
            }
        }
        return null;
    }

    public zServerData getServerByPlayer(String player){
        for(zServerData server : allServers) {
            if(server.isOnline(player)) {
                return server;
            }
        }
        return null;
    }

    public zServers getPlugin() {
        return plugin;
    }

    public Collection<zServerData> getServersByGroup(String group){
        List<zServerData> data = new ArrayList<>();
        for(zServerData server : allServers) {
            if(server.getGroup().equalsIgnoreCase(group)) {
                data.add(server);
            }
        }
        return Collections.unmodifiableCollection(data);
    }

    public Collection<zServer> getLocalServersByGroup(String group){
        List<zServer> servers = new ArrayList<>();
        for(zServer server : this.servers.values()) {
            if(server.getData().getGroup().equalsIgnoreCase(group)) {
                servers.add(server);
            }
        }
        return Collections.unmodifiableCollection(servers);
    }

    public int getLowestGroupServerNumber(String group){
        Set<Integer> usedNumbers = new HashSet<>();
        Collection<zServerData> servers = getServersByGroup(group);
        for (zServerData server : servers) {
            usedNumbers.add(server.getNumber());
        }
        int number = 1;
        while (usedNumbers.contains(number)) {
            number++;
        }
        return number;
    }

    public zServer startServer(zServerTemplate template){
        return startServer(template.getGroup() + "-" + getLowestGroupServerNumber(template.getGroup()), template);
    }

    public zServer startServer(String name){
        return startServer(name, null);
    }

    private zServer startServer(String name, zServerTemplate template){
        plugin.getLogger().log(Level.INFO, "Starting up server " + name + " (template=" + template.getName() +")");
        zServerData data = new zServerData(this, getCurrentHost().getName(), name, template);
        zServer server = new zServer(this, data);
        servers.put(data.getName(), server);
        allServers.add(data);
        plugin.getRedisBridge().connect(new CallbackRun<Jedis>() {
            @Override
            public void callback(Jedis jedis) {
                server.updateServer(jedis);
                server.startup();
                plugin.getRedisBridge().sendMessage("servers:added", server.getName());
            }
        });
        return server;
    }

    public void stopServer(zServer server){
        server.shutdown();
        servers.remove(server.getName());
        allServers.remove(server.getData());
    }

    public Host getCurrentHost(){
        return plugin.getHostManager().getHost();
    }

    public zServerTemplate getTemplateByName(String name){
        for(zServerTemplate template : templates) {
            if(template.getName().equalsIgnoreCase(name)) {
                return template;
            }
        }
        return null;
    }

    private void updateTemplates(){
        plugin.getRedisBridge().connect(new CallbackRun<Jedis>() {
            @Override
            public void callback(Jedis jedis) {
                Set<String> templateNames = jedis.smembers("templates");
                Set<zServerTemplate> templates = new HashSet<>();
                for(String name : templateNames){
                    zServerTemplate loaded = getTemplateByName(name);
                    Map<String, String> templateData = jedis.hgetAll("template:" + name);
                    if(loaded != null){
                        loaded.updateData(templateData);
                        continue;
                    }

                    loaded = new zServerTemplate(zServerManager.this, name, templateData);
                    zServerManager.this.templates.add(loaded);
                }

                Iterator<zServerTemplate> iterator = zServerManager.this.templates.iterator();
                while (iterator.hasNext()) {
                    zServerTemplate template = iterator.next();
                    if (!templateNames.contains(template.getName())) {
                        template.remove();
                        iterator.remove();
                    }
                }
            }
        });
    }

    private void orchestrateTemplate(zServerTemplate template){
        Collection<zServerData> allGroupServers = getServersByGroup(template.getGroup());

        int totalServers = allGroupServers.size();
        int requiredAvailable = template.getEmptyServers();

        int totalPlayers = allGroupServers.stream().mapToInt(zServerData::getPlayerCount).sum();
        int totalSlots = totalServers * template.getMaxPlayers();
        double utilization = totalSlots == 0 ? 0 : (double) totalPlayers / totalSlots;

        // === PHASE 1: SCALE DOWN EXTRA AVAILABLE SERVERS ===
        while (true) {
            List<zServer> localAvailable = getLocalServersByGroup(template.getGroup()).stream()
                    .filter(server -> !server.isBusy() && server.isEmpty())
                    .sorted(Comparator.comparingInt(zServer::getNumber).reversed())
                    .toList();

            if (localAvailable.size() <= requiredAvailable
                    || totalServers <= template.getMinServers() + template.getEmptyServers()) {
                break;
            }

            zServer toStop = localAvailable.get(0);
            stopServer(toStop);
            plugin.getLogger().info("Stopped server " + toStop.getName() + " as it was no longer needed.");
            totalServers--; // keep totalServers accurate in this context
        }

        // === PHASE 2: ENSURE MINIMUM SERVERS EXIST ===
        while (totalServers < template.getMinServers()) {
            if(!plugin.getCloud().isFull()){
                if (isSpaceHere()) {
                    startServer(template);
                    totalServers++;
                } else {
                    plugin.getLogger().log(Level.WARNING,
                            "Warning - This host has hit the server limit (" + plugin.getHost().getMaxServers() + "). " +
                                    "There is a request for " +
                                    (template.getMinServers() - totalServers) + " " + template.getGroup() + " server(s).");
                    plugin.getLogger().log(Level.WARNING,
                            "This host is being marked as full, and another host should start if resources are available.");
                    plugin.getCloud().full();
                    break;
                }
            } else {
                break;
            }
        }

        int localAvailable = (int) getLocalServersByGroup(template.getGroup()).stream()
                .filter(server -> !server.isBusy() && server.isEmpty())
                .count();

        // === PHASE 3: CREATE BUFFER IF TOO FEW AVAILABLE SERVERS ===
        if (localAvailable < requiredAvailable && isSpaceHere()) {
            if (startServer(template) != null) { // You might want this to return a boolean
                totalServers++;
                localAvailable++; // <-- update this!
                plugin.getLogger().info("Starting server to maintain available server buffer for group " + template.getGroup());
            }
        }

        // === PHASE 4: SCALE UP ON HIGH UTILIZATION ===
        if (utilization > template.getScaleUpBuffer() && isSpaceHere()) {
            startServer(template);
            plugin.getLogger().info("Scaling up " + template.getGroup() + " due to high utilization (" + (int)(utilization * 100) + "%)");
        }

        // === PHASE 5: SCALE DOWN IF UNDERUTILIZED ===
        if (utilization < 0.3 && totalServers > template.getMinServers() + 1 && localAvailable > requiredAvailable) {
            Optional<zServer> lastAvailable = getLocalServersByGroup(template.getGroup()).stream()
                    .filter(server -> !server.isBusy())
                    .sorted(Comparator.comparingInt(zServer::getNumber).reversed())
                    .findFirst();
            lastAvailable.ifPresent(server -> {
                stopServer(server);
                plugin.getLogger().info("Scaling down " + template.getGroup() + " due to low utilization.");
            });
        }

        if(servers.isEmpty()){
            plugin.getCloud().empty();
        } else if (!isSpaceHere() && !plugin.getCloud().isFull()) {
            plugin.getCloud().full();
        } else if (isSpaceHere() && plugin.getCloud().isFull()) {
            plugin.getCloud().notFull();
        }
    }

    public zServer getLocalServer(Player player){
        for(zServer server : servers.values()){
            if(server.isOnline(player)){
                return server;
            }
        }
        return null;
    }

    public boolean isSpaceHere(){
        return servers.size() < plugin.getHost().getMaxServers();
    }

    public Collection<zServerData> getServers() {
        return Collections.unmodifiableCollection(allServers);
    }

    public zServerData find(String name){
        for(zServerData server : allServers) {
            if(server.isOnline(name)) {
                return server;
            }
        }
        return null;
    }

    public int getMaxPlayerCount(){
        return servers.values().stream()
                .mapToInt(zServer::getMaxPlayers)
                .sum();
    }

    public void updateMaxPlayerCount(){
        setMaxPlayers(getMaxPlayerCount());
    }

    private void setMaxPlayers(int newMax) {
        CraftServer server = (CraftServer) plugin.getServer();
        PlayerList playerList = server.getHandle();

        try {
            Field maxPlayersField = PlayerList.class.getDeclaredField("maxPlayers");
            maxPlayersField.setAccessible(true);
            maxPlayersField.setInt(playerList, newMax);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to set max players: " + e.getMessage());
        }
    }

    public zServer getServer(Player player){
        for(zServer server : servers.values()) {
            if (server.isOnline(player)) {
                return server;
            }
        }
        return null;
    }

    public zServer getServer(Location location){
        return getServer(location.getWorld());
    }

    public zServer getServer(World world){
        for(zServer server : servers.values()) {
            if(server.isWorld(world)) {
                return server;
            }
        }
        return null;
    }

    public zWorldData getWorldData(String name){
        return availableWorlds.get(name);
    }

    public String generateWorldName(zServer server, String name) {
        return "zServers_" + server.getName() + "_" + name;
    }

    public File getGlobalModulesContainer(){
        return globalModulesContainer;
    }

    public File getModulesContainer(zServerTemplate template) {
        return new File(getGlobalModulesContainer(), template.getName());
    }

    private Map<zServerTemplate, Long> updateLock = new ConcurrentHashMap<zServerTemplate, Long>();

    private int updateModulesContainer(zServerTemplate template){
        if(updateLock.containsKey(template)) {
            return template.getModules().length;
        }
        updateLock.put(template, System.currentTimeMillis());

        File container = getModulesContainer(template);
        container.mkdirs();

        List<String> modules = Arrays.asList(template.getModules());
        int updated = 0;
        for(String moduleName : modules){
            File module = new File(container, moduleName);
            File global = getGlobalModule(moduleName);
            boolean copyOver = true;
            if (module.exists()) {
                copyOver = global.lastModified() > module.lastModified();
                plugin.getLogger().info("[Global Deployment] Determined there to be a newer version for " + module + " in the global modules directory.");
            }

            if(!copyOver || !global.exists()) {
                continue;
            }

            try {
                Files.copy(global.toPath(), module.toPath(), StandardCopyOption.REPLACE_EXISTING);
                updated++;
            } catch (IOException e) {
                plugin.getLogger().severe("[Global Deployment] Failed to copy global module into template global directory (" +
                        "[" + global.getAbsolutePath() + "] -> [" + module.getAbsolutePath() + "]: " + e.getMessage());
                e.printStackTrace();
            }
        }

        Set<File> remove = new HashSet<>();
        File[] files = container.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return !file.isDirectory() && file.getName().endsWith(".jar");
            }
        });
        for(File file : files){
            if(!modules.contains(file.getName())){
                remove.add(file);
            }
        }
        for(File file : remove){
            try {
                Files.deleteIfExists(file.toPath());
            } catch (IOException e) {
                plugin.getLogger().severe("[Global Deployment - " +template.getName() + "] Failed to delete unknown module in " +
                        "template global directory [" + file.getAbsolutePath() + "]: " + e.getMessage());
                e.printStackTrace();
            }
        }

        if(!updateLock.containsKey(template)) {
            return modules.size();
        }

        if(updated > 0){
            long updateStat = System.currentTimeMillis() - updateLock.remove(template);
            plugin.getLogger().info("[Global Deployment] Completed " + updated + " module update(s) " +
                    "in " + new DecimalFormat("#.###").format(updateStat / 1000) + "s");
        }

        return modules.size();
    }

    public File getGlobalModule(String moduleName){
        return new File(globalModulesContainer, moduleName);
    }

    public zServerBundledCommand getBundledCommand(String name) {
        return bundledCommands.get(name.toLowerCase());
    }

    public zServerBundledCommand getBundledCommand(Class<? extends zServerCommand> clazz){
        for(zServerBundledCommand command : bundledCommands.values()) {
            if(command.getCommand().equals(clazz)) {
                return command;
            }
        }
        return null;
    }

    public Collection<String> getBundledCommandNames(){
        return Collections.unmodifiableCollection(bundledCommands.keySet());
    }
}
