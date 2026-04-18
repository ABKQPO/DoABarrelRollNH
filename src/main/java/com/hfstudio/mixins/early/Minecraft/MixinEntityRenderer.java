package com.hfstudio.mixins.early.Minecraft;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.renderer.EntityRenderer;

import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.hfstudio.config.ModConfig;
import com.hfstudio.roll.PlayerRenderOrientation;
import com.hfstudio.roll.RollRuntime;

/**
 * EntityRenderer Mixin.
 * 1) Redirects setAngles calls in updateCameraAndRender to RollRuntime.handleMouseTurn
 * 2) Redirects the camRoll glRotatef in orientCamera to apply custom camera roll
 */
@Mixin(EntityRenderer.class)
public abstract class MixinEntityRenderer {

    @Shadow
    private Minecraft mc;

    @Unique
    private float dabr$orientPartialTick;

    // =====================
    // Redirect mouse look updates
    // =====================

    @Redirect(
        method = "updateCameraAndRender",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/entity/EntityClientPlayerMP;setAngles(FF)V",
            ordinal = 0))
    private void dabr$redirectSetAngles0(EntityClientPlayerMP player, float yaw, float pitch) {
        RollRuntime.handleMouseTurn(player, yaw, pitch);
    }

    @Redirect(
        method = "updateCameraAndRender",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/entity/EntityClientPlayerMP;setAngles(FF)V",
            ordinal = 1))
    private void dabr$redirectSetAngles1(EntityClientPlayerMP player, float yaw, float pitch) {
        RollRuntime.handleMouseTurn(player, yaw, pitch);
    }

    // =====================
    // Camera roll setup
    // =====================

    /**
     * Captures the partialTick parameter from orientCamera for use in the subsequent Redirect.
     */
    @Inject(method = "orientCamera", at = @At("HEAD"))
    private void dabr$capturePartialTick(float partialTick, CallbackInfo ci) {
        dabr$orientPartialTick = partialTick;
    }

    /**
     * Redirects the first GL11.glRotatef call (camRoll rotation) in orientCamera,
     * replacing it with custom flight roll / landing rollBack values.
     * More reliable than setting @Shadow camRoll/prevCamRoll fields, avoiding field access/sync issues.
     */
    @Redirect(
        method = "orientCamera",
        at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glRotatef(FFFF)V", remap = false, ordinal = 0))
    private void dabr$redirectCamRollRotation(float angle, float x, float y, float z) {
        if (!ModConfig.modEnabled) {
            GL11.glRotatef(angle, x, y, z);
            return;
        }

        EntityClientPlayerMP player = this.mc.thePlayer;
        if (player == null) {
            GL11.glRotatef(angle, x, y, z);
            return;
        }

        float partial = dabr$orientPartialTick;
        boolean thirdPerson = this.mc.gameSettings.thirdPersonView != 0;
        float barrelRoll = thirdPerson ? 0.0f : RollRuntime.STATE.getBarrelRoll(partial);

        float baseRoll;
        if (RollRuntime.shouldRoll(player)) {
            baseRoll = RollRuntime.STATE.getRoll(partial);
        } else {
            baseRoll = RollRuntime.STATE.getRollBack(partial);
        }

        float roll = PlayerRenderOrientation.resolveVisualRoll(baseRoll, barrelRoll, true);
        GL11.glRotatef(-roll, x, y, z);
    }
}
