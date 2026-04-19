package com.hfstudio.network;

import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemFlintAndSteel;
import net.minecraft.item.ItemStack;

import com.hfstudio.DoABarrelRollNH;
import com.hfstudio.flightassistant.FAConfig;

import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import ganymedes01.etfuturum.api.elytra.IElytraPlayer;

/**
 * Server-side handler for ThrowTntMessage.
 * Validates that the player is elytra flying and holding TNT,
 * consumes one TNT from the held stack and damages a flint and steel from the inventory,
 * then spawns a primed TNT entity in the direction the player is looking.
 * This class must NOT reference any client-only classes.
 */
public class ThrowTntMessageHandler implements IMessageHandler<ThrowTntMessage, ThrowTntMessage> {

    @Override
    public ThrowTntMessage onMessage(ThrowTntMessage message, MessageContext ctx) {
        // SimpleNetworkWrapper already dispatches server-side messages on the server thread.
        handleThrowTnt(ctx.getServerHandler().playerEntity);
        return null;
    }

    private static void handleThrowTnt(EntityPlayerMP player) {
        if (!FAConfig.safety.throwTntEnabled) {
            return;
        }

        // Must be elytra flying (server-side IElytraPlayer check)
        if (!(player instanceof IElytraPlayer) || !((IElytraPlayer) player).etfu$isElytraFlying()) {
            return;
        }

        // Must be holding TNT
        ItemStack held = player.getHeldItem();
        if (held == null || !isTntBlock(held)) {
            return;
        }

        // Find flint and steel in the full inventory, consume one durability point
        int flintSlot = findFlintAndSteel(player);
        if (flintSlot == -1) {
            return;
        }

        // Consume one TNT from held stack
        held.stackSize--;
        if (held.stackSize <= 0) {
            player.setCurrentItemOrArmor(0, null);
        }

        // Damage flint and steel (returns true if the item was destroyed)
        ItemStack flintStack = player.inventory.getStackInSlot(flintSlot);
        flintStack.damageItem(1, player);
        if (flintStack.stackSize <= 0 || flintStack.getItemDamage() >= flintStack.getMaxDamage()) {
            player.inventory.setInventorySlotContents(flintSlot, null);
        }

        // Spawn primed TNT slightly in front of and below the player's eye position
        double eyeX = player.posX;
        double eyeY = player.posY + player.getEyeHeight();
        double eyeZ = player.posZ;

        float pitch = (float) Math.toRadians(player.rotationPitch);
        float yaw = (float) Math.toRadians(player.rotationYaw);

        double dx = -Math.sin(yaw) * Math.cos(pitch);
        double dy = -Math.sin(pitch);
        double dz = Math.cos(yaw) * Math.cos(pitch);

        double spawnX = eyeX + dx * 1.5;
        double spawnY = eyeY + dy * 1.5;
        double spawnZ = eyeZ + dz * 1.5;

        EntityTNTPrimed tnt = new EntityTNTPrimed(player.worldObj, spawnX, spawnY, spawnZ, player);
        // Give it velocity in the look direction
        double speed = 1.5;
        tnt.motionX = dx * speed + player.motionX;
        tnt.motionY = dy * speed + player.motionY;
        tnt.motionZ = dz * speed + player.motionZ;
        player.worldObj.spawnEntityInWorld(tnt);
        DoABarrelRollNH.LOG.debug("Player {} threw a primed TNT", player.getCommandSenderName());
    }

    private static boolean isTntBlock(ItemStack stack) {
        Item item = stack.getItem();
        // TNT block item: net.minecraft.init.Blocks.tnt as an ItemBlock
        return item instanceof ItemBlock && ((ItemBlock) item).field_150939_a == net.minecraft.init.Blocks.tnt;
    }

    private static int findFlintAndSteel(EntityPlayer player) {
        for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
            ItemStack stack = player.inventory.getStackInSlot(i);
            if (stack != null && stack.getItem() instanceof ItemFlintAndSteel) {
                // Check that it has remaining durability
                if (stack.getItemDamage() < stack.getMaxDamage()) {
                    return i;
                }
            }
        }
        return -1;
    }
}
