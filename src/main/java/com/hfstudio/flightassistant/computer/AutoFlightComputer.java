package com.hfstudio.flightassistant.computer;

/**
 * Autopilot computer.
 * Manages AP (Autopilot), FD (Flight Directors), and A/THR (Auto-thrust) modes.
 */
public class AutoFlightComputer extends Computer {

    private final AirDataComputer data;

    // Autopilot state
    public boolean autopilot = false;
    public boolean flightDirectors = false;
    public boolean autoThrust = false;
    public boolean autopilotAlert = false;
    public boolean autoThrustAlert = false;

    // Resistance tracking (disconnect AP on excessive manual override)
    private float pitchResistance = 0.0f;
    private float headingResistance = 0.0f;
    private static final float PITCH_DISCONNECT_THRESHOLD = 20.0f;
    private static final float HEADING_DISCONNECT_THRESHOLD = 40.0f;

    // Active modes
    public VerticalMode activeVerticalMode = null;
    public VerticalMode selectedVerticalMode = null;
    public LateralMode activeLateralMode = null;
    public LateralMode selectedLateralMode = null;
    public ThrustMode activeThrustMode = null;
    public ThrustMode selectedThrustMode = null;

    // Targets
    public int selectedAltitude = 100;
    public int selectedSpeed = 30;
    public int selectedHeading = 0;
    public float targetPitch = 0;

    public AutoFlightComputer(AirDataComputer data) {
        this.data = data;
    }

    @Override
    public void tick() {
        if (!data.isFlying() || !data.automationsAllowed(true)) {
            if (autopilot || flightDirectors || autoThrust) {
                // Disconnect all modes when not flying
                if (autopilot) {
                    autopilotAlert = true;
                }
                if (autoThrust) {
                    autoThrustAlert = true;
                }
                autopilot = false;
                flightDirectors = false;
                autoThrust = false;
            }
            return;
        }

        // Decay pitch/heading resistance over time
        pitchResistance = Math.max(0.0f, pitchResistance - 0.5f);
        headingResistance = Math.max(0.0f, headingResistance - 1.0f);

        // Apply autopilot pitch hold
        if (autopilot && activeVerticalMode != null) {
            targetPitch = activeVerticalMode.getTargetPitch();
        }

        // Apply autopilot heading hold
        if (autopilot && activeLateralMode != null) {
            float target = activeLateralMode.getTargetHeading();
            // Gradual heading change through yaw adjustment
            float currentYaw = data.getYaw();
            float targetYaw = target - 180.0f;
            float diff = targetYaw - currentYaw;
            // Normalize to [-180, 180]
            while (diff > 180) diff -= 360;
            while (diff < -180) diff += 360;

            net.minecraft.client.entity.EntityPlayerSP player = data.getPlayer();
            if (player != null && Math.abs(diff) > 0.5f) {
                float correction = Math.max(-2.0f, Math.min(2.0f, diff * 0.1f));
                player.rotationYaw += correction;
            }
        }
    }

    @Override
    public void reset() {
        if (autopilot) {
            autopilotAlert = true;
        }
        if (autoThrust) {
            autoThrustAlert = true;
        }
        autopilot = false;
        flightDirectors = false;
        autoThrust = false;
        activeVerticalMode = null;
        selectedVerticalMode = null;
        activeLateralMode = null;
        selectedLateralMode = null;
        activeThrustMode = null;
        selectedThrustMode = null;
        pitchResistance = 0.0f;
        headingResistance = 0.0f;
    }

    /**
     * Toggle autopilot on/off.
     */
    public void toggleAP() {
        if (autopilot) {
            autopilot = false;
            autopilotAlert = false;
        } else {
            autopilot = true;
            flightDirectors = true;
            autopilotAlert = false;
        }
    }

    /**
     * Toggle flight directors on/off.
     */
    public void toggleFD() {
        flightDirectors = !flightDirectors;
    }

    /**
     * Toggle auto-thrust on/off.
     */
    public void toggleATHR() {
        if (autoThrust) {
            autoThrust = false;
            autoThrustAlert = false;
        } else {
            autoThrust = true;
            autoThrustAlert = false;
        }
    }

    /**
     * Register manual pitch input (for resistance tracking).
     */
    public void registerPitchInput(float pitchDelta) {
        if (autopilot) {
            pitchResistance += Math.abs(pitchDelta);
            if (pitchResistance > PITCH_DISCONNECT_THRESHOLD) {
                autopilot = false;
                autopilotAlert = true;
                pitchResistance = 0.0f;
            }
        }
    }

    /**
     * Register manual heading input (for resistance tracking).
     */
    public void registerHeadingInput(float headingDelta) {
        if (autopilot) {
            headingResistance += Math.abs(headingDelta);
            if (headingResistance > HEADING_DISCONNECT_THRESHOLD) {
                autopilot = false;
                autopilotAlert = true;
                headingResistance = 0.0f;
            }
        }
    }

    // Mode interfaces for type checking
    public interface VerticalMode {

        float getTargetPitch();

        String getModeName();
    }

    public interface LateralMode {

        float getTargetHeading();

        String getModeName();
    }

    public interface ThrustMode {

        int getTargetSpeed();

        String getModeName();
    }

    public interface FollowsPitchMode extends VerticalMode {

        float getTargetPitch();
    }

    public interface FollowsAltitudeMode extends VerticalMode {

        int getTargetAltitude();
    }

    public interface FollowsSpeedMode {

        int getTargetSpeed();
    }
}
