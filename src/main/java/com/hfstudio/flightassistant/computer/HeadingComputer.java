package com.hfstudio.flightassistant.computer;

/**
 * Manages heading state for the flight computer.
 */
public class HeadingComputer extends Computer {

    private final AirDataComputer data;
    private final AutoFlightComputer autoFlight;

    public float targetHeading = 0;

    public HeadingComputer(AirDataComputer data, AutoFlightComputer autoFlight) {
        this.data = data;
        this.autoFlight = autoFlight;
    }

    @Override
    public void tick() {
        if (!data.isFlying()) {
            return;
        }

        // When autopilot lateral mode is active, use its target heading
        if (autoFlight.autopilot && autoFlight.activeLateralMode != null) {
            targetHeading = autoFlight.activeLateralMode.getTargetHeading();
        } else {
            targetHeading = data.getHeading();
        }
    }

    @Override
    public void reset() {
        targetHeading = 0;
    }
}
