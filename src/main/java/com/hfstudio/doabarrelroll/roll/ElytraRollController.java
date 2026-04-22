package com.hfstudio.doabarrelroll.roll;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

/**
 * Core elytra flight 3D rotation.
 * Uses Rodrigues' rotation formula to rotate facing/left vectors in local space,
 * then decomposes back to Euler angles (pitch, yaw, roll) and applies them to the player.
 */
public final class ElytraRollController {

    private ElytraRollController() {}

    public static void applyElytraLookDelta(EntityPlayerSP player, RollState state, double pitchInput, double yawInput,
        double rollInput) {
        if (player == null) {
            return;
        }

        float currentPitch = player.rotationPitch;
        float currentYaw = player.rotationYaw;
        float currentRoll = state.getRawRoll();

        Vec3 facing = player.getLookVec();
        Vec3 left = computeLeftVector(currentPitch, currentYaw, currentRoll);

        double pitchRad = Math.toRadians(-0.15 * pitchInput);
        double yawRad = Math.toRadians(0.15 * yawInput);
        double rollRad = Math.toRadians(0.15 * rollInput);

        // Rotate around the left axis = pitch
        facing = rotateAroundAxis(facing, left, pitchRad);

        // Rotate around the up axis = yaw
        Vec3 up = facing.crossProduct(left);
        facing = rotateAroundAxis(facing, up, yawRad);
        left = rotateAroundAxis(left, up, yawRad);

        // Rotate around the forward axis = roll
        left = rotateAroundAxis(left, facing, rollRad);

        facing = facing.normalize();
        left = left.normalize();

        // Decompose Euler angles from the rotated vectors
        double newPitch = -Math.asin(clampD(facing.yCoord, -1.0, 1.0)) * 180.0 / Math.PI;
        double newYawBase = -Math.atan2(facing.xCoord, facing.zCoord) * 180.0 / Math.PI;
        double newYaw = currentYaw + MathHelper.wrapAngleTo180_float((float) (newYawBase - currentYaw));

        // Compute roll: signed angle between current left and "neutral left"
        Vec3 normalLeft = rotateY(Vec3.createVectorHelper(1.0, 0.0, 0.0), Math.toRadians(-(newYawBase + 180.0)));
        double newRoll = -Math.atan2(
            left.crossProduct(normalLeft)
                .dotProduct(facing),
            left.dotProduct(normalLeft)) * 180.0 / Math.PI;

        double deltaPitch = newPitch - currentPitch;
        double deltaYaw = newYaw - currentYaw;
        double deltaRoll = newRoll - currentRoll;

        // Apply yaw and pitch via vanilla setAngles, preserving clamp and prevRotation consistency
        // setAngles internally: rotationYaw += yaw * 0.15, rotationPitch -= pitch * 0.15
        player.setAngles((float) (deltaYaw / 0.15), (float) (-deltaPitch / 0.15));

        // Apply roll, updating prev values similar to Entity#setAngles (avoids partial tick jitter)
        state.addRollDelta((float) deltaRoll);
        state.setRollBack(state.getRawRoll());
    }

    private static Vec3 computeLeftVector(float pitchDeg, float yawDeg, float rollDeg) {
        Vec3 left = Vec3.createVectorHelper(1.0, 0.0, 0.0);
        left = rotateZ(left, Math.toRadians(-rollDeg));
        left = rotateX(left, Math.toRadians(-pitchDeg));
        left = rotateY(left, Math.toRadians(-(yawDeg + 180.0)));
        return left.normalize();
    }

    /**
     * Rodrigues' rotation formula: rotate a vector around an arbitrary axis
     */
    private static Vec3 rotateAroundAxis(Vec3 v, Vec3 axisUnit, double angleRad) {
        double cos = Math.cos(angleRad);
        double sin = Math.sin(angleRad);
        Vec3 axis = axisUnit.normalize();

        Vec3 term1 = scale(v, cos);
        Vec3 term2 = scale(axis.crossProduct(v), sin);
        Vec3 term3 = scale(axis, axis.dotProduct(v) * (1.0 - cos));
        return add(add(term1, term2), term3);
    }

    private static Vec3 rotateX(Vec3 v, double angleRad) {
        double cos = Math.cos(angleRad);
        double sin = Math.sin(angleRad);
        double y = v.yCoord * cos - v.zCoord * sin;
        double z = v.yCoord * sin + v.zCoord * cos;
        return Vec3.createVectorHelper(v.xCoord, y, z);
    }

    private static Vec3 rotateY(Vec3 v, double angleRad) {
        double cos = Math.cos(angleRad);
        double sin = Math.sin(angleRad);
        double x = v.xCoord * cos + v.zCoord * sin;
        double z = -v.xCoord * sin + v.zCoord * cos;
        return Vec3.createVectorHelper(x, v.yCoord, z);
    }

    private static Vec3 rotateZ(Vec3 v, double angleRad) {
        double cos = Math.cos(angleRad);
        double sin = Math.sin(angleRad);
        double x = v.xCoord * cos - v.yCoord * sin;
        double y = v.xCoord * sin + v.yCoord * cos;
        return Vec3.createVectorHelper(x, y, v.zCoord);
    }

    private static Vec3 scale(Vec3 v, double s) {
        return Vec3.createVectorHelper(v.xCoord * s, v.yCoord * s, v.zCoord * s);
    }

    private static Vec3 add(Vec3 a, Vec3 b) {
        return Vec3.createVectorHelper(a.xCoord + b.xCoord, a.yCoord + b.yCoord, a.zCoord + b.zCoord);
    }

    private static double clampD(double value, double min, double max) {
        if (value < min) return min;
        if (value > max) return max;
        return value;
    }
}
