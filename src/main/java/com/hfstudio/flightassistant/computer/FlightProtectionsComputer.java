package com.hfstudio.flightassistant.computer;

import net.minecraft.client.entity.EntityPlayerSP;

import com.hfstudio.flightassistant.FAConfig;

/**
 * Manages flight protection systems that limit control inputs.
 * Coordinates stall, void, and GPWS protections.
 * Handles both pitch limits (visual) and auto-pitch (active correction).
 */
public class FlightProtectionsComputer extends Computer {

    private final AirDataComputer data;
    private final StallComputer stall;
    private final VoidProximityComputer voidProx;
    private final GroundProximityComputer gpws;

    public Float minimumPitch = null;
    public Float maximumPitch = null;
    public boolean protectionActive = false;
    public boolean protectionsLost = false;

    // Auto-pitch correction rate (degrees per tick)
    private static final float AUTO_PITCH_RATE = 1.5f;

    public FlightProtectionsComputer(AirDataComputer data, StallComputer stall, VoidProximityComputer voidProx,
        GroundProximityComputer gpws) {
        this.data = data;
        this.stall = stall;
        this.voidProx = voidProx;
        this.gpws = gpws;
    }

    @Override
    public void tick() {
        if (!data.isFlying()) {
            reset();
            return;
        }

        // Check if protections are lost due to faulted computers
        protectionsLost = data.isDisabledOrFaulted();

        minimumPitch = null;
        maximumPitch = null;
        protectionActive = false;

        // Skip protections when safety is disabled
        if (!FAConfig.safetyEnabled) return;

        EntityPlayerSP player = data.getPlayer();
        if (player == null) return;

        // Stall protection: limit maximum pitch (nose up) to push nose down
        if (stall.isStalling && FAConfig.safety.stallLimitPitch) {
            maximumPitch = data.getPitch() - 5.0f;
            protectionActive = true;
        }

        // Void protection: limit minimum pitch (force nose up near void)
        if (voidProx.voidWarning && FAConfig.safety.voidLimitPitch) {
            float limit = 0.0f;
            if (minimumPitch == null || limit > minimumPitch) {
                minimumPitch = limit;
            }
            protectionActive = true;
        }

        // GPWS: limit minimum pitch near ground (force slight climb)
        if (gpws.terrainWarning && FAConfig.safety.sinkRateLimitPitch) {
            float limit = 5.0f;
            if (minimumPitch == null || limit > minimumPitch) {
                minimumPitch = limit;
            }
            protectionActive = true;
        }

        // Auto-pitch corrections (actively change player pitch)
        if (data.automationsAllowed(true)) {
            applyAutoPitch(player);
        }
    }

    /**
     * Apply automatic pitch corrections to the player.
     * This actively modifies the player's rotation to recover from dangerous situations.
     */
    private void applyAutoPitch(EntityPlayerSP player) {
        Float targetPitch = null;

        // Void auto-pitch: force nose up
        if (voidProx.voidWarning && FAConfig.safety.voidAutoPitch) {
            targetPitch = -10.0f; // Nose up 10 degrees (MC pitch: negative = up)
            protectionActive = true;
        }

        // Sink rate auto-pitch: force nose up to reduce descent
        if (gpws.sinkRateWarning && FAConfig.safety.sinkRateAutoPitch) {
            float limit = -5.0f; // Nose up 5 degrees
            if (targetPitch == null || limit < targetPitch) {
                targetPitch = limit;
            }
            protectionActive = true;
        }

        // Obstacle auto-pitch: force climb
        if (gpws.terrainWarning && FAConfig.safety.obstacleAutoPitch) {
            float limit = -15.0f; // Strong nose up
            if (targetPitch == null || limit < targetPitch) {
                targetPitch = limit;
            }
            protectionActive = true;
        }

        // Apply gradual pitch correction toward target
        if (targetPitch != null && player.rotationPitch > targetPitch) {
            float delta = Math.min(AUTO_PITCH_RATE, player.rotationPitch - targetPitch);
            player.rotationPitch -= delta;
        }
    }

    @Override
    public void reset() {
        minimumPitch = null;
        maximumPitch = null;
        protectionActive = false;
        protectionsLost = false;
    }
}
