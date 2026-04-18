package com.hfstudio.flightassistant.computer;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.item.ItemStack;

import com.hfstudio.flightassistant.FAConfig;

import ganymedes01.etfuturum.api.elytra.IElytraPlayer;
import ganymedes01.etfuturum.items.equipment.ItemArmorElytra;

/**
 * Monitors elytra durability status and handles auto-open.
 */
public class ElytraStatusComputer extends Computer {

    private final AirDataComputer data;

    public int durability = 0;
    public int maxDurability = 0;
    public float durabilityPercent = 1.0f;
    public boolean durabilityWarning = false;
    public boolean durabilityCaution = false;

    private static final float DURABILITY_WARNING = 0.1f;
    private static final float DURABILITY_CAUTION = 0.2f;
    private static final int FALL_TICKS_BEFORE_AUTO_OPEN = 10;
    private int fallTicks = 0;

    public ElytraStatusComputer(AirDataComputer data) {
        this.data = data;
    }

    @Override
    public void tick() {
        EntityPlayerSP player = data.getPlayer();
        if (player == null) {
            reset();
            return;
        }

        // Chest slot (index 2 in armorInventory)
        ItemStack chestItem = player.inventory.armorInventory[2];
        if (chestItem != null && chestItem.getMaxDamage() > 0) {
            maxDurability = chestItem.getMaxDamage();
            durability = maxDurability - chestItem.getItemDamage();
            durabilityPercent = (float) durability / (float) maxDurability;
        } else {
            maxDurability = 0;
            durability = 0;
            durabilityPercent = 0.0f;
        }

        int mode = FAConfig.safety.elytraDurabilityAlertMode;
        durabilityWarning = data.isFlying() && durabilityPercent < DURABILITY_WARNING
            && FAConfig.safety.isWarningEnabled(mode);
        durabilityCaution = data.isFlying() && !durabilityWarning
            && durabilityPercent < DURABILITY_CAUTION
            && FAConfig.safety.isCautionEnabled(mode);

        // Elytra auto-open: when falling with elytra equipped
        if (FAConfig.safety.elytraAutoOpen && !data.isFlying() && !player.onGround && player.motionY < -0.1) {
            ItemStack elytra = ItemArmorElytra.getElytra(player);
            if (elytra != null && !ItemArmorElytra.isBroken(elytra)) {
                fallTicks++;
                if (fallTicks >= FALL_TICKS_BEFORE_AUTO_OPEN && player instanceof IElytraPlayer
                    && !((IElytraPlayer) player).etfu$isElytraFlying()
                    && !player.capabilities.isFlying) {
                    ganymedes01.etfuturum.EtFuturum.networkWrapper
                        .sendToServer(new ganymedes01.etfuturum.network.StartElytraFlyingMessage());
                    fallTicks = 0;
                }
            }
        } else {
            fallTicks = 0;
        }
    }

    @Override
    public void reset() {
        durability = 0;
        maxDurability = 0;
        durabilityPercent = 1.0f;
        durabilityWarning = false;
        durabilityCaution = false;
        fallTicks = 0;
    }

    /**
     * Get durability display string based on configured units.
     */
    public String getDurabilityText() {
        int units = FAConfig.display.elytraDurabilityUnits;
        switch (units) {
            case 0: // RAW
                return String.valueOf(durability);
            case 1: // PERCENTAGE
                return String.format("%d%%", (int) (durabilityPercent * 100));
            case 2: // TIME (approximate seconds remaining at 1 durability per second of flight)
            default:
                int seconds = durability;
                if (seconds >= 60) {
                    return String.format("%d:%02d", seconds / 60, seconds % 60);
                }
                return String.format("%ds", seconds);
        }
    }
}
