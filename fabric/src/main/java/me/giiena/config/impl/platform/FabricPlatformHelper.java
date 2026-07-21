package me.giiena.config.impl.platform;

import me.giiena.config.impl.ConfigState;
import me.giiena.config.impl.platform.services.IPlatformHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

import java.io.File;
import java.nio.file.Path;

public class FabricPlatformHelper implements IPlatformHelper {
    public static Path gameDir = new File(".").toPath();

    @Override
    public String getPlatformName() {
        return "Fabric";
    }

    @Override
    public Path getDefaultConfigDir() {
        return gameDir.resolve("defaultconfigs");
    }

    @Override
    public Path getConfigDir() {
        return gameDir.resolve("config");
    }

    @Override
    public boolean isDedicatedServer() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER;
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public <T extends CustomPacketPayload> void sendPacketToServer(T payload) {
        if (Minecraft.getInstance().getConnection() == null) return;
        ClientPlayNetworking.send(payload);
    }

    @Override
    public <T extends CustomPacketPayload> void sendPacketToClient(ServerPlayer player, T payload) {
        ServerPlayNetworking.send(player, payload);
    }

    @Override
    public <T extends CustomPacketPayload> void broadcastPacketToClients(T payload) {
        if (ConfigState.server != null) {
            for (ServerPlayer player : ConfigState.server.getPlayerList().getPlayers()) {
                ServerPlayNetworking.send(player, payload);
            }
        }
    }
}
