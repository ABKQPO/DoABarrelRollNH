package com.hfstudio.roll;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.settings.GameSettings;

import com.hfstudio.config.ModConfig;

import ganymedes01.etfuturum.api.elytra.IElytraPlayer;

/**
 * Core runtime.
 * Handles the mouse input pipeline: sensitivity -> axis mapping -> smoothing -> banking -> righting -> 3D rotation.
 */
public final class RollRuntime {

    public static final RollState STATE = new RollState();
    private static final DabrSmoother PITCH_SMOOTHER = new DabrSmoother();
    private static final DabrSmoother YAW_SMOOTHER = new DabrSmoother();
    private static final DabrSmoother ROLL_SMOOTHER = new DabrSmoother();

    private static long lastNanoTime = -1L;
    private static double mouseTurnX;
    private static double mouseTurnY;

    private RollRuntime() {}

    public static boolean isElytraFlying(EntityPlayerSP player) {
        return player instanceof IElytraPlayer && ((IElytraPlayer) player).etfu$isElytraFlying();
    }

    public static boolean shouldRoll(EntityPlayerSP player) {
        if (!ModConfig.modEnabled) {
            return false;
        }
        if (player == null || !isElytraFlying(player)) {
            return false;
        }
        if (ModConfig.disableWhenSubmerged && player.isInWater()) {
            return false;
        }
        return true;
    }

    public static void clearSmoothers() {
        PITCH_SMOOTHER.clear();
        YAW_SMOOTHER.clear();
        ROLL_SMOOTHER.clear();
    }

    public static void resetMouseState() {
        lastNanoTime = -1L;
        mouseTurnX = 0.0;
        mouseTurnY = 0.0;
    }

    public static float getVisualRoll(float partialTicks) {
        return STATE.getRoll(partialTicks) + STATE.getBarrelRoll(partialTicks);
    }

    public static float getVisualRollBack(float partialTicks) {
        return STATE.getRollBack(partialTicks) + STATE.getBarrelRoll(partialTicks);
    }

    public static void tickBarrelRoll(EntityPlayerSP player) {
        if (player == null || !shouldRoll(player)) {
            STATE.resetBarrelRoll();
            return;
        }

        int direction = RollKeyBindings.consumeBarrelRollDirection();
        if (direction != 0) {
            STATE.startBarrelRoll(direction);
        }

        float duration = ModConfig.barrelRollDurationTicks;
        if (duration < 1.0f) {
            duration = 1.0f;
        }
        STATE.tickBarrelRoll(1.0f / duration);
        applyBarrelRollDodge(player);
    }

    /**
     * Mixin redirect entry point.
     * Intercepts the setAngles call in EntityRenderer.updateCameraAndRender.
     */
    public static void handleMouseTurn(EntityClientPlayerMP player, float yawInput, float pitchInput) {
        if (player == null) {
            return;
        }

        if (!shouldRoll(player)) {
            resetMouseState();
            player.setAngles(yawInput, pitchInput);
            return;
        }

        double dt = getDeltaSeconds(System.nanoTime());

        // Convert raw mouse deltas to input space
        double mouseX = yawInput;
        // In 1.7.10, the mouse Y input passed to setAngles is inverted relative to modern cursor delta; flip to match
        // DABR math
        double mouseY = -pitchInput;

        if (ModConfig.momentumBasedMouse) {
            // Accumulate "virtual joystick" vector, simulating DABR's momentum mouse
            mouseTurnX += mouseX / 300.0;
            mouseTurnY += mouseY / 300.0;

            double lenSq = mouseTurnX * mouseTurnX + mouseTurnY * mouseTurnY;
            if (lenSq > 1.0) {
                double invLen = 1.0 / Math.sqrt(lenSq);
                mouseTurnX *= invLen;
                mouseTurnY *= invLen;
            }

            double readyX = mouseTurnX;
            double readyY = mouseTurnY;

            double deadzone = ModConfig.momentumMouseDeadzone;
            if (readyX * readyX + readyY * readyY < deadzone * deadzone) {
                readyX = 0.0;
                readyY = 0.0;
            }

            double scale = 1200.0 * dt;
            mouseX = readyX * scale;
            mouseY = readyY * scale;
        } else {
            mouseTurnX = 0.0;
            mouseTurnY = 0.0;
        }

        double keyAxis = getKeyAxisInput(dt);

        FlightControlInput.Axes axes = FlightControlInput.resolve(
            mouseY,
            mouseX,
            keyAxis,
            ModConfig.switchRollAndYaw,
            ModConfig.invertPitch,
            ModConfig.invertYaw,
            ModConfig.invertRoll);

        double pitch = axes.getPitch();
        double yaw = axes.getYaw();
        double roll = axes.getRoll();

        // Apply extra sensitivity multipliers
        pitch *= ModConfig.sensitivity.pitch;
        yaw *= ModConfig.sensitivity.yaw;
        roll *= ModConfig.sensitivity.roll;

        if (ModConfig.smoothing.enabled) {
            pitch = smoothInputDabr(PITCH_SMOOTHER, pitch, ModConfig.smoothing.pitch, dt);
            yaw = smoothInputDabr(YAW_SMOOTHER, yaw, ModConfig.smoothing.yaw, dt);
            roll = smoothInputDabr(ROLL_SMOOTHER, roll, ModConfig.smoothing.roll, dt);
        }

        // Post-modifiers (bypass smoothing), matching DABR's order
        if (ModConfig.banking.enabled) {
            double[] bank = computeBankingInputs(player, dt);
            pitch += bank[0];
            yaw += bank[1];
        }
        if (ModConfig.banking.automaticRighting) {
            roll += computeRightingInput(dt);
        }

        ElytraRollController.applyElytraLookDelta(player, STATE, pitch, yaw, roll);
    }

