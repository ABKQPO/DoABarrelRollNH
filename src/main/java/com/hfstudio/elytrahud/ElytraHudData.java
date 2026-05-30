package com.hfstudio.elytrahud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Vec3;

import com.hfstudio.doabarrelroll.util.ElytraEquipmentResolver;

/**
 * Tracks flight telemetry data for ElytraHUD gauges.
 */
public class ElytraHudData {

    public double speed;
    public double verticalSpeed;
    public double fireworkRate;
    public double durability;
    public double height;
    public double yaw;
    public int currentDurability;

    private long lastFireworkTime;
    private long prevFireworkTime;

    public void update() {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayerSP player = mc.thePlayer;
        if (player == null) return;

        // Speed: velocity magnitude * 20 (blocks/sec)
        Vec3 velocity = Vec3.createVectorHelper(player.motionX, player.motionY, player.motionZ);
        speed = velocity.lengthVector() * 20.0;

        // Vertical speed: motionY * 20
        verticalSpeed = player.motionY * 20.0;

        // Height: Y position
        height = player.posY;

        // Yaw
        yaw = player.rotationYaw;

        // Durability
        ItemStack equippedElytra = ElytraEquipmentResolver.findEquippedElytra(player);
        if (equippedElytra != null && equippedElytra.getMaxDamage() > 0) {
            durability = 1.0 - (double) equippedElytra.getItemDamage() / (double) equippedElytra.getMaxDamage();
            currentDurability = equippedElytra.getMaxDamage() - equippedElytra.getItemDamage();
        } else {
            durability = 1.0;
            currentDurability = 0;
        }
    }

    /**
     * Called when a firework is used to update rate tracking.
     */
    public void onFireworkUsed() {
        long now = System.currentTimeMillis();
        prevFireworkTime = lastFireworkTime;
        lastFireworkTime = now;

        if (prevFireworkTime > 0) {
            long delta = lastFireworkTime - prevFireworkTime;
            if (delta > 0) {
                // Rate in fireworks per minute
                fireworkRate = 60000.0 / delta;
            }
        }
    }

    public static boolean isElytraFlying(EntityPlayerSP player) {
        return ElytraEquipmentResolver.isElytraFlying(player);
    }
}
