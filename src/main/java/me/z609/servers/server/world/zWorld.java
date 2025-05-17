package me.z609.servers.server.world;

import me.z609.servers.CallbackRun;
import me.z609.servers.server.zServer;
import me.z609.servers.server.zServerManager;
import me.z609.servers.zServers;
import org.bukkit.*;
import org.bukkit.entity.Player;

import java.io.*;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class zWorld {

    private static final ConcurrentHashMap<String, ReentrantLock> locks = new ConcurrentHashMap<>();

    private final zServer server;
    private zServerManager manager;
    private zServers plugin;
    private final String name;
    private final boolean mainWorld;
    private final String bukkitName;
    private final zWorldData data;
    private World world;
    private Location spawnpoint;
    private Set<ChunkSnapshot> spawnChunks = ConcurrentHashMap.newKeySet();

    public zWorld(zServer server, String name, zWorldData data) {
        if(name.trim().isEmpty()) {
            throw new IllegalArgumentException("World name cannot be empty/blank");
        }
        this.server = server;
        this.manager = server.getManager();
        this.plugin = server.getPlugin();

        this.name = name;
        this.mainWorld = name.equals("main");
        this.bukkitName = manager.generateWorldName(server, name);
        this.data = data;
    }

    private void downloadWorld(CallbackRun<zWorld> callback) {
        if(data == null) {
            server.logWarning("No world data provided for " + name + " - it WILL be generated");
            if(callback != null) {
                callback.callback(this); // skip download
            }
            return;
        }

        String url = data.getUrl();
        if (url == null || url.isEmpty()) {
            server.logWarning("No world URL provided for " + name);
            if(callback != null) {
                callback.callback(this); // skip download
            }
            return;
        }

        File downloadsDir = new File(plugin.getServer().getWorldContainer(), "downloads");
        downloadsDir.mkdirs();

        File zipFile = new File(downloadsDir, data.getName() + ".zip");
        File destinationDir = new File(plugin.getServer().getWorldContainer(), bukkitName);

        runWithLock(zipFile.getName(), () -> {
            if (!zipFile.exists() || !isZipValid(zipFile)) {
                if (zipFile.exists()) {
                    server.logWarning("Corrupt zip detected — deleting " + zipFile.getName());
                    zipFile.delete();
                }

                try (InputStream in = new URL(url).openStream();
                     FileOutputStream out = new FileOutputStream(zipFile)) {
                    byte[] buffer = new byte[8192];
                    int len;
                    while ((len = in.read(buffer)) != -1) {
                        out.write(buffer, 0, len);
                    }
                    plugin.getServer().getScheduler().runTask(plugin, () -> extractIfNeeded(zipFile, destinationDir, callback));
                } catch (Exception ex) {
                    server.logSevere("Download failed: " + ex.getMessage());
                    plugin.getServer().getScheduler().runTask(plugin, () -> callback.callback(this));
                }
            } else {
                server.logInfo("ZIP already cached and valid: " + zipFile.getPath());
                plugin.getServer().getScheduler().runTask(plugin, () -> extractIfNeeded(zipFile, destinationDir, callback));
            }
        });
    }

    public void loadWorld(CallbackRun<zWorld> callbackWhenWorldDownloaded,
                                      CallbackRun<zWorld> callbackWhenWorldFullyLoaded) {
        downloadWorld(downloaded -> {
            if(callbackWhenWorldDownloaded != null) {
                callbackWhenWorldDownloaded.callback(downloaded);
            }

            if (downloaded == null) {
                return; // fail early
            }
            loadDownloadedWorld(callbackWhenWorldFullyLoaded);
        });
    }

    private void loadDownloadedWorld(CallbackRun<zWorld> callback) {
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            WorldCreator creator = new WorldCreator(bukkitName);
            creator.generateStructures(false);
            creator.type(WorldType.FLAT);
            this.world = creator.createWorld();
            world.setAutoSave(isSaved());

            if (world == null) {
                server.logWarning("Failed to create world: " + bukkitName);
                if(callback != null) {
                    callback.callback(null);
                }
                return;
            }

            // Preload 8x8 chunks and invoke callback after
            spawnpoint = world.getSpawnLocation();
            preloadChunks(spawnpoint, 4, () -> {
                // Main thread context

                GameRule.applyDefaults(world);
                for(GameRule rule : GameRule.values()){
                    String value = data.getData("GameRule:" + rule.name());
                    if(value != null){
                        rule.set(world, value);
                    }
                    else{
                        value = server.getData().getTemplate().getData().get("GlobalGameRule:" + rule.name());
                        if(value != null){
                            rule.set(world, value);
                        }
                    }
                }

                if (data != null && data.getSpawnpoint() != null) {
                    spawnpoint = data.getSpawnpointWithin(world);
                    world.setSpawnLocation(spawnpoint);
                }

                server.logInfo("World " + name + " is fully loaded with spawn and chunks.");
                if(callback != null) {
                    callback.callback(this);
                }
            });
        });
    }

    public boolean updateSpawnLocation(){
        spawnpoint = world.getSpawnLocation();
        if (data != null && data.getSpawnpoint() != null) {
            spawnpoint = data.getSpawnpointWithin(world);
            world.setSpawnLocation(spawnpoint);
        }
        return spawnpoint != null;
    }

    private void preloadChunks(Location location, int radius, Runnable whenDone) {
        int centerX = location.getBlockX() >> 4;
        int centerZ = location.getBlockZ() >> 4;

        int total = (radius * 2 + 1) * (radius * 2 + 1);
        AtomicInteger loaded = new AtomicInteger(0);

        spawnChunks.clear(); // clear before starting.
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                int chunkX = centerX + dx;
                int chunkZ = centerZ + dz;

                world.getChunkAtAsync(chunkX, chunkZ, chunk -> {
                    spawnChunks.add(chunk.getChunkSnapshot());
                    if (loaded.incrementAndGet() >= total) {
                        // Once all chunks are loaded, call whenDone on the main thread
                        plugin.getServer().getScheduler().runTask(plugin, whenDone);
                    }
                });
            }
        }
    }

    private void extractIfNeeded(File zipFile, File destinationDir, CallbackRun<zWorld> callback) {
        if (destinationDir.exists() && isSaved()) {
            server.logInfo("World folder already exists, skipping extraction: " + destinationDir.getPath());
            callback.callback(this);
            return;
        }

        try {
            unzip(zipFile, destinationDir);
            server.logInfo("Extracted " + zipFile.getName() + " to " + destinationDir.getPath());
            callback.callback(this);
        } catch (IOException e) {
            server.logSevere("Extraction failed: " + e.getMessage());
            callback.callback(null);
        }
    }

    private boolean isZipValid(File file) {
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(file))) {
            while (zis.getNextEntry() != null) {
                // just iterate entries to validate
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private void unzip(File zipFile, File destDir) throws IOException {
        if (!destDir.exists()) {
            destDir.mkdirs();
        }

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                File newFile = new File(destDir, entry.getName());

                if (entry.isDirectory()) {
                    newFile.mkdirs();
                    continue;
                }

                new File(newFile.getParent()).mkdirs();
                try (FileOutputStream fos = new FileOutputStream(newFile)) {
                    byte[] buffer = new byte[4096];
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                }
            }
        }
    }

    public boolean unloadWorld(boolean save){
        return server.closeWorld(this, save);
    }

    public String getName() {
        return name;
    }

    public zWorldData getData() {
        return data;
    }

    public World getWorld() {
        return world;
    }

    public List<? extends Player> getPlayers(){
        return world != null ? world.getPlayers() : Collections.emptyList();
    }

    public Location getSpawnpoint(){
        return spawnpoint;
    }

    public String getBukkitName() {
        return bukkitName;
    }

    public boolean isMainWorld() {
        return mainWorld;
    }

    public boolean isSaved(){
        return data == null || data.isSaved();
    }

    public boolean isWorld(World world){
        return world.getName().equals(bukkitName);
    }

    public static void runWithLock(String key, Runnable action) {
        ReentrantLock lock = locks.computeIfAbsent(key, k -> new ReentrantLock());
        lock.lock();
        try {
            action.run();
        } finally {
            lock.unlock();
            if (!lock.hasQueuedThreads()) {
                locks.remove(key, lock);
            }
        }
    }

    public boolean isSpawnChunk(ChunkSnapshot chunkSnapshot) {
        return spawnChunks.contains(chunkSnapshot);
    }
}