    private static double[] computeBankingInputs(EntityPlayerSP player, double dt) {
        double currentRollRad = Math.toRadians(STATE.getRawRoll());
        double currentPitchRad = Math.toRadians(player.rotationPitch);

        double strength = ModConfig.banking.strength;

        double x = Math.sin(currentRollRad) * Math.cos(currentPitchRad) * 10.0 * strength;
        double y = (-1.0 + Math.cos(currentRollRad)) * Math.cos(currentPitchRad) * 10.0 * strength;
        if (Double.isNaN(x) || Double.isNaN(y)) {
            return new double[] { 0.0, 0.0 };
        }

        x *= dt;
        y *= dt;

        // Convert absolute-coordinate x/y to local pitch/yaw deltas under the current roll
        double cos = Math.cos(currentRollRad);
        double sin = Math.sin(currentRollRad);
        double pitchDelta = -y * cos - x * sin;
        double yawDelta = -y * sin + x * cos;
        return new double[] { pitchDelta, yawDelta };
    }

    private static double computeRightingInput(double dt) {
        double currentRollRad = Math.toRadians(STATE.getRawRoll());
        double cutoff = Math.sqrt(10.0 / 3.0);
        double rollDelta = 0.0;

        if (-cutoff < currentRollRad && currentRollRad < cutoff) {
            rollDelta = -Math.pow(currentRollRad, 3) / 3.0 + currentRollRad;
        }

        double strength = 10.0 * ModConfig.banking.rightingStrength;
        return -rollDelta * strength * dt;
    }

    private static double getDeltaSeconds(long nanoTime) {
        if (lastNanoTime < 0L) {
            lastNanoTime = nanoTime;
            return 1.0 / 60.0;
        }

        long deltaNanos = nanoTime - lastNanoTime;
        lastNanoTime = nanoTime;
        double dt = deltaNanos / 1.0e9;

        // Clamp to avoid wild jumps from game lag or window defocus
        if (dt < 0.0) {
            dt = 0.0;
        } else if (dt > 0.1) {
            dt = 0.1;
        }
        return dt;
    }

    private static double getKeyAxisInput(double dt) {
        Minecraft mc = Minecraft.getMinecraft();
        GameSettings settings = mc.gameSettings;
        if (settings == null) {
            return 0.0;
        }

        int dir = 0;
        if (settings.keyBindLeft.getIsKeyPressed()) {
            dir -= 1;
        }
        if (settings.keyBindRight.getIsKeyPressed()) {
            dir += 1;
        }
        if (dir == 0) {
            return 0.0;
        }

        double degPerSecond = ModConfig.yawRateDegPerTick * 20.0;
        double degThisFrame = degPerSecond * dt * dir;
        return degThisFrame / 0.15;
    }

    private static void applyBarrelRollDodge(EntityPlayerSP player) {
        float factor = STATE.getRawBarrelRollDodgeFactor();
        int direction = STATE.getBarrelRollDirection();
        if (factor <= 0.0f || direction == 0) {
            return;
        }

        double yawRad = Math.toRadians(player.rotationYaw);
        double rightX = -Math.cos(yawRad);
        double rightZ = -Math.sin(yawRad);
        double strength = ModConfig.barrelRollDodgeStrength * factor * direction;

        player.motionX += rightX * strength;
        player.motionZ += rightZ * strength;
    }

    private static double smoothInputDabr(DabrSmoother smoother, double input, double smoothness, double dt) {
        if (smoothness <= 0.0) {
            return input;
        }

        double amount = (1.0 / smoothness) * dt;
        if (amount < 0.0) {
            amount = 0.0;
        } else if (amount > 1.0) {
            amount = 1.0;
        }

        return smoother.smooth(input, amount, ModConfig.smoothing.stopBoost);
    }
}
