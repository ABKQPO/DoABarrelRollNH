package com.hfstudio.flightassistant.computer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.hfstudio.flightassistant.FAConfig;
import com.hfstudio.flightassistant.alert.Alert;
import com.hfstudio.flightassistant.alert.Alert.AlertLevel;
import com.hfstudio.flightassistant.alert.AlertCategory;

/**
 * Manages all flight alerts from various computers.
 */
public class AlertComputer extends Computer {

    private final AirDataComputer data;
    private final StallComputer stall;
    private final GroundProximityComputer gpws;
    private final VoidProximityComputer voidProx;
    private final ElytraStatusComputer elytraStatus;
    private final ChunkStatusComputer chunkStatus;
    private final FireworkComputer firework;
    private final FlightProtectionsComputer protections;
    private final AutoFlightComputer autoFlight;
    private final FlightPlanComputer flightPlan;

    // Alerts
    public final Alert stallWarning = new Alert(AlertCategory.STALL, AlertLevel.WARNING, "stall_warning", "STALL");
    public final Alert stallCaution = new Alert(AlertCategory.STALL, AlertLevel.CAUTION, "stall_caution", "STALL");
    public final Alert gpwsSinkRate = new Alert(AlertCategory.GPWS, AlertLevel.WARNING, "gpws_sink_rate", "SINK RATE");
    public final Alert gpwsSinkRateCaution = new Alert(
        AlertCategory.GPWS,
        AlertLevel.CAUTION,
        "gpws_sink_rate_caution",
        "SINK RATE");
    public final Alert gpwsTerrain = new Alert(AlertCategory.GPWS, AlertLevel.WARNING, "gpws_terrain", "TERRAIN");
    public final Alert gpwsTerrainCaution = new Alert(
        AlertCategory.GPWS,
        AlertLevel.CAUTION,
        "gpws_terrain_caution",
        "TERRAIN");
    public final Alert voidWarning = new Alert(AlertCategory.GPWS, AlertLevel.WARNING, "void_warning", "VOID");
    public final Alert voidCaution = new Alert(AlertCategory.GPWS, AlertLevel.CAUTION, "void_caution", "VOID");
    public final Alert elytraDurabilityWarning = new Alert(
        AlertCategory.ELYTRA,
        AlertLevel.WARNING,
        "elytra_durability_warn",
        "ELYTRA LOW");
    public final Alert elytraDurabilityCaution = new Alert(
        AlertCategory.ELYTRA,
        AlertLevel.CAUTION,
        "elytra_durability_caut",
        "ELYTRA LOW");
    public final Alert chunkUnloaded = new Alert(
        AlertCategory.NAV,
        AlertLevel.CAUTION,
        "chunk_unloaded",
        "NAV ACCURACY");
    public final Alert protectionActive = new Alert(
        AlertCategory.F_CTL,
        AlertLevel.CAUTION,
        "protection_active",
        "F/CTL PROT");
    public final Alert protectionLost = new Alert(
        AlertCategory.F_CTL,
        AlertLevel.WARNING,
        "protection_lost",
        "F/CTL PROT LOST");
    public final Alert fireworkExplosive = new Alert(
        AlertCategory.ELYTRA,
        AlertLevel.CAUTION,
        "firework_explosive",
        "FIREWORK EXPLOSIVE");
    public final Alert autopilotDisconnect = new Alert(
        AlertCategory.AUTO_FLT,
        AlertLevel.CAUTION,
        "ap_disconnect",
        "AP DISCONNECT");
    public final Alert autoThrustDisconnect = new Alert(
        AlertCategory.AUTO_FLT,
        AlertLevel.CAUTION,
        "athr_disconnect",
        "A/THR OFF");
    public final Alert altitudeLoss = new Alert(AlertCategory.GPWS, AlertLevel.WARNING, "altitude_loss", "ALT LOSS");
    public final Alert belowGlideSlope = new Alert(
        AlertCategory.F_PLAN,
        AlertLevel.CAUTION,
        "below_glide_slope",
        "GLIDE SLOPE");

    private final Alert[] allAlerts;
    public final List<Alert> activeAlerts = new ArrayList<>();

