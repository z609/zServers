package me.z609.servers.server;

import me.z609.servers.Callback;
import me.z609.servers.CallbackRun;
import me.z609.servers.redis.RedisBridge;
import me.z609.servers.redis.RedisSubscriber;
import me.z609.servers.server.module.zModule;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class zServerRedisBridge {

    public static final int ZSERVERS_MODULE_DB = 1;

    private zServer server;
    private Map<zModule, List<RedisSubscriber>> moduleSubscribers = new HashMap<>();

    public zServerRedisBridge(zServer server) {
        this.server = server;
    }

    public void connect(CallbackRun<Jedis> callback){
        if(!isAvailable())
            throw new IllegalStateException("zServers Redis Bridge is unavailable");
        getServersBridge().connect(callback, ZSERVERS_MODULE_DB);
    }

    public <Return> Return connect(Callback<Return, Jedis> callback){
        if(!isAvailable())
            throw new IllegalStateException("zServers Redis Bridge is unavailable");
        return getServersBridge().connect(callback, ZSERVERS_MODULE_DB);
    }

    public boolean isAvailable(){
        return getServersBridge() != null && getServersBridge().isAvailable();
    }

    private RedisBridge getServersBridge(){
        return server.getPlugin().getRedisBridge();
    }

    public void registerSubscriber(zModule module, RedisSubscriber subscriber) {
        moduleSubscribers.computeIfAbsent(module, k -> new ArrayList<>()).add(subscriber);
        getServersBridge().getSubscriberManager().subscribe(subscriber);
    }

    public void unregisterModule(zModule module) {
        List<RedisSubscriber> list = moduleSubscribers.remove(module);
        if (list != null) {
            for (RedisSubscriber sub : list) {
                getServersBridge().getSubscriberManager().unsubscribe(sub);
            }
        }
    }

    public void unregisterSubscriber(zModule module, RedisSubscriber subscriber){
        List<RedisSubscriber> list = moduleSubscribers.get(module);
        if(list != null && list.remove(subscriber)){
            getServersBridge().getSubscriberManager().unsubscribe(subscriber);
            if(list.isEmpty()){
                moduleSubscribers.remove(module);
            }
        }
    }

    public void sendMessage(String channel, String... messages){
        server.getPlugin().getRedisBridge().sendMessage(channel, messages);
    }

}
