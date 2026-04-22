package com.hfstudio.flightassistant;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import com.hfstudio.doabarrelroll.network.ModNetworkHandler;
import com.hfstudio.doabarrelroll.network.ServerModDetector;
import com.hfstudio.doabarrelroll.network.ThrowTntMessage;
import com.hfstudio.flightassistant.computer.ComputerHost;
import com.hfstudio.flightassistant.display.HudDisplayHost;
import com.hfstudio.flightassistant.util.FADrawHelper;
import com.hfstudio.flightassistant.util.FATickCounter;
import com.hfstudio.flightassistant.util.RenderMatrices;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import ganymedes01.etfuturum.api.elytra.IElytraPlayer;

/**
 * Main event handler for FlightAssistant.
 * Captures render matrices during world rendering and renders HUD during overlay.
 */
public class FAEventHandler {

    private final ComputerHost computers;
    private final HudDisplayHost displayHost;
    private boolean wasFlying = false;

    public FAEventHandler() {
        this.computers = new ComputerHost();
        this.displayHost = new HudDisplayHost(computers);
    }

    public ComputerHost getComputers() {
        return computers;
    }

    /**
     * Check if FlightAssistant should be active.
     */
    public boolean isEnabled() {
        return FAConfig.enabled;
    }

    /**
     * Capture matrices during world rendering.
     */
    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        if (!isEnabled()) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null) return;

        // Capture OpenGL matrices for ScreenSpace projection
        RenderMatrices.captureMatrices();

        // Build no-roll matrix from player camera angles
        float pitch = mc.thePlayer.rotationPitch;
        float yaw = mc.thePlayer.rotationYaw;
        RenderMatrices.buildNoRollMatrix(pitch, yaw);
    }

    /**
     * Render HUD overlay.
     */
    @SubscribeEvent
    public void onRenderGameOverlay(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.ALL) return;
        if (!isEnabled()) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || mc.theWorld == null) return;

        // Only render when flying
        if (!(mc.thePlayer instanceof IElytraPlayer) || !((IElytraPlayer) mc.thePlayer).etfu$isElytraFlying()) {
            return;
        }

        // Update render tick for smooth lerping
        computers.renderTick(event.partialTicks);

        ScaledResolution sr = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        int screenWidth = sr.getScaledWidth();
        int screenHeight = sr.getScaledHeight();

        // Update FADrawHelper with current screen info
        FADrawHelper.INSTANCE.updateScreen(screenWidth, screenHeight);

        // Render all displays
        displayHost.render(screenWidth, screenHeight, event.partialTicks);
    }

    /**
     * Tick computers.
     */
    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!isEnabled()) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || mc.theWorld == null) {
            if (wasFlying) {
                computers.resetAll();
                wasFlying = false;
            }
            return;
        }

        FATickCounter.tick(mc.thePlayer, 1.0f, mc.isGamePaused());

        boolean flying = (mc.thePlayer instanceof IElytraPlayer)
            && ((IElytraPlayer) mc.thePlayer).etfu$isElytraFlying();

        if (flying && !wasFlying) {
            // Just started flying
            computers.resetAll();
        }

        if (flying || wasFlying) {
            computers.tick();
            FAKeyBindings.processKeyBindings(this);
        }

        if (!flying && wasFlying) {
            // Just stopped flying
            computers.resetAll();
        }

        wasFlying = flying;
    }

    /**
     * Intercept right-click while holding TNT during elytra flight.
     * If the server has this mod installed, cancels the vanilla interaction and
     * sends a {@link ThrowTntMessage} to the server to spawn a primed TNT.
     */
    @SubscribeEvent
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.action != PlayerInteractEvent.Action.RIGHT_CLICK_AIR
            && event.action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        if (!(event.entityPlayer instanceof EntityPlayerSP)) {
            return;
        }
        EntityPlayerSP player = (EntityPlayerSP) event.entityPlayer;
        if (!(player instanceof IElytraPlayer) || !((IElytraPlayer) player).etfu$isElytraFlying()) {
            return;
        }
        ItemStack held = player.getHeldItem();
        if (held == null || !(held.getItem() instanceof ItemBlock)
            || ((ItemBlock) held.getItem()).field_150939_a != Blocks.tnt) {
            return;
        }
        if (!FAConfig.safety.throwTntEnabled) {
            return;
        }
        if (!ServerModDetector.isServerModInstalled()) {
            return;
        }
        // Cancel vanilla TNT placement and send throw request to server
        event.setCanceled(true);
        ModNetworkHandler.INSTANCE.sendToServer(new ThrowTntMessage());
    }
}
