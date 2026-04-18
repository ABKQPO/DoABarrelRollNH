package com.hfstudio.flightassistant.computer;

import net.minecraft.client.Minecraft;

/**
 * Monitors chunk loading status around the player.
 */
public class ChunkStatusComputer extends Computer {

    private final AirDataComputer data;

    public boolean chunksUnloaded = false;

    public ChunkStatusComputer(AirDataComputer data) {
        this.data = data;
    }

    @Override
    public void tick() {
        if (!data.isFlying()) {
            chunksUnloaded = false;
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null || mc.thePlayer == null) return;

        int px = (int) mc.thePlayer.posX >> 4;
        int pz = (int) mc.thePlayer.posZ >> 4;

        // Check if surrounding chunks are loaded
        chunksUnloaded = !mc.theWorld.getChunkProvider()
            .chunkExists(px, pz);
    }

    @Override
    public void reset() {
        chunksUnloaded = false;
    }
}
