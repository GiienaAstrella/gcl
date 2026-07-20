package me.giiena.config;

import me.giiena.config.impl.ConfigManager;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber
public final class ConfigSyncEvents {
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ConfigManager.onPlayerLogin(player);
        }
    }

    @SubscribeEvent
    public static void onServerReload(OnDatapackSyncEvent event) {
        if (event.getPlayer() == null) {
            ConfigManager.onServerReload();
        }
    }
}
