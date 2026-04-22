package com.hfstudio.doabarrelroll;

import net.minecraftforge.common.MinecraftForge;

import com.hfstudio.doabarrelroll.network.ServerModDetector;
import com.hfstudio.doabarrelroll.roll.ClientEventHandler;
import com.hfstudio.doabarrelroll.roll.RollKeyBindings;
import com.hfstudio.elytrahud.ElytraHudModule;
import com.hfstudio.flightassistant.FlightAssistantModule;

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

        // Detect whether the server has this mod installed via plugin-channel handshake
        ServerModDetector detector = new ServerModDetector();
        FMLCommonHandler.instance()
            .bus()
            .register(detector);

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
