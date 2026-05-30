package com.hfstudio.doabarrelroll.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import baubles.common.container.InventoryBaubles;
import baubles.common.lib.PlayerHandler;

/**
 * Synchronizes Baubles slot updates to tracking clients.
 */
public class BaublesSyncHelper {

    private BaublesSyncHelper() {}

    public static boolean syncMatchingBaubleSlot(EntityPlayer player, ItemStack expectedStack) {
        if (player == null || expectedStack == null) {
            return false;
        }

        InventoryBaubles baublesInventory = PlayerHandler.getPlayerBaubles(player);
        if (baublesInventory == null) {
            return false;
        }

        for (int slot = 0; slot < baublesInventory.getSizeInventory(); slot++) {
            if (baublesInventory.getStackInSlot(slot) == expectedStack) {
                baublesInventory.syncSlotToClients(slot);
                return true;
            }
        }

        return false;
    }
}
