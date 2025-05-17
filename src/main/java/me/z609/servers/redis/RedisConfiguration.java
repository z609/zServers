package me.z609.servers.redis;

import org.bukkit.configuration.Configuration;

public class RedisConfiguration {

    private final String host;
    private final int port;
    private final String username;
    private final String password;

    public RedisConfiguration(Configuration config){
        this.host = config.getString("redis.host", "localhost");
        this.port = config.getInt("redis.port", 6379);
        this.password = config.getString("redis.password", null);

        String username = config.getString("redis.username", null);
        if (username == null && password != null) {
            username = "default";
        }
        this.username = username;
    }

    public RedisConfiguration(String host, int port, String username, String password) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getPassword() {
        return password;
    }
}
