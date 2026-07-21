package me.giiena.config.impl;

import me.giiena.config.impl.network.ConfigPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class FabricConfigClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(
                ConfigPayload.TYPE,
                (payload, ctx) -> ctx.client().execute(payload::handle));
    }
}
