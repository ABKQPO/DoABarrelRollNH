package com.hfstudio.elytrahud;

import net.minecraftforge.common.MinecraftForge;

import com.hfstudio.doabarrelroll.DoABarrelRollNH;

import cpw.mods.fml.common.FMLCommonHandler;

/**
 * Module entry point for ElytraHUD.
 * Handles initialization of event handlers and configuration.
 */
public class ElytraHudModule {

    private static ElytraHudEventHandler eventHandler;

    private ElytraHudModule() {}

    /**
     * Initialize ElytraHUD module on the client side.
     */
    public static void initClient() {
        DoABarrelRollNH.LOG.info("Initializing ElytraHUD module");

        eventHandler = new ElytraHudEventHandler();
        MinecraftForge.EVENT_BUS.register(eventHandler);
        FMLCommonHandler.instance()
            .bus()
            .register(eventHandler);
    }

    public static ElytraHudEventHandler getEventHandler() {
        return eventHandler;
    }
}
