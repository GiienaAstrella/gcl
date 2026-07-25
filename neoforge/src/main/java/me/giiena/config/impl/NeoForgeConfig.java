package me.giiena.config.impl;

import me.giiena.config.impl.network.ConfigAckPayload;
import me.giiena.config.impl.network.ConfigPayload;
import me.giiena.config.impl.network.ConfigReloadPayload;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@Mod(ConfigConstants.MOD_ID)
@EventBusSubscriber
public final class NeoForgeConfig {
    @SubscribeEvent
    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");

        registrar.configurationToClient(ConfigPayload.TYPE,
                ConfigPayload.STREAM_CODEC,
                (payload, ctx) -> ctx.enqueueWork(payload::handle)
                        .thenAccept(_ -> ctx.reply(new ConfigAckPayload(payload.modID()))));
        registrar.configurationToServer(ConfigAckPayload.TYPE,
                ConfigAckPayload.STREAM_CODEC,
                (payload, ctx) -> ConfigManager.onConfigConnectionAck(ctx.listener(), payload));

        registrar.playToClient(
                ConfigPayload.TYPE,
                ConfigPayload.STREAM_CODEC,
                (payload, ctx) -> ctx.enqueueWork(payload::handle));
        registrar.playToServer(
                ConfigReloadPayload.TYPE,
                ConfigReloadPayload.STREAM_CODEC,
                (payload, ctx) -> ctx.enqueueWork(payload::handle));
    }
}
