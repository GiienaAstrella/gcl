package me.giiena.config.impl.network;

import me.giiena.config.impl.ConfigCommon;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.network.ConfigurationTask;
import org.jspecify.annotations.NonNull;

import java.util.function.Consumer;

public class ConfigSyncTask implements ConfigurationTask {
    private static final Identifier BASE_ID = ConfigCommon.identifier("config_sync/");

    private final Consumer<Type> finisher;
    private final Type type;
    private final String modID;
    private final byte[] contents;

    public ConfigSyncTask(final Consumer<Type> finisher,
                          final String modID,
                          final byte[] contents) {
        this.finisher = finisher;
        this.type = new Type(BASE_ID.withSuffix(modID).toString());
        this.modID = modID;
        this.contents = contents;
    }

    @Override
    public void start(Consumer<Packet<?>> sender) {
        sender.accept(new ClientboundCustomPayloadPacket(new ConfigPayload(this.modID,
                this.contents)));
    }

    @Override
    @NonNull
    public Type type() {
        return this.type;
    }

    public void ack() {
        this.finisher.accept(this.type);
    }
}
