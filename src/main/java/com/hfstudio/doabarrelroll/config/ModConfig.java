package com.hfstudio.doabarrelroll.config;

import com.gtnewhorizon.gtnhlib.config.Config;
import com.gtnewhorizon.gtnhlib.config.Config.Comment;
import com.gtnewhorizon.gtnhlib.config.Config.DefaultBoolean;
import com.gtnewhorizon.gtnhlib.config.Config.DefaultDouble;
import com.gtnewhorizon.gtnhlib.config.Config.DefaultFloat;
import com.gtnewhorizon.gtnhlib.config.Config.RangeDouble;
import com.gtnewhorizon.gtnhlib.config.Config.RangeFloat;
import com.gtnewhorizon.gtnhlib.config.Config.RequiresMcRestart;
import com.gtnewhorizon.gtnhlib.config.ConfigException;
import com.gtnewhorizon.gtnhlib.config.ConfigurationManager;
import com.hfstudio.doabarrelroll.DoABarrelRollNH;

@Config(modid = DoABarrelRollNH.MODID, filename = "doabarrelroll", configSubDirectory = "DoABarrelRoll")
@Config.LangKeyPattern(pattern = "dabr.gui.config.%cat.%field", fullyQualified = true)
@Comment("Do a Barrel Roll NH configuration")
public class ModConfig {

    public static void registerConfig() throws ConfigException {
        ConfigurationManager.registerConfig(ModConfig.class);
    }

    @Comment("Enable or disable the mod logic entirely")
    @DefaultBoolean(true)
    public static boolean modEnabled = true;

    @Comment("Swap roll and yaw axes for mouse input")
    @DefaultBoolean(false)
    public static boolean switchRollAndYaw = false;

    @Comment("Use a momentum-based mouse for continuous turning")
    @DefaultBoolean(false)
    public static boolean momentumBasedMouse = false;

    @Comment("Invert pitch input while rolling")
    @DefaultBoolean(false)
    public static boolean invertPitch = false;

    @Comment("Invert yaw input while rolling")
    @DefaultBoolean(false)
    public static boolean invertYaw = false;

    @Comment("Invert roll input while rolling")
    @DefaultBoolean(false)
    public static boolean invertRoll = false;

    @Comment("Invert the rendered roll (camera + player model) without affecting physics or banking")
    @DefaultBoolean(true)
    public static boolean invertVisualRoll = true;

    @Comment("Disable roll control when the player is submerged")
    @DefaultBoolean(true)
    public static boolean disableWhenSubmerged = true;

    @Comment("Yaw rate from keys in degrees per tick")
    @DefaultFloat(13.5f)
    @RangeFloat(min = 0.1f, max = 20.0f)
    public static float yawRateDegPerTick = 13.5f;

    @Comment("Barrel-roll animation duration in ticks")
    @DefaultFloat(12.0f)
    @RangeFloat(min = 2.0f, max = 40.0f)
    public static float barrelRollDurationTicks = 12.0f;

    @Comment("Horizontal dodge impulse applied during barrel rolls")
    @DefaultFloat(0.08f)
    @RangeFloat(min = 0.0f, max = 0.5f)
    public static float barrelRollDodgeStrength = 0.08f;

    @Comment("Roll return damping per tick when not rolling")
    @DefaultFloat(0.85f)
    @RangeFloat(min = 0.5f, max = 0.99f)
    public static float rollReturnDamping = 0.85f;

    @Comment("Deadzone for the momentum-based mouse")
    @DefaultDouble(0.2)
    @RangeDouble(min = 0.0, max = 1.0)
    public static double momentumMouseDeadzone = 0.2;

    public static final Smoothing smoothing = new Smoothing();
    public static final Banking banking = new Banking();
    public static final Sensitivity sensitivity = new Sensitivity();
    public static final Debug debug = new Debug();

    @Comment("Smoothing settings for pitch/yaw/roll inputs")
    public static class Smoothing {

        @Comment("Enable smoothing for pitch/yaw/roll inputs")
        @DefaultBoolean(true)
        public boolean enabled = true;

        @Comment("Smoothing strength for pitch")
        @DefaultDouble(1.0)
        @RangeDouble(min = 0.0, max = 10.0)
        public double pitch = 1.0;

        @Comment("Smoothing strength for yaw")
        @DefaultDouble(2.5)
        @RangeDouble(min = 0.0, max = 10.0)
        public double yaw = 2.5;

        @Comment("Smoothing strength for roll")
        @DefaultDouble(1.0)
        @RangeDouble(min = 0.0, max = 10.0)
        public double roll = 1.0;

        @Comment("When raw input is zero, reduces smoothing tail by shrinking internal backlog (higher = stops sooner)")
        @DefaultDouble(1.0)
        @RangeDouble(min = 1.0, max = 20.0)
        public double stopBoost = 1.0;
    }

    @Comment("Banking and automatic righting settings")
    public static class Banking {

        @Comment("Enable banking (adds subtle yaw/pitch based on roll)")
        @DefaultBoolean(true)
        public boolean enabled = true;

        @Comment("Banking strength")
        @DefaultDouble(40.0)
        @RangeDouble(min = 0.0, max = 200.0)
        public double strength = 40.0;

        @Comment("Automatically roll back towards upright")
        @DefaultBoolean(false)
        public boolean automaticRighting = false;

        @Comment("Automatic righting strength")
        @DefaultDouble(50.0)
        @RangeDouble(min = 0.0, max = 200.0)
        public double rightingStrength = 50.0;
    }

    @Comment("Additional sensitivity multipliers for each axis")
    public static class Sensitivity {

        @Comment("Additional multiplier for pitch input")
        @DefaultDouble(1.0)
        @RangeDouble(min = 0.0, max = 5.0)
        public double pitch = 1.0;

        @Comment("Additional multiplier for yaw input")
        @DefaultDouble(0.4)
        @RangeDouble(min = 0.0, max = 5.0)
        public double yaw = 0.4;

        @Comment("Additional multiplier for roll input")
        @DefaultDouble(0.5)
        @RangeDouble(min = 0.0, max = 5.0)
        public double roll = 0.5;
    }

    @Comment("Debug section")
    public static class Debug {

        @Comment("Enable Debug Print Log")
        @DefaultBoolean(false)
        @RequiresMcRestart
        public boolean enableDebugMode = false;
    }
}
