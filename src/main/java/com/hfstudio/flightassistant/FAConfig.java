package com.hfstudio.flightassistant;

import com.gtnewhorizon.gtnhlib.config.Config;
import com.gtnewhorizon.gtnhlib.config.Config.Comment;
import com.gtnewhorizon.gtnhlib.config.Config.DefaultBoolean;
import com.gtnewhorizon.gtnhlib.config.Config.DefaultFloat;
import com.gtnewhorizon.gtnhlib.config.Config.DefaultInt;
import com.gtnewhorizon.gtnhlib.config.Config.RangeFloat;
import com.gtnewhorizon.gtnhlib.config.Config.RangeInt;
import com.gtnewhorizon.gtnhlib.config.Config.Sync;
import com.gtnewhorizon.gtnhlib.config.ConfigException;
import com.gtnewhorizon.gtnhlib.config.ConfigurationManager;
import com.hfstudio.DoABarrelRollNH;

/**
 * FlightAssistant configuration.
 * Merged from GlobalOptions, DisplayOptions, and SafetyOptions.
 */
@Config(
    modid = DoABarrelRollNH.MODID,
    category = "fa",
    filename = "flightassistant",
    configSubDirectory = "DoABarrelRoll")
@Config.LangKeyPattern(pattern = "dabr.gui.config.%cat.%field", fullyQualified = true)
@Comment("FlightAssistant - Aviation HUD and safety systems configuration")
public class FAConfig {

    public static void registerConfig() throws ConfigException {
        ConfigurationManager.registerConfig(FAConfig.class);
    }

    // ========== Global Options ==========
    @Comment("Enable or disable FlightAssistant module")
    @DefaultBoolean(true)
    public static boolean enabled = true;

    @Comment("Enable HUD display")
    @DefaultBoolean(true)
    public static boolean hudEnabled = true;

    @Comment("Enable safety systems (alerts, protections)")
    @DefaultBoolean(true)
    public static boolean safetyEnabled = true;

    @Comment("Allow automations (autopilot, auto-thrust) while overlays are open")
    @DefaultBoolean(false)
    public static boolean automationsAllowedInOverlays = false;

    // ========== Display Options ==========
    public static final DisplayConfig display = new DisplayConfig();

    @Comment("Display configuration for FlightAssistant HUD")
    public static class DisplayConfig {

        @Comment("HUD frame width as fraction of screen width (0.1-1.0)")
        @DefaultFloat(0.6f)
        @RangeFloat(min = 0.1f, max = 1.0f)
        public float frameWidth = 0.6f;

        @Comment("HUD frame height as fraction of screen height (0.1-1.0)")
        @DefaultFloat(0.5f)
        @RangeFloat(min = 0.1f, max = 1.0f)
        public float frameHeight = 0.5f;

        // Colors stored as packed RGB ints
        @Comment("Primary HUD color (RGB hex, e.g. 0x00FF00 for green)")
        @DefaultInt(0x00FF00)
        public int primaryColor = 0x00FF00;

        @Comment("Secondary HUD color (RGB hex)")
        @DefaultInt(0xFFFFFF)
        public int secondaryColor = 0xFFFFFF;

        @Comment("Primary advisory color (RGB hex)")
        @DefaultInt(0x00FFFF)
        public int primaryAdvisoryColor = 0x00FFFF;

        @Comment("Secondary advisory color (RGB hex)")
        @DefaultInt(0xFF00FF)
        public int secondaryAdvisoryColor = 0xFF00FF;

        @Comment("Caution color (RGB hex)")
        @DefaultInt(0xFFFF00)
        public int cautionColor = 0xFFFF00;

        @Comment("Warning color (RGB hex)")
        @DefaultInt(0xFF0000)
        public int warningColor = 0xFF0000;

        // Attitude display
        @Comment("Attitude display mode: 0=DISABLED, 1=HORIZON_ONLY, 2=HORIZON_AND_LADDER")
        @DefaultInt(2)
        @RangeInt(min = 0, max = 2)
        public int showAttitude = 2;

