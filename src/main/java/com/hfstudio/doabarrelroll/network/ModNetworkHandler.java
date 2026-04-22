package com.hfstudio.doabarrelroll.network;

import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;

/**
 * Registers all network messages for DoABarrelRollNH.
 * Call {@link #init()} from both client and server proxy init phases.
 */
public class ModNetworkHandler {

    public static final String CHANNEL = "dabr:net";
    public static SimpleNetworkWrapper INSTANCE;

    private static int nextId = 0;

    public static void init() {
        INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(CHANNEL);
        INSTANCE.registerMessage(ThrowTntMessageHandler.class, ThrowTntMessage.class, nextId++, Side.SERVER);
    }
}
