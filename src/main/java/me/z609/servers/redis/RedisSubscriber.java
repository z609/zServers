package me.z609.servers.redis;

public abstract class RedisSubscriber {

    private final String channel;

    public RedisSubscriber(String channel) {
        this.channel = channel;
    }

    public String getChannel() {
        return channel;
    }

    public abstract void onMessageReceived(String[] message);
}
