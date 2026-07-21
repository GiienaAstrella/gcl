package me.giiena.config.impl.platform;

import me.giiena.config.impl.platform.services.IPlatformHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.network.PacketDistributor;

import java.nio.file.Path;

public class NeoForgePlatformHelper implements IPlatformHelper {
    @Override
    public String getPlatformName() {
        return "NeoForge";
    }

    @Override
    public Path getDefaultConfigDir() {
        return FMLPaths.GAMEDIR.get().resolve("defaultconfigs");
    }

    @Override
    public Path getConfigDir() {
        return FMLPaths.CONFIGDIR.get();
    }

    @Override
    public boolean isDedicatedServer() {
        return FMLLoader.getCurrent().getDist().isDedicatedServer();
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return !FMLLoader.getCurrent().isProduction();
    }

    @Override
    public <T extends CustomPacketPayload> void sendPacketToServer(T payload) {
        if (Minecraft.getInstance().getConnection() == null) return;
        ClientPacketDistributor.sendToServer(payload);
    }

    @Override
    public <T extends CustomPacketPayload> void sendPacketToClient(ServerPlayer player, T payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }

    @Override
    public <T extends CustomPacketPayload> void broadcastPacketToClients(T payload) {
        PacketDistributor.sendToAllPlayers(payload);
    }
}
