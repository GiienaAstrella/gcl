package me.giiena.config.impl;

import net.minecraft.network.protocol.configuration.ServerConfigurationPacketListener;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.network.event.RegisterConfigurationTasksEvent;

@EventBusSubscriber
public final class ConfigSyncEvents {
    @SubscribeEvent
    private static void onRegisterConfigTasks(RegisterConfigurationTasksEvent event) {
        ServerConfigurationPacketListener listener = event.getListener();
        ConfigManager.onConfigureConnection(listener,
                event::register,
                listener::finishCurrentTask);
    }

    @SubscribeEvent
    private static void onServerReload(OnDatapackSyncEvent event) {
        if (event.getPlayer() == null) {
            ConfigManager.onServerReload();
        }
    }
}
