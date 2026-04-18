package com.hfstudio.elytrahud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Event handler for ElytraHUD.
 * Manages flying state detection and triggers HUD rendering.
 */
@SideOnly(Side.CLIENT)
public class ElytraHudEventHandler {

    private ElytraHudData hudData;
    private ElytraHudRenderer hudRenderer;
    private boolean isFlying;
    private boolean fireworkCheck;

    public ElytraHudEventHandler() {
        Minecraft mc = Minecraft.getMinecraft();
        hudRenderer = new ElytraHudRenderer(mc);
        hudData = new ElytraHudData();
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!isEnabled()) return;

        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayerSP player = mc.thePlayer;
        if (player == null) return;

        if (ElytraHudData.isElytraFlying(player)) {
            if (!isFlying) {
                // Just started flying
                isFlying = true;
                fireworkCheck = true;
                hudData = new ElytraHudData();
            }
            if (fireworkCheck) {
                hudData.fireworkRate = 0.0;
                fireworkCheck = false;
            }
            hudData.update();
        } else if (isFlying) {
            // Stopped flying
            isFlying = false;
        }
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.ALL) return;
        if (!isEnabled()) return;
        if (!isFlying) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null) return;
        if (mc.currentScreen != null) return;

        hudRenderer.render(hudData, event.partialTicks);
    }

    /**
     * Mark that a firework was used for rate tracking.
     */
    public void onFireworkUsed() {
        fireworkCheck = true;
        if (hudData != null) {
            hudData.onFireworkUsed();
        }
    }

    public boolean isFlying() {
        return isFlying;
    }

    /**
     * Check if ElytraHUD is enabled.
     */
    private boolean isEnabled() {
        return ElytraHudConfig.enabled;
    }
}
