package it.pintux.life.common.utils;

import java.util.UUID;

public interface FormPlayer {
    UUID getUniqueId();

    String getName();

    void sendMessage(String message);

    void executeAction(String action);

    boolean hasPermission(String permission);
}
