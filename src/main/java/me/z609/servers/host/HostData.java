package me.z609.servers.host;

import org.bukkit.Server;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public class HostData {

    private String name;
    private String ip;
    private int port;
    boolean online;
    long heartbeat;
    Map<UUID, String> players;
    int maxServers;

    public HostData(String name, String ip, int port) {
        this.name = name;
        this.ip = ip;
        this.port = port;
    }

    public String getName() {
        return name;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public boolean isOnline() {
        return online;
    }

    public long getHeartbeat() {
        return heartbeat;
    }

    public Map<UUID, String> getPlayers() {
        return players;
    }

    public int getMaxServers() {
        return maxServers;
    }

    public static HostData fromServer(Server server){
        return new HostData(server.getServerName(), getLocalIpAddress(), server.getPort());
    }

    private static String getLocalIpAddress() {
        try (final DatagramSocket socket = new DatagramSocket()) {
            socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
            return socket.getLocalAddress().getHostAddress();
        } catch (Exception e) {
            return "127.0.0.1";
        }
    }

    public void updateAddress(String ip, int port) {
        if(!this.ip.equals(ip)) {
            this.ip = ip;
        }
        if(this.port != port) {
            this.port = port;
        }
    }

    public Collection<String> getPlayerNames(){
        return players.values();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Host) {
            obj = ((Host) obj).getData();
        }
        if(!(obj instanceof HostData)) {
            return false;
        }
        HostData o = (HostData)obj;
        return this.ip.equals(o.ip) && this.port==o.port && this.name.equals(o.name);
    }
}