    // Alert sound cooldown
    private int alertSoundCooldown = 0;
    private static final int ALERT_SOUND_INTERVAL = 40; // 2 seconds

    public AlertComputer(AirDataComputer data, StallComputer stall, GroundProximityComputer gpws,
        VoidProximityComputer voidProx, ElytraStatusComputer elytraStatus, ChunkStatusComputer chunkStatus,
        FireworkComputer firework, FlightProtectionsComputer protections, AutoFlightComputer autoFlight,
        FlightPlanComputer flightPlan) {
        this.data = data;
        this.stall = stall;
        this.gpws = gpws;
        this.voidProx = voidProx;
        this.elytraStatus = elytraStatus;
        this.chunkStatus = chunkStatus;
        this.firework = firework;
        this.protections = protections;
        this.autoFlight = autoFlight;
        this.flightPlan = flightPlan;

        allAlerts = new Alert[] { stallWarning, stallCaution, gpwsSinkRate, gpwsSinkRateCaution, gpwsTerrain,
            gpwsTerrainCaution, voidWarning, voidCaution, elytraDurabilityWarning, elytraDurabilityCaution,
            chunkUnloaded, protectionActive, protectionLost, fireworkExplosive, autopilotDisconnect,
            autoThrustDisconnect, altitudeLoss, belowGlideSlope };
    }

    @Override
    public void tick() {
        if (!data.isFlying()) {
            reset();
            return;
        }

        // Update alert states from computers
        stallWarning.setActive(stall.stallWarning);
        stallCaution.setActive(stall.stallCaution);

        gpwsSinkRate.setActive(gpws.sinkRateWarning);
        gpwsSinkRateCaution.setActive(gpws.sinkRateCaution);
        gpwsTerrain.setActive(gpws.terrainWarning);
        gpwsTerrainCaution.setActive(gpws.terrainCaution);

        voidWarning.setActive(voidProx.voidWarning);
        voidCaution.setActive(voidProx.voidCaution);

        elytraDurabilityWarning.setActive(elytraStatus.durabilityWarning);
        elytraDurabilityCaution.setActive(elytraStatus.durabilityCaution);

        chunkUnloaded.setActive(chunkStatus.chunksUnloaded);
        protectionActive.setActive(protections.protectionActive);
        protectionLost.setActive(protections.protectionsLost);

        // Firework explosive detection alert
        fireworkExplosive.setActive(firework.explosiveDetected && FAConfig.safety.fireworkExplosiveAlert);

        // Autopilot disconnect alert
        autopilotDisconnect.setActive(autoFlight.autopilotAlert);
        autoThrustDisconnect.setActive(autoFlight.autoThrustAlert);

        // Altitude loss alert
        altitudeLoss.setActive(data.significantAltitudeLoss && FAConfig.safety.altitudeLossAlert);

        // Below glide slope alert
        belowGlideSlope.setActive(flightPlan.belowGlideSlope);

        // Build sorted active alerts list (warnings first, then cautions, then advisories)
        activeAlerts.clear();
        for (Alert alert : allAlerts) {
            if (alert.active) {
                activeAlerts.add(alert);
            }
        }
        activeAlerts.sort(Comparator.comparingInt(a -> a.level.ordinal()));

        // Play alert sound based on alertVolume
        if (alertSoundCooldown > 0) {
            alertSoundCooldown--;
        }
        if (hasWarnings() && alertSoundCooldown <= 0 && FAConfig.safety.alertVolume > 0.0f) {
            net.minecraft.client.entity.EntityPlayerSP player = data.getPlayer();
            if (player != null) {
                player.playSound("random.orb", FAConfig.safety.alertVolume * 0.5f, 0.5f);
                alertSoundCooldown = ALERT_SOUND_INTERVAL;
            }
        }
    }

    @Override
    public void reset() {
        for (Alert alert : allAlerts) {
            alert.setActive(false);
        }
        activeAlerts.clear();
    }

    public boolean hasWarnings() {
        for (Alert alert : activeAlerts) {
            if (alert.level == AlertLevel.WARNING) return true;
        }
        return false;
    }

    public boolean hasCautions() {
        for (Alert alert : activeAlerts) {
            if (alert.level == AlertLevel.CAUTION) return true;
        }
        return false;
    }
}
