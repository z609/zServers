package me.z609.servers.server.world;

import me.z609.servers.server.zServerManager;
import me.z609.servers.zServers;
import org.bukkit.Location;
import org.bukkit.World;
import redis.clients.jedis.Jedis;

import java.util.*;

public class zWorldData {

    private zServerManager manager;
    private zServers plugin;
    private String name;

    private Map<String, String> data = new HashMap<String, String>();
    private String friendlyName;
    private String url;
    private boolean saved;
    private Map<String, List<Coordinates>> coordinates = new HashMap<String, List<Coordinates>>();
    private Coordinates spawnpoint;

    public zWorldData(zServerManager manager, String name){
        this.manager = manager;
        this.plugin = manager.getPlugin();
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void update(Jedis jedis){
        this.data = jedis.hgetAll("world:" + name);
        if(data == null || data.isEmpty()) {
            return;
        }

        this.friendlyName = data.containsKey("friendlyName") ? data.remove("friendlyName") : name;
        this.url = data.remove("url");
        this.saved = Boolean.parseBoolean(data.getOrDefault("saved", String.valueOf(this.url != null)));
        data.remove("saved");

        this.spawnpoint = Coordinates.parse(data.remove("spawnpoint"));
    }

    public boolean isSaved(){
        return url == null || this.saved;
    }

    public Set<String> keySet(){
        return Collections.unmodifiableSet(data.keySet());
    }

    public Collection<String> values(){
        return Collections.unmodifiableCollection(data.values());
    }

    public String getData(String key){
        return data.get(key);
    }

    public String getDataOrDefault(String key, String def){
        return data.getOrDefault(key, def);
    }

    public Map<String, List<Coordinates>> getCoordinates() {
        Map<String, List<Coordinates>> copy = new HashMap<>();
        for (Map.Entry<String, List<Coordinates>> entry : coordinates.entrySet()) {
            copy.put(entry.getKey(), Collections.unmodifiableList(entry.getValue()));
        }
        return Collections.unmodifiableMap(copy);
    }

    public List<Coordinates> getCoordinates(String key){
        return Collections.unmodifiableList(coordinates.getOrDefault(key, Collections.emptyList()));
    }

    public Coordinates getSpawnpoint(){
        return spawnpoint;
    }

    public Location getSpawnpointWithin(World world){
        return spawnpoint.asLocation(world);
    }

    public String getUrl() {
        return url;
    }

    public String getFriendlyName() {
        return friendlyName;
    }
}
