package com.hfstudio.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;

/**
 * Sent from client to server when the player right-clicks while holding TNT during elytra flight.
 * The server validates conditions (flint and steel in inventory) and spawns a primed TNT entity.
 */
public class ThrowTntMessage implements IMessage {

    @Override
    public void fromBytes(ByteBuf buf) {}

    @Override
    public void toBytes(ByteBuf buf) {}
}
