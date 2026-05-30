package com.hfstudio.doabarrelroll.util;

import java.util.Map;
import java.util.WeakHashMap;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import ganymedes01.etfuturum.items.equipment.ItemArmorElytra;

/**
 * Keeps client bauble elytra durability in sync while flying.
 */
public class BaublesElytraDurabilitySyncHandler {

    private final Map<EntityPlayer, Integer> lastKnownDamageByPlayer = new WeakHashMap<>();

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.side.isClient()) {
            return;
        }

        EntityPlayer player = event.player;
        ItemStack equippedElytra = ElytraEquipmentResolver.findEquippedElytra(player);
        if (!shouldSync(player, equippedElytra)) {
            lastKnownDamageByPlayer.remove(player);
            return;
        }

        int currentDamage = equippedElytra.getItemDamage();
        Integer previousDamage = lastKnownDamageByPlayer.get(player);
        if (previousDamage == null || previousDamage.intValue() != currentDamage) {
            if (BaublesSyncHelper.syncMatchingBaubleSlot(player, equippedElytra)) {
                lastKnownDamageByPlayer.put(player, currentDamage);
            }
        }
    }

    private boolean shouldSync(EntityPlayer player, ItemStack equippedElytra) {
        if (player == null || equippedElytra == null) {
            return false;
        }
        if (!ElytraEquipmentResolver.isElytraFlying(player)) {
            return false;
        }
        if (equippedElytra == player.getEquipmentInSlot(3)) {
            return false;
        }
        return equippedElytra.getItem() instanceof ItemArmorElytra;
    }
}
