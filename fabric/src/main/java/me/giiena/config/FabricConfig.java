package me.giiena.config;

import me.giiena.config.impl.ConfigManager;
import me.giiena.config.impl.networking.ConfigPayload;
import me.giiena.config.impl.networking.ConfigReloadPayload;
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
                (payload, context) -> context.server().execute(() ->
                        ConfigManager.onServerReload(payload.modID())));

        ServerLifecycleEvents.SERVER_STARTED.register(server -> ConfigState.server = server);
        ServerLifecycleEvents.SERVER_STOPPED.register(_ -> ConfigState.server = null);

        ServerPlayConnectionEvents.JOIN.register((handler, _, _) ->
                ConfigManager.onPlayerLogin(handler.player));
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((_, _, success) -> {
            if (success) ConfigManager.onServerReload();
        });
    }
}
