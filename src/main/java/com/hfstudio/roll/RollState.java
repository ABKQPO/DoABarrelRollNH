package com.hfstudio.roll;

import net.minecraft.util.MathHelper;

/**
 * Roll state management.
 * Maintains the primary roll angle, landing recovery angle, and barrel roll animation progress.
 */
public final class RollState {

    private float roll;
    private float prevRoll;
    private float rollBack;
    private float prevRollBack;
    private float barrelRollProgress;
    private float prevBarrelRollProgress;
    private int barrelRollDirection;
    private boolean barrelRolling;
    private boolean rolling;
    private int ticksElytraFlying;

    public void setRolling(boolean rolling) {
        this.rolling = rolling;
    }

    public boolean isRolling() {
        return rolling;
    }

    public float getRoll(float partialTicks) {
        return prevRoll + (roll - prevRoll) * partialTicks;
    }

    public float getRollBack(float partialTicks) {
        return prevRollBack + (rollBack - prevRollBack) * partialTicks;
    }

    public float getRawRoll() {
        return roll;
    }

    public float getRawRollBack() {
        return rollBack;
    }

    public float getBarrelRoll(float partialTicks) {
        float progress = prevBarrelRollProgress + (barrelRollProgress - prevBarrelRollProgress) * partialTicks;
        return barrelRollDirection * 360.0f * easeInOut(progress);
    }

    public float getRawBarrelRoll() {
        return barrelRollDirection * 360.0f * easeInOut(barrelRollProgress);
    }

    public float getRawBarrelRollDodgeFactor() {
        if (barrelRollDirection == 0) {
            return 0.0f;
        }
        return (float) Math.sin(barrelRollProgress * Math.PI);
    }

    public int getBarrelRollDirection() {
        return barrelRollDirection;
    }

    public boolean isBarrelRolling() {
        return barrelRolling;
    }

    public int getTicksElytraFlying() {
        return ticksElytraFlying;
    }

    public void incrementFlightTicks() {
        ticksElytraFlying++;
    }

    public void resetFlightTicks() {
        ticksElytraFlying = 0;
    }

    public void setRoll(float newRoll) {
        if (!Float.isFinite(newRoll)) {
            return;
        }

        float lastRoll = this.roll;
        this.roll = newRoll;

        // Maintain interpolation continuity across the -180/180 boundary
        if (newRoll < -90.0f && lastRoll > 90.0f) {
            prevRoll -= 360.0f;
        } else if (newRoll > 90.0f && lastRoll < -90.0f) {
            prevRoll += 360.0f;
        }
    }

    public void addRollDelta(float deltaRoll) {
        if (!Float.isFinite(deltaRoll)) {
            return;
        }

        this.roll += deltaRoll;
        this.prevRoll += deltaRoll;
    }

    /**
     * Force reset both roll and prevRoll to zero.
     * Used for landing reset to avoid setRoll(0) leaving stale prevRoll that causes getRoll(partial) to return
     * non-zero.
     */
    public void forceResetRoll() {
        this.roll = 0.0f;
        this.prevRoll = 0.0f;
    }

    public void setRollBack(float newRollBack) {
        newRollBack = MathHelper.wrapAngleTo180_float(newRollBack);
        prevRollBack = rollBack;
        rollBack = newRollBack;
        normalizePrevBackForInterpolation();
    }

    /**
     * Sets both prev and current rollBack to the same value,
     * ensuring getRollBack(partial) doesn't produce a jump during the flying -> landing transition.
     */
    public void snapRollBack(float value) {
        value = MathHelper.wrapAngleTo180_float(value);
        prevRollBack = value;
        rollBack = value;
    }

    public void startBarrelRoll(int direction) {
        if (direction == 0 || isBarrelRolling()) {
            return;
        }

        barrelRollDirection = direction > 0 ? 1 : -1;
        barrelRollProgress = 0.0f;
        prevBarrelRollProgress = 0.0f;
        barrelRolling = true;
    }

    public void tickBarrelRoll(float progressStep) {
        if (!Float.isFinite(progressStep) || progressStep <= 0.0f || !isBarrelRolling()) {
            return;
        }

        barrelRollProgress += progressStep;
        if (barrelRollProgress >= 1.0f) {
            barrelRollProgress = 1.0f;
            barrelRolling = false;
        }
    }

    public void resetBarrelRoll() {
        barrelRollProgress = 0.0f;
        prevBarrelRollProgress = 0.0f;
        barrelRollDirection = 0;
        barrelRolling = false;
    }

    public void updatePrev() {
        prevRoll = roll;
        prevRollBack = rollBack;
        prevBarrelRollProgress = barrelRollProgress;
    }

    private static float easeInOut(float progress) {
        if (progress <= 0.0f) {
            return 0.0f;
        }
        if (progress >= 1.0f) {
            return 1.0f;
        }
        return progress * progress * (3.0f - 2.0f * progress);
    }

    @SuppressWarnings("unused")
    private void normalizePrevForInterpolation() {
        while (roll - prevRoll < -180.0f) {
            prevRoll -= 360.0f;
        }
        while (roll - prevRoll >= 180.0f) {
            prevRoll += 360.0f;
        }
    }

    private void normalizePrevBackForInterpolation() {
        while (rollBack - prevRollBack < -180.0f) {
            prevRollBack -= 360.0f;
        }
        while (rollBack - prevRollBack >= 180.0f) {
            prevRollBack += 360.0f;
        }
    }
}
