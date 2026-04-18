package com.hfstudio;

import net.minecraftforge.common.MinecraftForge;

import com.hfstudio.elytrahud.ElytraHudModule;
import com.hfstudio.flightassistant.FlightAssistantModule;
import com.hfstudio.roll.ClientEventHandler;
import com.hfstudio.roll.RollKeyBindings;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLLoadCompleteEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy {

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        RollKeyBindings.register();

        ClientEventHandler rollHandler = new ClientEventHandler();
        MinecraftForge.EVENT_BUS.register(rollHandler);
        FMLCommonHandler.instance()
            .bus()
            .register(rollHandler);

        ElytraHudModule.initClient();
        FlightAssistantModule.initClient();
    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {
        super.postInit(event);
    }

    @Override
    public void completeInit(FMLLoadCompleteEvent event) {
        super.completeInit(event);
    }
}
