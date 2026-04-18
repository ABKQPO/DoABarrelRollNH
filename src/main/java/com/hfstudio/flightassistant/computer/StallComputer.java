package com.hfstudio.flightassistant.computer;

import com.hfstudio.flightassistant.FAConfig;

/**
 * Detects stall conditions during elytra flight.
 * A stall occurs when forward velocity drops too low while airborne.
 */
public class StallComputer extends Computer {

    private final AirDataComputer data;

    public boolean isStalling = false;
    public boolean stallWarning = false;
    public boolean stallCaution = false;

    private static final double STALL_SPEED_THRESHOLD = 0.5; // blocks/tick

    public StallComputer(AirDataComputer data) {
        this.data = data;
    }

    @Override
    public void tick() {
        if (!data.isFlying()) {
            isStalling = false;
            stallWarning = false;
            stallCaution = false;
            return;
        }

        // Suppress when safety systems are disabled or player is invulnerable
        if (!FAConfig.safetyEnabled || data.isInvulnerable()) {
            isStalling = false;
            stallWarning = false;
            stallCaution = false;
            return;
        }

        double forwardSpeed = data.forwardVelocity.lengthVector();
        double verticalSpeed = data.velocity.yCoord;

        // Stall when forward speed is very low and descending
        isStalling = forwardSpeed < STALL_SPEED_THRESHOLD && verticalSpeed < -0.1;

        int alertMode = FAConfig.safety.stallAlertMode;
        stallWarning = isStalling && FAConfig.safety.isWarningEnabled(alertMode);
        stallCaution = !isStalling && forwardSpeed < STALL_SPEED_THRESHOLD * 1.5
            && verticalSpeed < 0
            && FAConfig.safety.isCautionEnabled(alertMode);
    }

    @Override
    public void reset() {
        isStalling = false;
        stallWarning = false;
        stallCaution = false;
    }
}
