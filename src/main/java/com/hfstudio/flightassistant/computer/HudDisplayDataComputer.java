package com.hfstudio.flightassistant.computer;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Vec3;

import com.hfstudio.flightassistant.util.FATickCounter;

/**
 * Computes interpolated (lerped) display values for smooth HUD rendering.
 */
public class HudDisplayDataComputer extends Computer {

    private final Minecraft mc;
    private final AirDataComputer data;

    // Lerped values
    public Vec3 lerpedVelocity = Vec3.createVectorHelper(0, 0, 0);
    public Vec3 lerpedForwardVelocity = Vec3.createVectorHelper(0, 0, 0);
    public double lerpedAltitude = 0.0;
    public float roll = 0.0f;
    public boolean isViewMirrored = false;

    public HudDisplayDataComputer(Minecraft mc, AirDataComputer data) {
        this.mc = mc;
        this.data = data;
    }

    @Override
    public void tick() {}

    @Override
    public void renderTick() {
        float t = FATickCounter.partialTick;

        // Lerp velocity
        lerpedVelocity = lerpVec3(lerpedVelocity, data.velocity, t);
        lerpedForwardVelocity = lerpVec3(lerpedForwardVelocity, data.forwardVelocity, t);
        lerpedAltitude = data.getAltitude();

        // Roll from DoABarrelRoll state
        roll = com.hfstudio.doabarrelroll.roll.RollRuntime.getVisualRoll(t);
    }

    @Override
    public void reset() {
        lerpedVelocity = Vec3.createVectorHelper(0, 0, 0);
        lerpedForwardVelocity = Vec3.createVectorHelper(0, 0, 0);
        lerpedAltitude = 0.0;
        roll = 0.0f;
    }

    private Vec3 lerpVec3(Vec3 from, Vec3 to, float t) {
        return Vec3.createVectorHelper(
            from.xCoord + (to.xCoord - from.xCoord) * t,
            from.yCoord + (to.yCoord - from.yCoord) * t,
            from.zCoord + (to.zCoord - from.zCoord) * t);
    }
}
