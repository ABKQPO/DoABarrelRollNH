package com.hfstudio.flightassistant.computer;

import com.hfstudio.flightassistant.FAConfig;

/**
 * Monitors proximity to the void.
 */
public class VoidProximityComputer extends Computer {

    private final AirDataComputer data;

    public boolean voidWarning = false;
    public boolean voidCaution = false;

    private static final double VOID_WARNING_ALTITUDE = 5.0;
    private static final double VOID_CAUTION_ALTITUDE = 20.0;

    public VoidProximityComputer(AirDataComputer data) {
        this.data = data;
    }

    @Override
    public void tick() {
        if (!data.isFlying()) {
            reset();
            return;
        }

        // Suppress when safety systems are disabled or player is invulnerable
        if (!FAConfig.safetyEnabled || data.isInvulnerable()) {
            voidWarning = false;
            voidCaution = false;
            return;
        }

        double distanceToVoid = data.getAltitude() - data.getVoidY();
        int mode = FAConfig.safety.voidAlertMode;

        voidWarning = distanceToVoid < VOID_WARNING_ALTITUDE && data.velocity.yCoord < 0
            && FAConfig.safety.isWarningEnabled(mode);
        voidCaution = !voidWarning && distanceToVoid < VOID_CAUTION_ALTITUDE
            && data.velocity.yCoord < 0
            && FAConfig.safety.isCautionEnabled(mode);
    }

    @Override
    public void reset() {
        voidWarning = false;
        voidCaution = false;
    }
}
