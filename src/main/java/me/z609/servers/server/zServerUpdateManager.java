package me.z609.servers.server;

import me.z609.servers.server.module.zModule;
import me.z609.servers.server.module.zModuleDescription;
import me.z609.servers.server.module.zModuleRuntime;
import me.z609.servers.zServers;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class zServerUpdateManager {
    private final zServers plugin;
    private final zServer server;
    private final BukkitTask checkForUpdates, applyUpdates;

    private List<zModule> restarting = new ArrayList<>();
    private Map<zModule, File> hotSwap = new HashMap<>();

    public zServerUpdateManager(zServer server) {
        this.server = server;
        this.plugin = server.getPlugin();

        this.checkForUpdates = server.getPlugin().getServer().getScheduler().runTaskTimerAsynchronously(server.getPlugin(), new Runnable() {
            @Override
            public void run() {
                checkForUpdates();
            }
        }, 10*20, 10*20);

        this.applyUpdates = server.getPlugin().getServer().getScheduler().runTaskTimer(server.getPlugin(), new Runnable() {
            @Override
            public void run() {
                applyUpdates();
            }
        }, 10*20, 20);
    }

    public void applyUpdates(){
        if(server.isBusy())
            return;
        if(!restarting.isEmpty()){
            server.logWarning("[UpdateApply] There are modules that require a full server restart. We are restarting the server.");
            server.broadcastMessage(ChatColor.GREEN + "This server requires a restart for updates to apply. We apologize for the inconvenience.");
            server.shutdown();
            return;
        }
        // Hot swap doesn't matter if there are big updates that require a full restart.
        if(hotSwap.isEmpty())
            return;
        server.logInfo("[UpdateApply] Applying queued hot-swap updates...");
        attemptHotSwaps();
    }

    public void close(){
        this.checkForUpdates.cancel();
        this.applyUpdates.cancel();
    }

    public boolean checkForUpdates() {
        File[] jarFiles = server.getManager()
                .getModulesContainer(server.getData().getTemplate())
                .listFiles(new JarFilter());

        if (jarFiles == null || jarFiles.length == 0)
            return false;

        Map<String, File> jarByName = new HashMap<>();
        for (File jar : jarFiles) {
            try {
                zModuleDescription desc = zServer.retrieveModuleDescription(jar);
                jarByName.put(desc.getName(), jar);
            } catch (Exception ignored) {
                // Ignore invalid jars
            }
        }

        for (zModule module : server.getModules()) {
            final zModuleRuntime runtime = server.getModuleRuntime(module);
            boolean hotSwap = module.getDescription().isHotSwappable();

            // check if these modules are already queued for update.
            if(hotSwap && this.hotSwap.containsKey(module))
                continue;
            if(!hotSwap && this.restarting.contains(module))
                continue;
            String name = module.getDescription().getName();
            File updatedJar = jarByName.get(name);

            if (updatedJar == null || !updatedJar.exists())
                continue;

            try {
                String currentHash = runtime.getMd5();
                String actualHash = zServer.md5(updatedJar);

                if (currentHash != null && !actualHash.equals(currentHash)) {
                    if (hotSwap) {
                        this.hotSwap.put(module, updatedJar);
                        server.logInfo("[Deployment] Module " + name + " has a hot-swap update. The server will attempt to reload this module when the server isn't busy.");
                    } else {
                        this.restarting.add(module);
                        server.logInfo("[Deployment] Module " + name + " has update. Server will restart when the server isn't busy.");
                    }
                }
            } catch (Exception e) {
                server.logWarning("[UpdateCheck] Failed to check MD5 for " + name + ": " + e.getMessage());
            }
        }

        return !restarting.isEmpty() || !hotSwap.isEmpty();
    }

    public boolean isHotSwapUpdate(){
        return restarting.isEmpty() && !hotSwap.isEmpty();
    }

    public void attemptHotSwaps(){
        if(server.isBusy())
            return;
        if(hotSwap.isEmpty())
            return;
        server.setBusy(true, true); //  first arg - busy, second arg - lock busy (package-private method)

        server.broadcastMessage(ChatColor.GREEN + "Attempting to update " + hotSwap.size() + " modules - please be patient.");
        int updated = 0;
        for (Map.Entry<zModule, File> entry : hotSwap.entrySet()) {
            zModule module = entry.getKey();
            File templateJar = entry.getValue();

            String name = module.getDescription().getName();
            zModuleRuntime runtime = server.getModuleRuntime(module);
            File targetJar = runtime.getJar();

            server.logInfo("[Deployment - HotSwap] Attempting to hot-swap " + name + "...");

            // Disable the module programmatically (this calls all onDisable methods so that modules can safely perform their cleanup)
            server.disableModule(module, true); // package-private

            // Attempt to unload
            try {
                runtime.unload(); // this resets the class loader
            } catch (IOException e) {
                server.logSevere("[Deployment - HotSwap] Could not unload module " + name);
                e.printStackTrace();
                continue;
            }
            server.logInfo("[Deployment - HotSwap] Fully unloaded " + name);

            try {
                // Windows **LOVES** to fail if a file is in use, so this is just to be sure the file doesn't cause conflicts
                // If this fails, it means Windows is being stupid, and the server will likely have to reboot anyway.
                Files.delete(targetJar.toPath());
                Files.copy(templateJar.toPath(), targetJar.toPath(), StandardCopyOption.REPLACE_EXISTING);
                server.logInfo("[Deployment - HotSwap] Copied updated JAR file: [" + templateJar.getAbsolutePath() + "] -> [" + targetJar.getAbsolutePath() + "]");
            } catch (IOException e) {
                server.logSevere("[Deployment - HotSwap] Could not copy the updated JAR file: [" + templateJar.getAbsolutePath() + "] -> [" + targetJar.getAbsolutePath() + "]");
                e.printStackTrace();
                continue;
            }

            // Attempt to reload now that the file has been copied.
            try {
                module = runtime.load(); // this will instantiate a new class loader
                if(module.getDescription() != null){
                    // The name might have changed, which means we will have to update everything.
                    if(!name.equals(module.getDescription().getName())){
                        server.logWarning("[Deployment - HotSwap] The module " + name + " has a new name: " + module.getDescription().getName() + " - we are going to continue loading it anyway.");
                        server.modules.remove(name);
                        server.moduleRuntimes.remove(name);
                        name = module.getDescription().getName();
                        server.moduleRuntimes.put(name, runtime); // update old reference with new one.
                    }
                }
                server.modules.put(name, module); // update with new module
                server.logInfo("[Deployment - HotSwap] Successfully loaded " + name);
            } catch (Exception e) {
                server.logSevere("[Deployment - HotSwap] Could not reload module " + name);
                e.printStackTrace();
                continue;
            }
            server.logInfo("[Deployment - HotSwap] Update completed for " + name + ". Attempting to load the module now.");

            try {
                server.loadModule(module);
                server.enableModule(module);
            } catch (Exception e) {
                plugin.getLogger().severe("[Module Loader] Failed to load module " + name + ": " + e.getMessage());
                e.printStackTrace();
                continue;
            }

            updated++;
        }

        if(!hotSwap.isEmpty() && updated < hotSwap.size()){
            hotSwap.clear();

            // Could not complete all updates - maybe it really should just be rebooted after all.
            server.logSevere("Could not hot-swap all modules. Attempting a reboot anyway.");
            server.broadcastMessage(ChatColor.RED + "The server could not update. We are rebooting the server now...");
            server.shutdown();
            return;
        }

        hotSwap.clear();

        server.setBusy(false, true);
    }

    public zServers getPlugin() {
        return plugin;
    }

    public zServer getServer() {
        return server;
    }
}
