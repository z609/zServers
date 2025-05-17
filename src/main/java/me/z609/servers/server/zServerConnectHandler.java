package me.z609.servers.server;

public interface zServerConnectHandler {
    void onFailure(Exception ex);
    void onSuccess();
    void onCancelled(String message);
}
