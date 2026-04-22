package com.hfstudio.doabarrelroll.roll;

import net.minecraft.util.MathHelper;

import com.hfstudio.doabarrelroll.config.ModConfig;

/**
 * Player render orientation computation.
 * Provides render rotation values (body yaw, flight pitch, visual roll) during elytra flight.
 */
public final class PlayerRenderOrientation {

    public enum ModelRollAxis {
        BODY_Y
    }

    private PlayerRenderOrientation() {}

    public static Orientation resolve(float prevBodyYaw, float bodyYaw, float prevViewYaw, float viewYaw,
        float prevViewPitch, float viewPitch, int ticksElytraFlying, float rawRoll, float partialTicks) {
        float interpolatedViewYaw = interpolateRotation(prevViewYaw, viewYaw, partialTicks);
        float interpolatedViewPitch = prevViewPitch + (viewPitch - prevViewPitch) * partialTicks;
        float flightTicks = ticksElytraFlying + partialTicks;
        float flightProgress = MathHelper.clamp_float(flightTicks * flightTicks / 100.0f, 0.0f, 1.0f);
        float visualRollRotation = resolveVisualRoll(rawRoll, 0.0f, true);

        return new Orientation(
            interpolatedViewYaw,
            interpolatedViewPitch,
            180.0f - interpolatedViewYaw,
            flightProgress * (-90.0f - interpolatedViewPitch),
            visualRollRotation,
            0.0f);
    }

    public static float resolveVisualRoll(float baseRoll, float barrelRoll, boolean includeBarrelRoll) {
        float visualRoll = baseRoll;
        if (includeBarrelRoll) {
            visualRoll += barrelRoll;
        }

        if (ModConfig.invertVisualRoll) {
            visualRoll = -visualRoll;
        }
        return visualRoll;
    }

    public static float resolveModelRollRotation(float visualRoll) {
        return -visualRoll;
    }

    public static ModelRollAxis getModelRollAxis() {
        return ModelRollAxis.BODY_Y;
    }

    static float interpolateRotation(float previousAngle, float angle, float partialTicks) {
        float delta = angle - previousAngle;

        while (delta < -180.0f) {
            delta += 360.0f;
        }
        while (delta >= 180.0f) {
            delta -= 360.0f;
        }

        return previousAngle + partialTicks * delta;
    }

    public static final class Orientation {

        private final float interpolatedViewYaw;
        private final float interpolatedViewPitch;
        private final float bodyYawRotation;
        private final float flightPitchRotation;
        private final float visualRollRotation;
        private final float motionYawCorrection;

        private Orientation(float interpolatedViewYaw, float interpolatedViewPitch, float bodyYawRotation,
            float flightPitchRotation, float visualRollRotation, float motionYawCorrection) {
            this.interpolatedViewYaw = interpolatedViewYaw;
            this.interpolatedViewPitch = interpolatedViewPitch;
            this.bodyYawRotation = bodyYawRotation;
            this.flightPitchRotation = flightPitchRotation;
            this.visualRollRotation = visualRollRotation;
            this.motionYawCorrection = motionYawCorrection;
        }

        public float getInterpolatedViewYaw() {
            return interpolatedViewYaw;
        }

        public float getInterpolatedViewPitch() {
            return interpolatedViewPitch;
        }

        public float getBodyYawRotation() {
            return bodyYawRotation;
        }

        public float getFlightPitchRotation() {
            return flightPitchRotation;
        }

        public float getVisualRollRotation() {
            return visualRollRotation;
        }

        public float getMotionYawCorrection() {
            return motionYawCorrection;
        }
    }
}
