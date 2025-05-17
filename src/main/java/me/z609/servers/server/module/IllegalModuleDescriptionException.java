package me.z609.servers.server.module;

public class IllegalModuleDescriptionException extends RuntimeException {
    public IllegalModuleDescriptionException(String message) {
        super(message);
    }

    public IllegalModuleDescriptionException(String message, Throwable cause) {
        super(message, cause);
    }
}