        @Comment("Pitch ladder degree step")
        @DefaultInt(15)
        @RangeInt(min = 5, max = 45)
        public int attitudeDegreeStep = 15;

        @Comment("Draw pitch bars outside the HUD frame")
        @DefaultBoolean(true)
        public boolean drawPitchOutsideFrame = true;

        // Heading
        @Comment("Show heading reading (numeric)")
        @DefaultBoolean(true)
        public boolean showHeadingReading = true;

        @Comment("Show heading scale (tape)")
        @DefaultBoolean(true)
        public boolean showHeadingScale = true;

        // Speed
        @Comment("Show speed reading (numeric)")
        @DefaultBoolean(true)
        public boolean showSpeedReading = true;

        @Comment("Show speed scale (tape)")
        @DefaultBoolean(true)
        public boolean showSpeedScale = true;

        // Altitude
        @Comment("Show altitude reading (numeric)")
        @DefaultBoolean(true)
        public boolean showAltitudeReading = true;

        @Comment("Show altitude scale (tape)")
        @DefaultBoolean(true)
        public boolean showAltitudeScale = true;

        @Comment("Show radar altitude")
        @DefaultBoolean(true)
        public boolean showRadarAltitude = true;

        // Other displays
        @Comment("Show flight path vector")
        @DefaultBoolean(true)
        public boolean showFlightPathVector = true;

        @Comment("Show elytra durability")
        @DefaultBoolean(true)
        public boolean showElytraDurability = true;

        @Comment("Durability units: 0=RAW, 1=PERCENTAGE, 2=TIME")
        @DefaultInt(2)
        @RangeInt(min = 0, max = 2)
        public int elytraDurabilityUnits = 2;

        @Comment("Show coordinates")
        @DefaultBoolean(true)
        public boolean showCoordinates = true;

        @Comment("Show ground speed")
        @DefaultBoolean(true)
        public boolean showGroundSpeed = true;

        @Comment("Show vertical speed indicator")
        @DefaultBoolean(true)
        public boolean showVerticalSpeed = true;

        @Comment("Show alerts")
        @DefaultBoolean(true)
        public boolean showAlerts = true;

        @Comment("Show status messages")
        @DefaultBoolean(true)
        public boolean showStatusMessages = true;

        @Comment("Show automation modes (AP, FD, A/THR)")
        @DefaultBoolean(true)
        public boolean showAutomationModes = true;

        @Comment("Show flight directors")
        @DefaultBoolean(true)
        public boolean showFlightDirectors = true;

        @Comment("Show course deviation")
        @DefaultBoolean(true)
        public boolean showCourseDeviation = true;

        public int getPrimaryColorAlpha() {
            return primaryColor | (255 << 24);
        }

        public int getSecondaryColorAlpha() {
            return secondaryColor | (255 << 24);
        }

        public int getPrimaryAdvisoryColorAlpha() {
            return primaryAdvisoryColor | (255 << 24);
        }

        public int getSecondaryAdvisoryColorAlpha() {
            return secondaryAdvisoryColor | (255 << 24);
        }

        public int getCautionColorAlpha() {
            return cautionColor | (255 << 24);
        }

        public int getWarningColorAlpha() {
            return warningColor | (255 << 24);
        }
    }

    // ========== Safety Options ==========
    public static final SafetyConfig safety = new SafetyConfig();

    @Comment("Safety systems configuration")
    public static class SafetyConfig {

        @Comment("Alert volume (0.0-1.0)")
        @DefaultFloat(1.0f)
        @RangeFloat(min = 0.0f, max = 1.0f)
        public float alertVolume = 1.0f;

        @Comment("Consider player invulnerability for safety alerts")
        @DefaultBoolean(true)
        public boolean considerInvulnerability = true;

        // Elytra
        @Comment("Elytra durability alert mode: 0=DISABLED, 1=CAUTION, 2=WARNING, 3=WARNING_AND_CAUTION")
        @DefaultInt(3)
        @RangeInt(min = 0, max = 3)
        public int elytraDurabilityAlertMode = 3;

