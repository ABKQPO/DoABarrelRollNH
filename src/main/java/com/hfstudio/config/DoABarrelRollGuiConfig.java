package com.hfstudio.config;

import net.minecraft.client.gui.GuiScreen;

import com.gtnewhorizon.gtnhlib.config.ConfigException;
import com.gtnewhorizon.gtnhlib.config.SimpleGuiConfig;
import com.hfstudio.DoABarrelRollNH;

public class DoABarrelRollGuiConfig extends SimpleGuiConfig {

    public DoABarrelRollGuiConfig(GuiScreen parentScreen) throws ConfigException {
        super(parentScreen, ModConfig.class, DoABarrelRollNH.MODID, DoABarrelRollNH.MODNAME);
    }
}
