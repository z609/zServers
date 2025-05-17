package me.z609.servers.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;

public class RedisSubscriberManager {

    private final RedisBridge bridge;
    private final List<RedisSubscriber> subscribers = new CopyOnWriteArrayList<>();
    private final Set<String> channels = Collections.synchronizedSet(new HashSet<>());

    private Thread listenerThread;
    private JedisPubSub subscription;

    public RedisSubscriberManager(RedisBridge bridge) {
        this.bridge = bridge;
        bridge.getPlugin().getLogger().log(Level.INFO, "Redis Subscription Manager has been started.");
        startListener();
    }

    public void subscribe(RedisSubscriber subscriber) {
        if (subscribers.contains(subscriber)) {
            throw new IllegalArgumentException("Already subscribed with this instance");
        }

        subscribers.add(subscriber);
        channels.add(subscriber.getChannel());

        restartListener(); // force a reconnect with updated channels
    }

    public void unsubscribe(RedisSubscriber subscriber) {
        if (!subscribers.remove(subscriber)) {
            return; // Already removed or never subscribed
        }

        boolean removedChannel = channels.remove(subscriber.getChannel());

        if (removedChannel) {
            restartListener(); // force re-subscribe with updated channel list
        }
    }

    public long sendMessage(String channel, String... messages){
        final String message = String.join("\u001F", messages);
        return bridge.connect(jedis -> {
            return jedis.publish(channel, message);
        });
    }

    private void startListener() {
        listenerThread = new Thread(() -> {
            while (true) {
                try (Jedis jedis = bridge.getConnection()) {
                    subscription = new JedisPubSub() {
                        @Override
                        public void onMessage(String channel, String message) {
                            notifySubscribers(channel, message);
                        }
                    };

                    String[] channelArray;
                    synchronized (channels) {
                        channelArray = channels.toArray(new String[0]);
                    }

                    if (channelArray.length > 0) {
                        jedis.subscribe(subscription, channelArray);
                    } else {
                        Thread.sleep(1000); // wait for subscriptions
                    }

                } catch (Exception ex) {
                    bridge.getPlugin().getLogger().log(Level.SEVERE, "Redis pub/sub error. Retrying in 5s...", ex);
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException ignored) {}
                }
            }
        }, "RedisSubscriberThread");
        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    private void restartListener() {
        if (subscription != null) {
            try {
                subscription.unsubscribe(); // triggers reconnect
            } catch (Exception ignored) {}
        }
    }

    private void notifySubscribers(String channel, String message) {
        String[] split = message.split("\u001F");
        for (RedisSubscriber sub : subscribers) {
            if (sub.getChannel().equals(channel)) {
                sub.onMessageReceived(split);
            }
        }
    }
}
