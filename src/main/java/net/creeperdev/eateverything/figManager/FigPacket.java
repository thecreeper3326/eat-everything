package net.creeperdev.eateverything.figManager;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record FigPacket(
       String figs
) implements CustomPacketPayload {
    public static final Type<FigPacket> ID = new Type<>(Identifier.fromNamespaceAndPath(FigManager.name, "fig_packet"));
    public static final StreamCodec<FriendlyByteBuf, FigPacket> CODEC = StreamCodec.of(
            (buf, p) -> {
                buf.writeUtf(p.figs);
            },
            buf -> new FigPacket(buf.readUtf())
    );
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}