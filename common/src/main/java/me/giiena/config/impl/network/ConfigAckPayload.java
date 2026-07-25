package me.giiena.config.impl.network;

import me.giiena.config.impl.ConfigCommon;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jspecify.annotations.NonNull;

public record ConfigAckPayload(String modID) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ConfigAckPayload> TYPE =
            new CustomPacketPayload.Type<>(ConfigCommon.identifier("config_ack"));
    public static final StreamCodec<FriendlyByteBuf, ConfigAckPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, ConfigAckPayload::modID,
                    ConfigAckPayload::new);

    @Override
    @NonNull
    public Type<ConfigAckPayload> type() {
        return TYPE;
    }
}
