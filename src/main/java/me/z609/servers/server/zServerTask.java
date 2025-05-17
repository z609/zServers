package me.z609.servers.server;

import me.z609.servers.server.module.zModule;
import org.bukkit.scheduler.BukkitTask;

public class zServerTask {

    private final zModule module;
    private final zServer server;
    private final BukkitTask task;

    public zServerTask(zModule module, BukkitTask task) {
        this.module = module;
        this.server = module.getServer();
        this.task = task;
    }

    public zModule getModule() {
        return module;
    }

    public void cancel(){
        server.cancelTask(this);
    }

    void cancelBukkitTask(){
        task.cancel();
    }

    public zServer getServer() {
        return server;
    }
}
