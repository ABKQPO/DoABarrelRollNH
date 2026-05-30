package com.hfstudio.doabarrelroll.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import ganymedes01.etfuturum.api.elytra.IElytraPlayer;
import ganymedes01.etfuturum.items.equipment.ItemArmorElytra;

/**
 * Resolves the currently equipped elytra across armor and bauble inventories.
 */
public class ElytraEquipmentResolver {

    private ElytraEquipmentResolver() {}

    public static ItemStack findEquippedElytra(EntityPlayer player) {
        if (player == null) {
            return null;
        }
        return ItemArmorElytra.getElytra(player);
    }

    public static boolean isElytraFlying(EntityPlayer player) {
        return player instanceof IElytraPlayer && ((IElytraPlayer) player).etfu$isElytraFlying();
    }
}
