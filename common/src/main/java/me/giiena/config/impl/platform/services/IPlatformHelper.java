package me.giiena.config.impl.platform.services;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

import java.nio.file.Path;

@SuppressWarnings("unused")
public interface IPlatformHelper {
    /**
     * Returns platform name.
     */
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
     * Sends {@code payload} to the server.
     */
    <T extends CustomPacketPayload> void sendPacketToServer(T payload);

    /**
     * Sends {@code payload} to {@code player}.
     */
    <T extends CustomPacketPayload> void sendPacketToClient(ServerPlayer player, T payload);

    /**
     * Sends {@code payload} to all {@link ServerPlayer} connected to the server.
     */
    <T extends CustomPacketPayload> void broadcastPacketToClients(T payload);
}
