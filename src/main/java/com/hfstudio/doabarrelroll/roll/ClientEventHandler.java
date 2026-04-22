package com.hfstudio.doabarrelroll.roll;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;

import com.hfstudio.doabarrelroll.config.ModConfig;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Client-side event handler.
 * Handles tick updates: prevState saving, barrel roll stepping, landing damping, flight timer.
 */
@SideOnly(Side.CLIENT)
public class ClientEventHandler {

    private EntityClientPlayerMP lastPlayer;

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            RollRuntime.STATE.updatePrev();
            return;
        }
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        EntityClientPlayerMP player = mc.thePlayer;
        if (player == null) {
            lastPlayer = null;
            RollRuntime.STATE.resetBarrelRoll();
            RollRuntime.resetMouseState();
            return;
        }

        // Reset all state when the player instance changes
        if (player != lastPlayer) {
            lastPlayer = player;
            RollRuntime.STATE.forceResetRoll();
            RollRuntime.STATE.setRollBack(0.0f);
            RollRuntime.STATE.setRolling(false);
            RollRuntime.STATE.resetBarrelRoll();
            RollRuntime.STATE.resetFlightTicks();
            RollRuntime.clearSmoothers();
            RollRuntime.resetMouseState();
        }

        boolean rolling = RollRuntime.shouldRoll(player);
        boolean wasRolling = RollRuntime.STATE.isRolling();
        RollRuntime.STATE.setRolling(rolling);
        RollRuntime.tickBarrelRoll(player);

        // Update flight timer
        if (rolling) {
            RollRuntime.STATE.incrementFlightTicks();
        } else {
            RollRuntime.STATE.resetFlightTicks();
        }

        if (!rolling) {
            // Flying -> landing transition: snap rollBack to current roll to avoid interpolation jump
            if (wasRolling) {
                RollRuntime.STATE.snapRollBack(RollRuntime.STATE.getRawRoll());
            }
            // Damp rollBack after landing
            float rollBack = RollRuntime.STATE.getRawRollBack();
            rollBack *= ModConfig.rollReturnDamping;
            if (Math.abs(rollBack) < 0.01f) {
                rollBack = 0.0f;
            }
            RollRuntime.STATE.setRollBack(rollBack);
            // Force reset both roll and prevRoll to prevent stale prevRoll from causing getRoll(partial) to return
            // non-zero
            RollRuntime.STATE.forceResetRoll();
            RollRuntime.STATE.resetBarrelRoll();
            RollRuntime.clearSmoothers();
            RollRuntime.resetMouseState();
        }
    }
}
