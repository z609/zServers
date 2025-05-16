package me.z609.servers.server.module;

import me.z609.servers.server.zServer;

public abstract class zModule {

    private zServer server;
    private zModuleDescription description;

    private boolean loaded, enabled = false;

    public void hook(zServer server, zModuleDescription description){
        this.server = server;
        this.description = description;
    }

    public void load(){
        if(loaded || enabled)
            throw new IllegalStateException("zModule already loaded!");
        loaded = true;
        onLoad();
    }

    public void enable(){
        if(!loaded)
            throw new IllegalStateException("zModule not loaded!");
        if(enabled)
            throw new IllegalStateException("zModule already enabled!");
        enabled = true;
        onEnable();
    }

    public void disable(){
        if(!loaded || !enabled)
            throw new IllegalStateException("zModule not enabled!");
        enabled = false;
        onDisable();
    }

    public void onLoad(){

    }

    public void onEnable(){

    }

    public void onDisable(){

    }

    public void start(){
        if(!loaded)
            throw new IllegalStateException("zModule not loaded!");
        if(!enabled)
            throw new IllegalStateException("zModule not enabled!");
        onStarted();
    }

    public void onStarted(){

    }

    public zModuleDescription getDescription() {
        return description;
    }

    public zServer getServer() {
        return server;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
