package com.hfstudio.flightassistant.computer;

/**
 * Manages roll state for the flight computer.
 */
public class RollComputer extends Computer {

    private final AirDataComputer data;
    private final AutoFlightComputer autoFlight;
    private final HeadingComputer heading;

    public float targetRoll = 0;

    public RollComputer(AirDataComputer data, AutoFlightComputer autoFlight, HeadingComputer heading) {
        this.data = data;
        this.autoFlight = autoFlight;
        this.heading = heading;
    }

    @Override
    public void tick() {
        if (!data.isFlying()) {
            targetRoll = 0;
            return;
        }

        // When autopilot is active with lateral mode, compute bank angle from heading error
        if (autoFlight.autopilot && autoFlight.activeLateralMode != null) {
            float headingError = heading.targetHeading - data.getHeading();
            // Normalize to [-180, 180]
            while (headingError > 180) headingError -= 360;
            while (headingError < -180) headingError += 360;

            // Bank proportional to heading error, max 30 degrees
            targetRoll = Math.max(-30.0f, Math.min(30.0f, headingError * 1.5f));
        } else {
            targetRoll = 0;
        }
    }

    @Override
    public void reset() {
        targetRoll = 0;
    }
}
