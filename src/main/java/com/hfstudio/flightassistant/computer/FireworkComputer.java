package com.hfstudio.flightassistant.computer;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.AxisAlignedBB;

import com.hfstudio.elytrahud.ElytraHudModule;
import com.hfstudio.flightassistant.FAConfig;

import ganymedes01.etfuturum.entities.EntityBoostingFireworkRocket;

/**
 * Monitors firework rocket availability, usage, and safety.
 * Detects explosive firework rockets and manages boost tracking.
 * Detects firework usage by monitoring inventory count changes.
 * Tracks active boost by scanning for EntityBoostingFireworkRocket in the world.
 */
public class FireworkComputer extends Computer {

    private final AirDataComputer data;

    public int fireworkCount = 0;
    public boolean hasFireworks = false;
    public boolean activeBoosting = false;

    // Explosive firework detection
    public boolean explosiveDetected = false;
    public boolean explosiveInHotbar = false;
    public int explosiveSlot = -1;

    // Firework use request (from ThrustComputer)
    private boolean fireworkUseRequested = false;

    // Inventory tracking for firework usage detection
    private int prevFireworkCount = -1;
    private boolean autoUsedLastTick = false;

    public FireworkComputer(AirDataComputer data) {
        this.data = data;
    }

    @Override
    public void tick() {
        EntityPlayerSP player = data.getPlayer();
        if (player == null) {
            reset();
            return;
        }

        // Count firework rockets and check for explosives
        int oldCount = prevFireworkCount;
        fireworkCount = 0;
        explosiveDetected = false;
        explosiveInHotbar = false;
        explosiveSlot = -1;

        for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
            ItemStack stack = player.inventory.getStackInSlot(i);
            if (stack != null && isFireworkRocket(stack)) {
                fireworkCount += stack.stackSize;

                boolean isExplosive = hasExplosiveCharge(stack);
                if (isExplosive) {
                    explosiveDetected = true;
                    if (i < 9) {
                        explosiveInHotbar = true;
                        if (explosiveSlot == -1) {
                            explosiveSlot = i;
                        }
                    }
                }
            }
        }
        hasFireworks = fireworkCount > 0;

        // Detect firework usage by inventory count decrease
        if (data.isFlying() && oldCount >= 0 && fireworkCount < oldCount && !autoUsedLastTick) {
            notifyElytraHud();
        }
        autoUsedLastTick = false;
        prevFireworkCount = fireworkCount;

        // Detect active boost by scanning for EntityBoostingFireworkRocket attached to player
        activeBoosting = false;
        AxisAlignedBB box = player.boundingBox.expand(1.0, 1.0, 1.0);
        List<EntityBoostingFireworkRocket> list = player.worldObj
            .getEntitiesWithinAABB(EntityBoostingFireworkRocket.class, box);
        for (EntityBoostingFireworkRocket firework : list) {
            if (!firework.isDead && firework.isAttachedToEntity()) {
                int attachedId = firework.getDataWatcher()
                    .getWatchableObjectInt(9);
                if (attachedId == player.getEntityId()) {
                    activeBoosting = true;
                    break;
                }
            }
        }

        // Handle firework use request from auto-thrust
        if (fireworkUseRequested) {
            fireworkUseRequested = false;
            useFirework(player);
        }

        // Lock explosive fireworks in hotbar (swap away)
        if (data.isFlying() && explosiveInHotbar && FAConfig.safety.fireworkLockExplosive) {
            int currentSlot = player.inventory.currentItem;
            ItemStack currentStack = player.inventory.getStackInSlot(currentSlot);
            if (currentStack != null && isFireworkRocket(currentStack) && hasExplosiveCharge(currentStack)) {
                for (int i = 0; i < 9; i++) {
                    ItemStack s = player.inventory.getStackInSlot(i);
                    if (s == null || (isFireworkRocket(s) && !hasExplosiveCharge(s))) {
                        player.inventory.currentItem = i;
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void reset() {
        fireworkCount = 0;
        hasFireworks = false;
        activeBoosting = false;
        explosiveDetected = false;
        explosiveInHotbar = false;
        explosiveSlot = -1;
        fireworkUseRequested = false;
        prevFireworkCount = -1;
        autoUsedLastTick = false;
    }

    /**
     * Request a firework to be used for auto-thrust.
     */
    public void requestFireworkUse() {
        fireworkUseRequested = true;
    }

    /**
     * Try to use a firework rocket from the player's hotbar.
     */
    private void useFirework(EntityPlayerSP player) {
        int originalSlot = player.inventory.currentItem;
        int fireworkSlot = -1;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.inventory.getStackInSlot(i);
            if (stack != null && isFireworkRocket(stack) && !hasExplosiveCharge(stack)) {
                fireworkSlot = i;
                break;
            }
        }

        if (fireworkSlot == -1) return;

        player.inventory.currentItem = fireworkSlot;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.playerController != null) {
            mc.playerController.sendUseItem(player, mc.theWorld, player.inventory.getStackInSlot(fireworkSlot));
            notifyElytraHud();
            autoUsedLastTick = true;
        }
        player.inventory.currentItem = originalSlot;
    }

    /**
     * Notify ElytraHUD module that a firework was used.
     */
    private void notifyElytraHud() {
        try {
            var hudHandler = ElytraHudModule.getEventHandler();
            if (hudHandler != null) {
                hudHandler.onFireworkUsed();
            }
        } catch (Exception ignored) {}
    }

    /**
     * Check if an ItemStack is a firework rocket.
     */
    private static boolean isFireworkRocket(ItemStack stack) {
        return stack.getUnlocalizedName()
            .contains("firework");
    }

    /**
     * Check if a firework rocket has explosive charges (stars).
     * Fireworks with stars explode and deal damage.
     */
    private static boolean hasExplosiveCharge(ItemStack stack) {
        if (!stack.hasTagCompound()) return false;
        NBTTagCompound tag = stack.getTagCompound();
        if (!tag.hasKey("Fireworks")) return false;
        NBTTagCompound fireworks = tag.getCompoundTag("Fireworks");
        if (!fireworks.hasKey("Explosions")) return false;
        NBTTagList explosions = fireworks.getTagList("Explosions", 10);
        return explosions.tagCount() > 0;
    }
}
