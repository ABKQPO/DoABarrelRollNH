package com.hfstudio.flightassistant.util;

import net.minecraft.client.entity.EntityPlayerSP;

import ganymedes01.etfuturum.api.elytra.IElytraPlayer;

/**
 * Tick counter for FlightAssistant.
 * Tracks time passed, ticks since world load, etc.
 */
public final class FATickCounter {

    public static final int worldLoadWaitTime = 40; // ticks to wait after world load

    public static int ticksSinceWorldLoad;
    public static int ticksPassed;
    public static float timePassed;
    public static float partialTick;

    private static float prevPartialTick;
    private static boolean wasFlying;

    private FATickCounter() {}

    public static void tick(EntityPlayerSP player, float partial, boolean paused) {
        partialTick = partial;

        if (paused) {
            ticksPassed = 0;
            timePassed = 0.0f;
            return;
        }

        boolean isFlying = player instanceof IElytraPlayer && ((IElytraPlayer) player).etfu$isElytraFlying();

        if (!wasFlying && isFlying) {
            ticksSinceWorldLoad = 0;
        }
        wasFlying = isFlying;

        float delta = partial - prevPartialTick;
        if (delta < 0) {
            delta += 1.0f;
            ticksPassed = 1;
        } else {
            ticksPassed = 0;
        }
        prevPartialTick = partial;

        timePassed = delta * 0.05f; // Convert ticks to seconds (1 tick = 0.05s)
        ticksSinceWorldLoad++;
    }

    public static void reset() {
        ticksSinceWorldLoad = 0;
        ticksPassed = 0;
        timePassed = 0.0f;
        partialTick = 0.0f;
        prevPartialTick = 0.0f;
        wasFlying = false;
    }
}
