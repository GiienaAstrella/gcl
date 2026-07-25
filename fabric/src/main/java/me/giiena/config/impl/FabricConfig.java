package me.giiena.config.impl;

import me.giiena.config.impl.network.ConfigAckPayload;
import me.giiena.config.impl.network.ConfigPayload;
import me.giiena.config.impl.network.ConfigReloadPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;

public class FabricConfig implements ModInitializer {
    @Override
    public void onInitialize() {
        PayloadTypeRegistry.clientboundConfiguration().register(ConfigPayload.TYPE,
                ConfigPayload.STREAM_CODEC);
        PayloadTypeRegistry.serverboundConfiguration().register(ConfigAckPayload.TYPE,
                ConfigAckPayload.STREAM_CODEC);

        PayloadTypeRegistry.clientboundPlay().register(ConfigPayload.TYPE,
                ConfigPayload.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ConfigReloadPayload.TYPE,
                ConfigReloadPayload.STREAM_CODEC);

        ServerLifecycleEvents.SERVER_STARTED.register(server -> ConfigState.server = server);
        ServerLifecycleEvents.SERVER_STOPPED.register(_ -> ConfigState.server = null);
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((_, _, success) -> {
            if (success) ConfigManager.onServerReload();
        });

        ServerConfigurationConnectionEvents.CONFIGURE.register((listener, _) -> {
            if (!ServerConfigurationNetworking.canSend(listener, ConfigPayload.TYPE)) {
                listener.disconnect(Component.translatable(ConfigCommon.langKey("config_sync",
                        "gui",
                        "disconnect")));
                return;
            }

            ConfigManager.onConfigureConnection(listener, listener::addTask, listener::completeTask);
            ServerConfigurationNetworking.registerReceiver(listener,
                    ConfigAckPayload.TYPE,
                    (payload, ctx) -> ConfigManager.onConfigConnectionAck(ctx.packetListener(),
                            payload));
        });

        ServerPlayNetworking.registerGlobalReceiver(ConfigReloadPayload.TYPE,
                (payload, context) -> context.server().execute(payload::handle));
    }
}
