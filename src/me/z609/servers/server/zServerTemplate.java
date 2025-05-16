package me.z609.servers.server;

import org.bukkit.GameMode;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class zServerTemplate {

    private zServerManager manager;
    private final String name;
    private int maxPlayers;
    private GameMode gameMode;
    private boolean pvp;
    private String[] games;
    private String[] worlds;
    private String[] availableMaps;
    private String mainWorld;
    private String[] modules = new String[0];
    private String[] bundledCommands = new String[0];
    private String[] blockedCommands = new String[0];
    private Map<String, String> data = new HashMap<>();

    private String group;
    private int minServers;
    private int emptyServers;
    private double scaleUpBuffer = 0.8; // % of used capacity before scaling
    private int scaleCooldown = 300000; // Prevent flapping

    public zServerTemplate(zServerManager manager, String name, Map<String, String> data){
        this.manager = manager;
        this.name = name;
        updateData(data);
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public GameMode getGameMode() {
        return gameMode;
    }

    public boolean isPvp() {
        return pvp;
    }

    public String getName() {
        return name;
    }

    public String[] getGames() {
        return games;
    }

    public int getEmptyServers() {
        return emptyServers;
    }

    public String[] getWorlds() {
        return worlds;
    }

    public String getMainWorld() {
        return mainWorld;
    }

    public void updateData(Map<String, String> data){
        if(data == null){
            manager.getPlugin().getLogger().log(Level.WARNING, "Warning - There is no template \"" + name + "\" in Redis. " +
                    "This doesn't matter too much, unless you're actually trying to use this server...");
            return;
        }

        if(!data.containsKey("group") || data.get("group") == null){
            manager.getPlugin().getLogger().log(Level.WARNING, "Warning - Template " + name + " does not have a group name " +
                    "specified. This means that servers will not spin up until you specify one manually.");
            return;
        }
        this.data = data;

        group = data.get("group");

        int maxPlayers;
        try {
            maxPlayers = Integer.parseInt(data.getOrDefault("maxPlayers", "20"));
            if(maxPlayers != this.maxPlayers)
                this.maxPlayers = maxPlayers;
        } catch (NumberFormatException ignored) {
        }

        String mainWorld = data.getOrDefault("mainWorld", "");
        if(!mainWorld.equals(this.mainWorld))
            this.mainWorld = mainWorld;

        String[] worlds = Arrays.stream(data.getOrDefault("worlds", "")
                        .split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);
        if(!Arrays.equals(worlds, this.worlds))
            this.worlds = worlds;

        String[] availableMaps = Arrays.stream(data.getOrDefault("availableMaps", "")
                        .split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);;;
        if(!Arrays.equals(availableMaps, this.availableMaps))
            this.availableMaps = availableMaps;

        String[] modules = Arrays.stream(data.getOrDefault("modules", "")
                        .split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);
        if(!Arrays.equals(modules, this.modules))
            this.modules = modules;

        String[] bundledCommands = Arrays.stream(data.getOrDefault("bundledCommands", "")
                        .split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);
        if(bundledCommands.length == 0)
            bundledCommands = manager.getBundledCommandNames().toArray(new String[0]);
        if(!Arrays.equals(bundledCommands, this.bundledCommands))
            this.bundledCommands = bundledCommands;

        String[] blockedCommands = Arrays.stream(data.getOrDefault("blockedCommands", "")
                        .split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);
        if(!Arrays.equals(blockedCommands, this.blockedCommands))
            this.blockedCommands = blockedCommands;

        GameMode gameMode;
        try {
            gameMode = GameMode.valueOf(data.getOrDefault("gameMode", "SURVIVAL"));
            if(gameMode != this.gameMode)
                this.gameMode = gameMode;
        } catch (IllegalArgumentException ignored) {
        }

        boolean pvp = Boolean.parseBoolean(data.getOrDefault("pvp", "true"));
        if(pvp != this.pvp)
            this.pvp = pvp;

        String[] games = data.get("games") != null ? data.get("games").trim().split(",") : new String[0];
        if(!Arrays.equals(games, this.games))
            this.games = games;

        int minServers;
        try {
            minServers = Integer.parseInt(data.getOrDefault("minServers", "0"));
            if(minServers != this.minServers)
                this.minServers = minServers;
        } catch (NumberFormatException ignored) {
        }

        int emptyServers;
        try {
            emptyServers = Integer.parseInt(data.getOrDefault("emptyServers", "0"));
            if(emptyServers != this.emptyServers)
                this.emptyServers = emptyServers;
        } catch (NumberFormatException ignored) {
        }

        double scaleUpBuffer;
        try {
            scaleUpBuffer = Double.parseDouble(data.getOrDefault("scaleUpBuffer", "999"));
            if(scaleUpBuffer != this.scaleUpBuffer)
                this.scaleUpBuffer = scaleUpBuffer;
        } catch (NumberFormatException ignored) {
        }

        int scaleCooldown;
        try {
            scaleCooldown = Integer.parseInt(data.getOrDefault("scaleCooldown", "300000"));
            if(scaleCooldown != this.scaleCooldown)
                this.scaleCooldown = scaleCooldown;
        } catch (NumberFormatException ignored) {
        }
    }

    public int getMinServers() {
        return minServers;
    }

    public String getGroup() {
        return group;
    }

    public void remove() {
        // Template deleted, meaning shut down all servers regardless of state.
        for(zServer server : manager.getLocalServersByGroup(group)){
            server.shutdown();
        }
    }

    public double getScaleUpBuffer() {
        return scaleUpBuffer;
    }

    public int getScaleCooldown() {
        return scaleCooldown;
    }

    public String[] getAvailableMaps() {
        return availableMaps;
    }

    public String[] getModules() {
        return modules;
    }

    public String[] getBundledCommands() {
        return bundledCommands;
    }

    public String[] getBlockedCommands() {
        return blockedCommands;
    }

    public Map<String, String> getData() {
        return Collections.unmodifiableMap(data);
    }
}
