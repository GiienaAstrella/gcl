package me.giiena.config.impl.network;

import me.giiena.config.impl.ConfigCommon;
import io.netty.buffer.ByteBuf;
import me.giiena.config.impl.ConfigManager;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jspecify.annotations.NonNull;

public record ConfigReloadPayload(String modID) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ConfigReloadPayload> TYPE =
            new CustomPacketPayload.Type<>(ConfigCommon.identifier("config_reload"));
    public static final StreamCodec<ByteBuf, ConfigReloadPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, ConfigReloadPayload::modID,
                    ConfigReloadPayload::new);

    @Override
    @NonNull
    public Type<ConfigReloadPayload> type() {
        return TYPE;
    }

    public void handle() {
        ConfigManager.onServerReload(this.modID());
    }
}
