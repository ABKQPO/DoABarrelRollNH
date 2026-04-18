package com.hfstudio.elytrahud;

import com.gtnewhorizon.gtnhlib.config.Config;
import com.gtnewhorizon.gtnhlib.config.Config.Comment;
import com.gtnewhorizon.gtnhlib.config.Config.DefaultBoolean;
import com.gtnewhorizon.gtnhlib.config.Config.DefaultFloat;
import com.gtnewhorizon.gtnhlib.config.Config.DefaultInt;
import com.gtnewhorizon.gtnhlib.config.Config.RangeFloat;
import com.gtnewhorizon.gtnhlib.config.Config.RangeInt;
import com.gtnewhorizon.gtnhlib.config.ConfigException;
import com.gtnewhorizon.gtnhlib.config.ConfigurationManager;
import com.hfstudio.DoABarrelRollNH;

@Config(
    modid = DoABarrelRollNH.MODID,
    category = "elytrahud",
    filename = "elytrahud",
    configSubDirectory = "DoABarrelRoll")
@Config.LangKeyPattern(pattern = "dabr.gui.config.elytrahud.%field", fullyQualified = true)
@Comment("ElytraHUD - Gauge-style flight HUD configuration")
public class ElytraHudConfig {

    public static void registerConfig() throws ConfigException {
        ConfigurationManager.registerConfig(ElytraHudConfig.class);
    }

    @Comment("Enable or disable ElytraHUD module")
    @DefaultBoolean(false)
    public static boolean enabled = false;

    @Comment("Render the airspeed gauge")
    @DefaultBoolean(true)
    public static boolean renderAirspeed = true;

    @Comment("Render the firework rate gauge")
    @DefaultBoolean(true)
    public static boolean renderFireworksRate = true;

    @Comment("Render the elytra durability bar")
    @DefaultBoolean(true)
    public static boolean renderDurability = true;

    @Comment("Render the altitude gauge")
    @DefaultBoolean(true)
    public static boolean renderAltitude = true;

    @Comment("Render the vertical speed gauge")
    @DefaultBoolean(true)
    public static boolean renderVertical = true;

    @Comment("Render the compass")
    @DefaultBoolean(true)
    public static boolean renderCompass = true;

    @Comment("Render gauge titles")
    @DefaultBoolean(true)
    public static boolean renderTitles = true;

    @Comment("Render numeric values on gauges")
    @DefaultBoolean(false)
    public static boolean renderValues = false;

    @Comment("Auto-switch to third person on elytra open")
    @DefaultBoolean(false)
    public static boolean thirdPersonEnabled = false;

    @Comment("Compass horizontal position offset (0-100, percentage of screen)")
    @DefaultInt(0)
    @RangeInt(min = 0, max = 100)
    public static int compassDefaultX = 0;

    @Comment("HUD display scale (0.5 = half size, 2.0 = double size)")
    @DefaultFloat(0.8f)
    @RangeFloat(min = 0.1f, max = 10.0f)
    public static float hudScale = 0.8f;
}
