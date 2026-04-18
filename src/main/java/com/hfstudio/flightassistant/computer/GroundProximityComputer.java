package com.hfstudio.flightassistant.computer;

import net.minecraft.block.Block;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.init.Blocks;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import com.hfstudio.flightassistant.FAConfig;

/**
 * Ground Proximity Warning System (GPWS).
 * Detects excessive sink rate and terrain proximity during flight.
 */
public class GroundProximityComputer extends Computer {

    private final AirDataComputer data;

    public Double groundY = null;
    public double radarAltitude = Double.MAX_VALUE;
    public boolean sinkRateWarning = false;
    public boolean sinkRateCaution = false;
    public boolean terrainWarning = false;
    public boolean terrainCaution = false;

    private static final double SINK_RATE_WARNING = -1.5; // blocks/tick
    private static final double SINK_RATE_CAUTION = -1.0;
    private static final double TERRAIN_WARNING_ALTITUDE = 10.0;
    private static final double TERRAIN_CAUTION_ALTITUDE = 20.0;

    public GroundProximityComputer(AirDataComputer data) {
        this.data = data;
    }

    @Override
    public void tick() {
        if (!data.isFlying()) {
            reset();
            return;
        }

        EntityPlayerSP player = data.getPlayer();
        if (player == null) return;

        // Raycast down to find ground
        groundY = findGroundY(player);

        if (groundY != null && groundY != Double.MAX_VALUE) {
            radarAltitude = data.getAltitude() - groundY;
        } else {
            radarAltitude = Double.MAX_VALUE;
        }

        // Suppress when safety systems are disabled or player is invulnerable
        if (!FAConfig.safetyEnabled || data.isInvulnerable()) {
            sinkRateWarning = false;
            sinkRateCaution = false;
            terrainWarning = false;
            terrainCaution = false;
            return;
        }

        // Sink rate detection
        double verticalSpeed = data.velocity.yCoord;
        int sinkMode = FAConfig.safety.sinkRateAlertMode;
        sinkRateWarning = verticalSpeed < SINK_RATE_WARNING && FAConfig.safety.isWarningEnabled(sinkMode);
        sinkRateCaution = !sinkRateWarning && verticalSpeed < SINK_RATE_CAUTION
            && FAConfig.safety.isCautionEnabled(sinkMode);

        // Terrain proximity detection
        int obstMode = FAConfig.safety.obstacleAlertMode;
        if (radarAltitude < Double.MAX_VALUE) {
            terrainWarning = radarAltitude < TERRAIN_WARNING_ALTITUDE && verticalSpeed < 0
                && FAConfig.safety.isWarningEnabled(obstMode);
            terrainCaution = !terrainWarning && radarAltitude < TERRAIN_CAUTION_ALTITUDE
                && FAConfig.safety.isCautionEnabled(obstMode);
        } else {
            terrainWarning = false;
            terrainCaution = false;
        }
    }

    @Override
    public void reset() {
        groundY = null;
        radarAltitude = Double.MAX_VALUE;
        sinkRateWarning = false;
        sinkRateCaution = false;
        terrainWarning = false;
        terrainCaution = false;
    }

    /**
     * Raycast downward to find the ground Y level.
     */
    private Double findGroundY(EntityPlayerSP player) {
        World world = player.worldObj;
        int px = MathHelper.floor_double(player.posX);
        int pz = MathHelper.floor_double(player.posZ);
        int startY = MathHelper.floor_double(player.posY);

        // Check if chunk is loaded
        if (!world.blockExists(px, startY, pz)) {
            return Double.MAX_VALUE;
        }

        // Scan downward
        for (int y = startY; y >= 0; y--) {
            Block block = world.getBlock(px, y, pz);
            if (block != null && block != Blocks.air
                && !block.getMaterial()
                    .isLiquid()) {
                return (double) (y + 1);
            }
        }

        return 0.0;
    }
}
