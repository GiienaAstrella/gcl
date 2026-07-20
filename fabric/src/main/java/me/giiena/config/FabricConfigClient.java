package me.giiena.config;

import me.giiena.config.impl.ConfigManager;
import me.giiena.config.impl.networking.ConfigPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class FabricConfigClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(
                ConfigPayload.TYPE,
                (payload, context) -> context.client().execute(() ->
                        ConfigManager.onSyncReceived(payload.modID(), payload.contents())));
    }
}
