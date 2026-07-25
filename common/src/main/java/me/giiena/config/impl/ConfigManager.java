package me.giiena.config.impl;

import me.giiena.config.api.Config;
import me.giiena.config.api.NestedMap;
import me.giiena.config.impl.network.ConfigAckPayload;
import me.giiena.config.impl.network.ConfigPayload;
import me.giiena.config.impl.network.ConfigSyncTask;
import me.giiena.config.impl.platform.Services;
import net.minecraft.network.protocol.configuration.ServerConfigurationPacketListener;
import net.minecraft.server.network.ConfigurationTask;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;

import java.util.Optional;
import java.util.function.Consumer;

@SuppressWarnings("LoggingSimilarMessage")
public class ConfigManager {
    private static final NestedMap<Object, String, ConfigSyncTask> PENDING_CONFIG_TASKS =
            new NestedMap<>();

    /**
     * Configuration phase event handler.
     * Syncs all {@link Config.Type#COMMON} config managed by the library.
     */
    public static void onConfigureConnection(ServerConfigurationPacketListener listener,
                                             Consumer<ConfigurationTask> queuer,
                                             Consumer<ConfigurationTask.Type> finisher) {
        String player;
        if (listener instanceof ServerConfigurationPacketListenerImpl impl) {
            player = impl.getOwner().name();
        } else {
            player = "UNKNOWN PLAYER";
        }

        for (Config config : ConfigRegistryImpl.getAllCommons()) {
            ConfigConstants.LOG.info("Syncing common config for {} with {}",
                    config.getModID(),
                    player);
            ConfigSyncTask task = new ConfigSyncTask(finisher, config.getModID(), config.toml());
            PENDING_CONFIG_TASKS.put(listener, config.getModID(), task);
            queuer.accept(task);
        }
    }

    /**
     * Configuration phase acknowledgement event handler.
     */
    public static void onConfigConnectionAck(Object listener, ConfigAckPayload payload) {
        ConfigSyncTask task = PENDING_CONFIG_TASKS.get(listener, payload.modID());
        if (task == null) return;
        task.ack();
        PENDING_CONFIG_TASKS.remove(listener, payload.modID());
    }

    /**
     * Disconnect event handler.
     */
    public static void onDisconnect() {
        for (Config config : ConfigRegistryImpl.getAllCommons()) {
            ConfigConstants.LOG.info("Clearing synced common config for {}", config.getModID());
            config.clearSyncedValues();
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
            Services.PLATFORM.broadcastPacketToClients(new ConfigPayload(config.getModID(),
                    config.toml()));
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
            Services.PLATFORM.broadcastPacketToClients(new ConfigPayload(modID, conf.toml()));
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
