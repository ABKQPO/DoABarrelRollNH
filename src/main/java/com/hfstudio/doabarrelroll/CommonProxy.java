package com.hfstudio.doabarrelroll;

import com.hfstudio.doabarrelroll.network.ModNetworkHandler;
import com.hfstudio.doabarrelroll.util.BaublesElytraDurabilitySyncHandler;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLLoadCompleteEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public class CommonProxy {

    private final BaublesElytraDurabilitySyncHandler baublesSyncHandler = new BaublesElytraDurabilitySyncHandler();

    public void preInit(FMLPreInitializationEvent event) {
        ModNetworkHandler.init();
        FMLCommonHandler.instance()
            .bus()
            .register(baublesSyncHandler);
    }

    public void init(FMLInitializationEvent event) {}

    public void postInit(FMLPostInitializationEvent event) {}

    public void completeInit(FMLLoadCompleteEvent event) {}
}
