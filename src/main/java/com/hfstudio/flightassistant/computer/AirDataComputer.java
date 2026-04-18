package com.hfstudio.flightassistant.computer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

import com.hfstudio.flightassistant.FAConfig;

import ganymedes01.etfuturum.api.elytra.IElytraPlayer;

/**
 * Core air data computer.
 * Computes speed, altitude, pitch, yaw, heading, velocity, etc.
 */
public class AirDataComputer extends Computer {

    private final Minecraft mc;

    // Current data
    public Vec3 velocity = Vec3.createVectorHelper(0, 0, 0);
    public Vec3 velocityPerSecond = Vec3.createVectorHelper(0, 0, 0);
    public Vec3 forwardVelocity = Vec3.createVectorHelper(0, 0, 0);
    public Vec3 forwardVelocityPerSecond = Vec3.createVectorHelper(0, 0, 0);
    public double forwardAcceleration = 0.0;

    // Altitude loss tracking
    public double maxAltitude = 0.0;
    public double altitudeLoss = 0.0;
    public boolean significantAltitudeLoss = false;
    private static final double ALTITUDE_LOSS_THRESHOLD = 30.0; // blocks

    public AirDataComputer(Minecraft mc) {
        this.mc = mc;
    }

    public EntityPlayerSP getPlayer() {
        return mc.thePlayer;
    }

    public WorldClient getLevel() {
        return mc.theWorld;
    }

    public boolean isFlying() {
        EntityPlayerSP p = getPlayer();
        return p instanceof IElytraPlayer && ((IElytraPlayer) p).etfu$isElytraFlying();
    }

    public double getX() {
        EntityPlayerSP p = getPlayer();
        return p != null ? p.posX : 0;
    }

    public double getZ() {
        EntityPlayerSP p = getPlayer();
        return p != null ? p.posZ : 0;
    }

    public double getAltitude() {
        EntityPlayerSP p = getPlayer();
        return p != null ? p.posY : 0;
    }

    public int getVoidY() {
        return -64; // 1.7.10 world bottom is 0, void starts below
    }

    public float getPitch() {
        EntityPlayerSP p = getPlayer();
        return p != null ? -p.rotationPitch : 0; // Inverted to match aviation convention (nose up = positive)
    }

    public float getYaw() {
        EntityPlayerSP p = getPlayer();
        return p != null ? MathHelper.wrapAngleTo180_float(p.rotationYaw) : 0;
    }

    public float getHeading() {
        return getYaw() + 180.0f;
    }

    public float getFlightPitch() {
        Vec3 vel = velocity;
        double len = vel.lengthVector();
        if (len < 0.001) return 0;
        return (float) Math.toDegrees(Math.asin(vel.yCoord / len));
    }

    public float getFlightYaw() {
        return (float) Math.toDegrees(Math.atan2(-velocity.xCoord, velocity.zCoord));
    }

    public boolean isFallDistanceSafe() {
        EntityPlayerSP player = getPlayer();
        if (player == null) return true;
        if (player.isInWater()) return true;
        return player.fallDistance <= 3.0f;
    }

    @Override
    public void tick() {
        EntityPlayerSP player = getPlayer();
        if (player == null) return;

        // Capture velocity
        velocity = Vec3.createVectorHelper(player.motionX, player.motionY, player.motionZ);
        velocityPerSecond = scale(velocity, 20.0); // 20 ticks per second

        // Forward velocity: component of velocity along look direction
        forwardVelocity = computeForwardVector(velocity);
        Vec3 prevForwardVelocity = forwardVelocityPerSecond;
        forwardVelocityPerSecond = scale(forwardVelocity, 20.0);
        forwardAcceleration = (forwardVelocity.lengthVector() - prevForwardVelocity.lengthVector()) / 20.0;

        // Track altitude loss
        double alt = getAltitude();
        if (alt > maxAltitude) {
            maxAltitude = alt;
        }
        altitudeLoss = maxAltitude - alt;
        significantAltitudeLoss = altitudeLoss > ALTITUDE_LOSS_THRESHOLD && velocity.yCoord < -0.1;
        // Reset max altitude when climbing
        if (velocity.yCoord > 0.05) {
            maxAltitude = alt;
        }
    }

    @Override
    public void reset() {
        velocity = Vec3.createVectorHelper(0, 0, 0);
        velocityPerSecond = Vec3.createVectorHelper(0, 0, 0);
        forwardVelocity = Vec3.createVectorHelper(0, 0, 0);
        forwardVelocityPerSecond = Vec3.createVectorHelper(0, 0, 0);
        forwardAcceleration = 0.0;
        maxAltitude = 0.0;
        altitudeLoss = 0.0;
        significantAltitudeLoss = false;
    }

    /**
     * Check if automations (autopilot, etc.) are allowed.
     */
    public boolean automationsAllowed(boolean checkFlying) {
        if (!FAConfig.enabled) return false;
        if (checkFlying && !isFlying()) return false;
        if (!FAConfig.automationsAllowedInOverlays && mc.currentScreen != null) return false;
        return true;
    }

    /**
     * Check if safety alerts should be suppressed due to player invulnerability.
     */
    public boolean isInvulnerable() {
        EntityPlayerSP player = getPlayer();
        if (player == null) return false;
        if (!FAConfig.safety.considerInvulnerability) return false;
        return player.capabilities.isCreativeMode || player.capabilities.disableDamage;
    }

    /**
     * Check if this computer is disabled or faulted.
     */
    public boolean isDisabledOrFaulted() {
        return !enabled || faulted;
    }

    public Vec3 computeForwardVector(Vec3 vector) {
        EntityPlayerSP player = getPlayer();
        if (player == null) return Vec3.createVectorHelper(0, 0, 0);

        Vec3 lookAngle = player.getLookVec()
            .normalize();
        Vec3 normalizedVector = vector.normalize();
        double dot = lookAngle.dotProduct(normalizedVector);
        if (dot < 0) dot = 0;
        return scale(vector, dot);
    }

    private static Vec3 scale(Vec3 v, double s) {
        return Vec3.createVectorHelper(v.xCoord * s, v.yCoord * s, v.zCoord * s);
    }
}
