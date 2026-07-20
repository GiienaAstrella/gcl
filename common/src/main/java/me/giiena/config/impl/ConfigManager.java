package me.giiena.config.impl;

import me.giiena.config.ConfigConstants;
import me.giiena.config.api.Config;
import me.giiena.config.platform.Services;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

@SuppressWarnings("LoggingSimilarMessage")
public class ConfigManager {
    /**
     * Player login event handler.
     * Syncs all {@link Config.Type#COMMON} config managed by the library with {@code player}.
     */
    public static void onPlayerLogin(ServerPlayer player) {
        for (Config config : ConfigRegistryImpl.getAllCommons()) {
            ConfigConstants.LOG.info("Syncing common config for {} with {}",
                    config.getModID(), player.getName().getString());
            Services.PLATFORM.sendSyncPacket(player, config.getModID(), config.toml());
        }
    }

    /**
     * Server reload event handler.
     * Broadcasts all {@link Config.Type#COMMON} config managed by the library to all players.
     */
    public static void onServerReload() {
        for (Config config : ConfigRegistryImpl.getAllCommons()) {
            ConfigConstants.LOG.info("Broadcasting common config for {}", config.getModID());
            config.load();
            Services.PLATFORM.broadcastSyncPacket(config.getModID(), config.toml());
        }
    }

    /**
     * Server reload event handler.
     * Broadcasts {@link Config.Type#COMMON} config for {@code modID} to all players.
     */
    public static void onServerReload(String modID) {
        Optional<Config> config = ConfigRegistryImpl.get(modID, Config.Type.COMMON);
        config.ifPresent(conf -> {
            ConfigConstants.LOG.info("Broadcasting common config for {}", conf.getModID());
            conf.load();
            Services.PLATFORM.broadcastSyncPacket(modID, conf.toml());
        });
    }

    /**
     * Sync data event handler.
     * Applies the received {@link Config.Type#COMMON} config for {@code modID} to the local,
     * in-memory copy.
     */
    public static void onSyncReceived(String modID, byte[] contents) {
        ConfigConstants.LOG.info("Received common config for {}", modID);
        ConfigRegistryImpl.get(modID, Config.Type.COMMON)
                .ifPresent(config -> config.acceptSyncedConfig(contents));
    }
}
