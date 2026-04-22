package com.hfstudio.doabarrelroll.mixins.early.Minecraft;

import net.minecraft.client.entity.EntityPlayerSP;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.hfstudio.doabarrelroll.roll.RollRuntime;

/**
 * EntityPlayerSP Mixin.
 * Clears moveStrafe after updatePlayerMoveState in onLivingUpdate,
 * preventing A/D keys from causing lateral movement during elytra flight (A/D are used for yaw control).
 */
@Mixin(EntityPlayerSP.class)
public abstract class MixinEntityPlayerSP {

    @Inject(
        method = "onLivingUpdate",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/util/MovementInput;updatePlayerMoveState()V",
            shift = At.Shift.AFTER))
    private void dabr$clearStrafeInput(CallbackInfo ci) {
        EntityPlayerSP self = (EntityPlayerSP) (Object) this;
        if (RollRuntime.shouldRoll(self)) {
            self.movementInput.moveStrafe = 0.0f;
        }
    }
}
