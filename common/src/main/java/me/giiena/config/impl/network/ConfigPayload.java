package me.giiena.config.impl.network;

import me.giiena.config.impl.ConfigCommon;
import me.giiena.config.impl.ConfigManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jspecify.annotations.NonNull;

public record ConfigPayload(String modID, byte[] contents) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ConfigPayload> TYPE =
            new CustomPacketPayload.Type<>(ConfigCommon.identifier("config_sync"));
    public static final StreamCodec<FriendlyByteBuf, ConfigPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, ConfigPayload::modID,
                    ByteBufCodecs.BYTE_ARRAY, ConfigPayload::contents,
                    ConfigPayload::new);

    @Override
    @NonNull
    public Type<ConfigPayload> type() {
        return TYPE;
    }

    public void handle() {
        ConfigManager.onSyncReceived(this.modID(), this.contents());
    }
}
