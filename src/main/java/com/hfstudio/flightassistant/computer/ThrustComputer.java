package com.hfstudio.flightassistant.computer;

import com.hfstudio.flightassistant.FAConfig;

/**
 * Manages thrust (firework) scheduling for the flight computer.
 * Coordinates auto-thrust from safety systems (stall, void, sink rate, obstacle).
 */
public class ThrustComputer extends Computer {

    private final AirDataComputer data;
    private final AutoFlightComputer autoFlight;
    private final FireworkComputer firework;
    private final StallComputer stall;
    private final VoidProximityComputer voidProx;
    private final GroundProximityComputer gpws;

    // Current thrust target (0.0 = idle, 1.0 = full thrust)
    public float currentThrust = 0.0f;
    public boolean thrustDemand = false;
    public String thrustSource = null;
    public boolean noThrustSource = false;
    public boolean thrustLocked = false;

    private int thrustCooldown = 0;
    private static final int THRUST_COOLDOWN_TICKS = 30; // ~1.5 seconds between auto-uses

    public ThrustComputer(AirDataComputer data, AutoFlightComputer autoFlight, FireworkComputer firework,
        StallComputer stall, VoidProximityComputer voidProx, GroundProximityComputer gpws) {
        this.data = data;
        this.autoFlight = autoFlight;
        this.firework = firework;
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

        if (thrustCooldown > 0) {
            thrustCooldown--;
        }

        thrustDemand = false;
        thrustSource = null;
        thrustLocked = false;

        // Skip safety auto-thrust when safety is disabled
        if (!FAConfig.safetyEnabled) {
            // Only check A/THR mode (not safety-related)
            if (autoFlight.autoThrust && autoFlight.activeThrustMode != null) {
                int targetSpeed = autoFlight.activeThrustMode.getTargetSpeed();
                double currentSpeed = data.forwardVelocity.lengthVector() * 20.0;
                if (currentSpeed < targetSpeed * 0.9) {
                    thrustDemand = true;
                    thrustSource = "A/THR";
                }
            }
            applyThrust();
            return;
        }

        // Safety auto-thrust: stall recovery
        if (stall.isStalling && FAConfig.safety.stallAutoThrust) {
            thrustDemand = true;
            thrustSource = "STALL";
        }

        // Safety auto-thrust: void proximity recovery
        if (voidProx.voidWarning && FAConfig.safety.voidAutoThrust) {
            thrustDemand = true;
            thrustSource = "VOID";
        }

        // Safety auto-thrust: excessive sink rate
        if (gpws.sinkRateWarning && FAConfig.safety.sinkRateAutoThrust) {
            thrustDemand = true;
            thrustSource = "SINK RATE";
        }

        // Safety auto-thrust: terrain proximity
        if (gpws.terrainWarning && FAConfig.safety.obstacleAutoThrust) {
            thrustDemand = true;
            thrustSource = "TERRAIN";
        }

        // Auto-thrust from autopilot (A/THR engaged)
        if (autoFlight.autoThrust && autoFlight.activeThrustMode != null) {
            int targetSpeed = autoFlight.activeThrustMode.getTargetSpeed();
            double currentSpeed = data.forwardVelocity.lengthVector() * 20.0;
            if (currentSpeed < targetSpeed * 0.9) {
                thrustDemand = true;
                thrustSource = "A/THR";
            }
        }

        // Compute thrust level
        currentThrust = thrustDemand ? 1.0f : 0.0f;

        // Check if fireworks are locked near obstacles
        if (firework.explosiveDetected && FAConfig.safety.fireworkLockObstacles && gpws.radarAltitude < 30) {
            thrustLocked = true;
        }

        applyThrust();
    }

    private void applyThrust() {
        currentThrust = thrustDemand ? 1.0f : 0.0f;
        noThrustSource = thrustDemand && !firework.hasFireworks;

        // Apply thrust: use firework if demanded and available
        if (thrustDemand && !thrustLocked
            && firework.hasFireworks
            && !firework.activeBoosting
            && thrustCooldown <= 0
            && data.automationsAllowed(true)) {
            firework.requestFireworkUse();
            thrustCooldown = THRUST_COOLDOWN_TICKS;
        }
    }

    @Override
    public void reset() {
        currentThrust = 0.0f;
        thrustDemand = false;
        thrustSource = null;
        noThrustSource = false;
        thrustLocked = false;
        thrustCooldown = 0;
    }
}
