package me.giiena.config.impl;

import me.giiena.config.impl.network.ConfigPayload;
import me.giiena.config.impl.network.ConfigReloadPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class FabricConfig implements ModInitializer {
    @Override
    public void onInitialize() {
        PayloadTypeRegistry.clientboundPlay().register(
                ConfigPayload.TYPE,
                ConfigPayload.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(
                ConfigReloadPayload.TYPE,
                ConfigReloadPayload.STREAM_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(
                ConfigReloadPayload.TYPE,
                (payload, context) -> context.server().execute(payload::handle));

        ServerLifecycleEvents.SERVER_STARTED.register(server -> ConfigState.server = server);
        ServerLifecycleEvents.SERVER_STOPPED.register(_ -> ConfigState.server = null);

        ServerPlayConnectionEvents.JOIN.register((handler, _, _) ->
                ConfigManager.onPlayerLogin(handler.player));
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((_, _, success) -> {
            if (success) ConfigManager.onServerReload();
        });
    }
}
