package me.giiena.config.impl;

import me.giiena.config.impl.network.ConfigAckPayload;
import me.giiena.config.impl.network.ConfigPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class FabricConfigClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientConfigurationNetworking.registerGlobalReceiver(ConfigPayload.TYPE,
                (payload, ctx) -> {
                    ctx.client().execute(payload::handle);
                    ctx.responseSender().sendPacket(new ConfigAckPayload(payload.modID()));
                });
        ClientConfigurationConnectionEvents.DISCONNECT.register((_, _) ->
                ConfigManager.onDisconnect());

        ClientPlayConnectionEvents.DISCONNECT.register((_, _) ->
                ConfigManager.onDisconnect());
        ClientPlayNetworking.registerGlobalReceiver(
                ConfigPayload.TYPE,
                (payload, ctx) -> ctx.client().execute(payload::handle));
    }
}
