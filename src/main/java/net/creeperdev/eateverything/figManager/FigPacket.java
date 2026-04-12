package net.creeperdev.eateverything.figManager;


import net.minecraft.network.FriendlyByteBuf;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;


public record FigPacket(
       int nutrition,
       float saturation,
       Boolean alwaysEat,
       float consumeSeconds
) implements CustomPacketPayload {

    public static final Type<FigPacket> ID = new Type<>(Identifier.fromNamespaceAndPath("sleepdeprivation", "fig_packet"));

    public static final StreamCodec<FriendlyByteBuf, FigPacket> CODEC = StreamCodec.of(
            (buf, p) -> {
                buf.writeVarInt(p.nutrition);
                buf.writeFloat(p.saturation);
                buf.writeBoolean(p.alwaysEat);
                buf.writeFloat(p.consumeSeconds);

            },
            buf -> new FigPacket(buf.readVarInt(), buf.readFloat(),  buf.readBoolean(), buf.readFloat())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}