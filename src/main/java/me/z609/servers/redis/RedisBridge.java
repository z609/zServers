package me.z609.servers.redis;

import me.z609.servers.Callback;
import me.z609.servers.CallbackRun;
import me.z609.servers.zServers;
import org.bukkit.configuration.Configuration;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisConnectionException;

public class RedisBridge {

    private zServers plugin;
    private RedisConfiguration config;

    private JedisPool pool;

    private RedisSubscriberManager subscriberManager;

    public RedisBridge(zServers plugin, Configuration config) throws JedisConnectionException {
        this(plugin, new RedisConfiguration(config));
    }

    public RedisBridge(zServers plugin, RedisConfiguration config) throws JedisConnectionException {
        this.plugin = plugin;
        this.config = config;

        this.pool = new JedisPool(config.getHost(), config.getPort(), config.getUsername(), config.getPassword());
        try (Jedis jedis = pool.getResource()) {
            jedis.ping();
        } catch (JedisConnectionException ex) {
            plugin.getServer().getLogger().severe("Failed to establish Redis bridge.");
            throw ex;
        }
        this.subscriberManager = new RedisSubscriberManager(this);
    }

    public boolean isAvailable() {
        return subscriberManager != null;
    }

    Jedis getConnection(){
        return pool.getResource();
    }

    public <Return> Return connect(Callback<Return, Jedis> callback) {
        return connect(callback, 0);
    }

    public <Return> Return connect(Callback<Return, Jedis> callback, int database) {
        try (Jedis jedis = pool.getResource()){
            jedis.select(database);
            return callback.callback(jedis);
        } catch (JedisConnectionException ex){
            throw new RedisException("Failed to get resource from the Jedis pool", ex);
        } catch (Exception ex) {
            throw new RedisException("Exception while handling Jedis connection request", ex);
        }
    }

    public void connect(CallbackRun<Jedis> callback) {
        connect(callback, 0);
    }

    public void connect(CallbackRun<Jedis> callback, int database) {
        try (Jedis jedis = pool.getResource()){
            jedis.select(database);
            callback.callback(jedis);
        } catch (JedisConnectionException ex){
            throw new RedisException("Failed to get resource from the Jedis pool", ex);
        } catch (Exception ex) {
            throw new RedisException("Exception while handling Jedis connection request", ex);
        }
    }

    public zServers getPlugin() {
        return plugin;
    }

    public RedisConfiguration getConfig() {
        return config;
    }

    public RedisSubscriberManager getSubscriberManager() {
        return subscriberManager;
    }

    public void sendMessage(String channel, String... messages) {
        subscriberManager.sendMessage(channel, messages);
    }
}
