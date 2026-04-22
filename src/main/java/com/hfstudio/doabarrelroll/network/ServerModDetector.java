package com.hfstudio.doabarrelroll.network;

import net.minecraft.client.network.NetHandlerPlayClient;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Client-only listener that detects whether the server has DoABarrelRollNH installed.
 * Detection relies on FML's plugin-channel REGISTER handshake: when a server with this
 * mod installed sends its channel registrations the channel name {@link ModNetworkHandler#CHANNEL}
 * will be included. For integrated (LAN/singleplayer) servers the channel is always present.
 * Must be registered on the FML event bus from the client proxy only.
 */
@SideOnly(Side.CLIENT)
public class ServerModDetector {

    private static boolean serverHasMod = false;

    @SubscribeEvent
    public void onClientConnected(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        // Integrated/LAN server: our mod is always installed on both sides.
        if (event.isLocal) {
            serverHasMod = true;
            return;
        }
        // Remote server: assume not installed until we receive the REGISTER packet.
        serverHasMod = false;
    }

    /**
     * FML fires this event when the server sends a plugin-channel REGISTER payload.
     * If our channel appears in the set the server has this mod installed.
     */
    @SubscribeEvent
    public void onChannelRegistration(FMLNetworkEvent.CustomPacketRegistrationEvent<NetHandlerPlayClient> event) {
        if (event.side == Side.SERVER && "REGISTER".equals(event.operation)) {
            if (event.registrations.contains(ModNetworkHandler.CHANNEL)) {
                serverHasMod = true;
            }
        }
    }

    @SubscribeEvent
    public void onClientDisconnected(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        serverHasMod = false;
    }

    /**
     * Returns true if the currently connected server has DoABarrelRollNH installed.
     */
    public static boolean isServerModInstalled() {
        return serverHasMod;
    }
}
