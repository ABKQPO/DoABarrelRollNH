package com.hfstudio.flightassistant;

import net.minecraftforge.common.MinecraftForge;

import com.hfstudio.DoABarrelRollNH;

import cpw.mods.fml.common.FMLCommonHandler;

/**
 * Module entry point for FlightAssistant.
 * Handles initialization of event handlers, key bindings, and configuration.
 */
public class FlightAssistantModule {

    private static FAEventHandler eventHandler;

    private FlightAssistantModule() {}

    /**
     * Initialize FlightAssistant module on the client side.
     */
    public static void initClient() {
        DoABarrelRollNH.LOG.info("Initializing FlightAssistant module");
        FAKeyBindings.register();

        eventHandler = new FAEventHandler();
        MinecraftForge.EVENT_BUS.register(eventHandler);
        FMLCommonHandler.instance()
            .bus()
            .register(eventHandler);
    }

    public static FAEventHandler getEventHandler() {
        return eventHandler;
    }
}
