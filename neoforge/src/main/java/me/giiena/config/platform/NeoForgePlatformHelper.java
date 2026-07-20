package me.giiena.config.platform;

import me.giiena.config.impl.networking.ConfigPayload;
import me.giiena.config.impl.networking.ConfigReloadPayload;
import me.giiena.config.platform.services.IPlatformHelper;
import net.minecraft.client.Minecraft;
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
    public void sendSyncPacket(ServerPlayer player, String modID, byte[] contents) {
        PacketDistributor.sendToPlayer(player, new ConfigPayload(modID, contents));
    }

    @Override
    public void broadcastSyncPacket(String modID, byte[] contents) {
        PacketDistributor.sendToAllPlayers(new ConfigPayload(modID, contents));
    }

    @Override
    public void sendReloadPacket(String modID) {
        if (Minecraft.getInstance().getConnection() == null) return;
        ClientPacketDistributor.sendToServer(new ConfigReloadPayload(modID));
    }
}
