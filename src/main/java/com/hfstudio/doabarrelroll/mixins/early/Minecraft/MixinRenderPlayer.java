package com.hfstudio.doabarrelroll.mixins.early.Minecraft;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.renderer.entity.RenderPlayer;

import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.hfstudio.doabarrelroll.roll.PlayerRenderOrientation;
import com.hfstudio.doabarrelroll.roll.RollRuntime;

/**
 * RenderPlayer Mixin.
 * 1) doRender HEAD/TAIL: aligns renderYawOffset/rotationYawHead to the view direction
 * 2) rotateCorpse HEAD+cancel: fully replaces rotation logic (body yaw + flight pitch + model roll)
 */
@Mixin(value = RenderPlayer.class, priority = 900)
public abstract class MixinRenderPlayer {

    @Unique
    private boolean dabr$renderPoseOverridden;

    @Unique
    private float dabr$prevRenderYawOffset;

    @Unique
    private float dabr$renderYawOffset;

    @Unique
    private float dabr$prevRotationYawHead;

    @Unique
    private float dabr$rotationYawHead;

    // =====================
    // doRender HEAD: save original values and replace with view direction
    // =====================

    @Inject(method = "doRender(Lnet/minecraft/client/entity/AbstractClientPlayer;DDDFF)V", at = @At("HEAD"))
    private void dabr$alignRenderPose(AbstractClientPlayer entity, double x, double y, double z, float entityYaw,
        float partialTicks, CallbackInfo ci) {
        if (!dabr$shouldUseCameraAlignedRender(entity)) {
            return;
        }

        dabr$renderPoseOverridden = true;
        dabr$prevRenderYawOffset = entity.prevRenderYawOffset;
        dabr$renderYawOffset = entity.renderYawOffset;
        dabr$prevRotationYawHead = entity.prevRotationYawHead;
        dabr$rotationYawHead = entity.rotationYawHead;

        // Align body yaw and head yaw to the view direction
        entity.prevRenderYawOffset = entity.prevRotationYaw;
        entity.renderYawOffset = entity.rotationYaw;
        entity.prevRotationYawHead = entity.prevRotationYaw;
        entity.rotationYawHead = entity.rotationYaw;
    }

    // =====================
    // doRender TAIL: restore original values
    // =====================

    @Inject(method = "doRender(Lnet/minecraft/client/entity/AbstractClientPlayer;DDDFF)V", at = @At("TAIL"))
    private void dabr$restoreRenderPose(AbstractClientPlayer entity, double x, double y, double z, float entityYaw,
        float partialTicks, CallbackInfo ci) {
        if (!dabr$renderPoseOverridden) {
            return;
        }

        entity.prevRenderYawOffset = dabr$prevRenderYawOffset;
        entity.renderYawOffset = dabr$renderYawOffset;
        entity.prevRotationYawHead = dabr$prevRotationYawHead;
        entity.rotationYawHead = dabr$rotationYawHead;
        dabr$renderPoseOverridden = false;
    }

    // =====================
    // rotateCorpse HEAD+cancel: replace flight rotation logic
    // =====================

    @Inject(
        method = "rotateCorpse(Lnet/minecraft/client/entity/AbstractClientPlayer;FFF)V",
        at = @At("HEAD"),
        cancellable = true)
    private void dabr$applyCameraAlignedRotations(AbstractClientPlayer entityLiving, float ageInTicks,
        float rotationYaw, float partialTicks, CallbackInfo ci) {
        if (!dabr$shouldUseCameraAlignedRender(entityLiving)) {
            return;
        }

        PlayerRenderOrientation.Orientation orientation = PlayerRenderOrientation.resolve(
            entityLiving.prevRenderYawOffset,
            entityLiving.renderYawOffset,
            entityLiving.prevRotationYaw,
            entityLiving.rotationYaw,
            entityLiving.prevRotationPitch,
            entityLiving.rotationPitch,
            RollRuntime.STATE.getTicksElytraFlying(),
            RollRuntime.getVisualRoll(partialTicks),
            partialTicks);

        GL11.glRotatef(orientation.getBodyYawRotation(), 0.0f, 1.0f, 0.0f);
        GL11.glRotatef(orientation.getFlightPitchRotation(), 1.0f, 0.0f, 0.0f);

        float visualRoll = orientation.getVisualRollRotation();
        float modelRoll = PlayerRenderOrientation.resolveModelRollRotation(visualRoll);
        if (modelRoll != 0.0f) {
            if (PlayerRenderOrientation.getModelRollAxis() == PlayerRenderOrientation.ModelRollAxis.BODY_Y) {
                GL11.glRotatef(modelRoll, 0.0f, 1.0f, 0.0f);
            }
        }

        ci.cancel();
    }

    @Unique
    private boolean dabr$shouldUseCameraAlignedRender(AbstractClientPlayer entityLiving) {
        EntityClientPlayerMP player = Minecraft.getMinecraft().thePlayer;
        return player != null && entityLiving == player && RollRuntime.shouldRoll(player);
    }
}
