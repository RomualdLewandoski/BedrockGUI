package it.pintux.life.common;

import org.geysermc.floodgate.api.FloodgateApi;

import java.util.UUID;

public class FloodgateUtil {

    public static boolean isFloodgate(UUID playerUUID) {
        try {
            FloodgateApi api = FloodgateApi.getInstance();
            return api.isFloodgatePlayer(playerUUID);
        } catch (Exception e) {
            return false;
        }
    }
}
