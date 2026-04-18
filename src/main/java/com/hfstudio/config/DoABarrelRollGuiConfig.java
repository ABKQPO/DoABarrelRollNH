package com.hfstudio.config;

import net.minecraft.client.gui.GuiScreen;

import com.gtnewhorizon.gtnhlib.config.ConfigException;
import com.gtnewhorizon.gtnhlib.config.SimpleGuiConfig;
import com.hfstudio.DoABarrelRollNH;
import com.hfstudio.elytrahud.ElytraHudConfig;
import com.hfstudio.flightassistant.FAConfig;

public class DoABarrelRollGuiConfig extends SimpleGuiConfig {

    public DoABarrelRollGuiConfig(GuiScreen parentScreen) throws ConfigException {
        super(
            parentScreen,
            DoABarrelRollNH.MODID,
            DoABarrelRollNH.MODNAME,
            true,
            ModConfig.class,
            FAConfig.class,
            ElytraHudConfig.class);
    }
}