        @Comment("Auto-open elytra when falling")
        @DefaultBoolean(true)
        public boolean elytraAutoOpen = true;

        // Stall
        @Comment("Stall alert mode: 0=DISABLED, 1=CAUTION, 2=WARNING, 3=WARNING_AND_CAUTION")
        @DefaultInt(3)
        @RangeInt(min = 0, max = 3)
        public int stallAlertMode = 3;

        @Comment("Limit pitch to prevent stall")
        @DefaultBoolean(true)
        public boolean stallLimitPitch = true;

        @Comment("Auto-thrust to recover from stall")
        @DefaultBoolean(true)
        public boolean stallAutoThrust = true;

        // Void proximity
        @Comment("Void proximity alert mode: 0=DISABLED, 1=CAUTION, 2=WARNING, 3=WARNING_AND_CAUTION")
        @DefaultInt(3)
        @RangeInt(min = 0, max = 3)
        public int voidAlertMode = 3;

        @Comment("Limit pitch near void")
        @DefaultBoolean(true)
        public boolean voidLimitPitch = true;

        @Comment("Auto-thrust near void")
        @DefaultBoolean(true)
        public boolean voidAutoThrust = true;

        @Comment("Auto-pitch near void")
        @DefaultBoolean(true)
        public boolean voidAutoPitch = true;

        // GPWS - Sink rate
        @Comment("Sink rate alert mode: 0=DISABLED, 1=CAUTION, 2=WARNING, 3=WARNING_AND_CAUTION")
        @DefaultInt(3)
        @RangeInt(min = 0, max = 3)
        public int sinkRateAlertMode = 3;

        @Comment("Limit pitch on excessive sink rate")
        @DefaultBoolean(true)
        public boolean sinkRateLimitPitch = true;

        @Comment("Auto-thrust on excessive sink rate")
        @DefaultBoolean(true)
        public boolean sinkRateAutoThrust = true;

        @Comment("Auto-pitch on excessive sink rate")
        @DefaultBoolean(true)
        public boolean sinkRateAutoPitch = true;

        // GPWS - Obstacle
        @Comment("Obstacle alert mode: 0=DISABLED, 1=CAUTION, 2=WARNING, 3=WARNING_AND_CAUTION")
        @DefaultInt(3)
        @RangeInt(min = 0, max = 3)
        public int obstacleAlertMode = 3;

        @Comment("Auto-thrust for obstacle avoidance")
        @DefaultBoolean(true)
        public boolean obstacleAutoThrust = true;

        @Comment("Auto-pitch for obstacle avoidance")
        @DefaultBoolean(true)
        public boolean obstacleAutoPitch = true;

        // Other alerts
        @Comment("Altitude loss alert")
        @DefaultBoolean(true)
        public boolean altitudeLossAlert = true;

        @Comment("Below glide slope alert mode: 0=DISABLED, 1=CAUTION, 2=WARNING, 3=WARNING_AND_CAUTION")
        @DefaultInt(3)
        @RangeInt(min = 0, max = 3)
        public int belowGlideSlopeAlertMode = 3;

        @Comment("Alert for explosive fireworks")
        @DefaultBoolean(true)
        public boolean fireworkExplosiveAlert = true;

        @Comment("Lock hotbar when explosive firework detected")
        @DefaultBoolean(true)
        public boolean fireworkLockExplosive = true;

        @Comment("Lock firework use near obstacles")
        @DefaultBoolean(true)
        public boolean fireworkLockObstacles = true;

        @Comment("Allow throwing primed TNT while elytra flying by right-clicking TNT")
        @DefaultBoolean(false)
        @Sync(true)
        public boolean throwTntEnabled = false;

        public boolean isWarningEnabled(int alertMode) {
            return alertMode == 2 || alertMode == 3;
        }

        public boolean isCautionEnabled(int alertMode) {
            return alertMode == 1 || alertMode == 3;
        }
    }
}
