package me.z609.servers.server;

import me.z609.servers.host.Host;
import me.z609.servers.host.HostData;
import me.z609.servers.zServers;

import java.util.*;

public class zServerData {

    private zServers plugin;
    private zServerManager manager;

    private Map<String, String> data = new HashMap<>();
    private final String host;
    private final String name;
    private final String group;
    private final int number;
    private zServerTemplate template; // For getting default data
    Map<UUID, String> players = new HashMap<>();
    boolean busy;
    boolean available;

    public zServerData(zServerManager manager, String host, String name, zServerTemplate template){
        this.manager = manager;
        this.plugin = manager.getPlugin();
        this.host = host;
        this.name = name;

        int number = -1;
        if(this.name.contains("-")){
            String[] args = this.name.split("-");
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < args.length - 1; i++) {
                builder.append(args[i]);
                if(i != args.length - 2) {
                    builder.append("-");
                }
            }
            group = builder.toString();
            try {
                number = Integer.parseInt(args[args.length - 1]);
            } catch (NumberFormatException ignored) {
            }
        }
        else{
            this.group = name;
        }
        this.number = number;
        this.template = template;
    }

    public String getHostName() {
        return host;
    }

    public String getName() {
        return name;
    }

    public String getGroup() {
        return group;
    }

    public int getNumber() {
        return number;
    }

    public zServerTemplate getTemplate() {
        return template;
    }

    public HostData getHost(){
        return plugin.getHostManager().getHost(host);
    }

    public boolean isEmpty(){
        return players.isEmpty();
    }

    public int getPlayerCount(){
        return players.size();
    }

    public Collection<String> getPlayerNames(){
        return Collections.unmodifiableCollection(players.values());
    }

    public boolean isOnline(String name){
        for(String player : players.values()) {
            if(player.equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    public boolean isBusy() {
        return busy;
    }

    public boolean isAvailable() {
        return available;
    }

    public void update(Map<String, String> data){
        this.data = data;
        this.players = Host.deEncapsulatePlayerNames(data.getOrDefault("players", ""));
        data.remove("players");
        this.busy = Boolean.parseBoolean(data.getOrDefault("busy", "false"));
        data.remove("busy");
        this.available = Boolean.parseBoolean(data.getOrDefault("available", "false"));
        data.remove("available");
    }

    public int getMaxPlayers(){
        return template != null ? template.getMaxPlayers() : 0;
    }

    public boolean isFallbackServer(){
        return plugin.getGlobalConfig().getFallbackGroup().equals(group);
    }

    public Map<String, String> getData() {
        return data;
    }

    public String getCustomData(String key){
        return data.get(key);
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof zServerData other)) {
            return false;
        }
        return other.getName().equals(getName()) && other.getHost().equals(getHost());
    }
}
