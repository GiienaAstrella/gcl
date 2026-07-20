package me.giiena.config.platform.services;

import net.minecraft.server.level.ServerPlayer;

import java.nio.file.Path;

public interface IPlatformHelper {
    /**
     * Returns platform name.
     */
    @SuppressWarnings("unused")
    String getPlatformName();

    /**
     * Returns the default config directory.
     * Usually, this is {@code defaultconfigs}.
     */
    @SuppressWarnings("unused")
    Path getDefaultConfigDir();

    /**
     * Returns the config directory.
     * Usually, this is {@code config}.
     */
    Path getConfigDir();

    /**
     * Returns {@code true} on dedicated server platform.
     */
    @SuppressWarnings("unused")
    boolean isDedicatedServer();

    /**
     * Checks whether the game is currently in development environment.
     */
    boolean isDevelopmentEnvironment();

    /**
     * Sends sync data for {@code modID} for {@code player}.
     */
    void sendSyncPacket(ServerPlayer player, String modID, byte[] contents);

    /**
     * Sends sync data for {@code modID} for all players on the server.
     */
    void broadcastSyncPacket(String modID, byte[] contents);

    /**
     * Sends reload data for {@code modID} to the server.
     */
    void sendReloadPacket(String modID);
}
