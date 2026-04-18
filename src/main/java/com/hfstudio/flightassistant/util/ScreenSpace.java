package com.hfstudio.flightassistant.util;

import java.nio.FloatBuffer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

import org.lwjgl.BufferUtils;

/**
 * Converts world-space positions to screen-space coordinates.
 * Port of FlightAssistant's ScreenSpace utility.
 */
public final class ScreenSpace {

    private static final FloatBuffer resultBuf = BufferUtils.createFloatBuffer(3);
    private static final FloatBuffer modelBuf = BufferUtils.createFloatBuffer(16);
    private static final FloatBuffer projBuf = BufferUtils.createFloatBuffer(16);

    private ScreenSpace() {}

    /**
     * Get the screen X coordinate for a given heading angle.
     * Uses the no-roll matrix so heading positions are independent of camera roll.
     *
     * @param heading Heading in degrees (0-360)
     * @return Screen X coordinate, or Float.MAX_VALUE if off-screen
     */
    public static float getX(float heading) {
        // Direction vector for this heading
        float headingRad = (float) Math.toRadians(heading - 180.0f);
        float dx = (float) -Math.sin(headingRad);
        float dy = 0.0f;
        float dz = (float) Math.cos(headingRad);

        float[] screenPos = projectNoRoll(dx, dy, dz);
        if (screenPos == null) return Float.MAX_VALUE;
        return screenPos[0];
    }

    /**
     * Get the screen Y coordinate for a given pitch angle.
     * Uses the no-roll matrix so pitch positions are independent of camera roll.
     *
     * @param pitch Pitch in degrees
     * @return Screen Y coordinate, or Float.MAX_VALUE if off-screen
     */
    public static float getY(float pitch) {
        Minecraft mc = Minecraft.getMinecraft();
        float cameraYaw = mc.thePlayer != null ? mc.thePlayer.rotationYaw : 0;

        float pitchRad = (float) Math.toRadians(-pitch);
        float yawRad = (float) Math.toRadians(cameraYaw);

        float cp = (float) Math.cos(pitchRad);
        float sp = (float) Math.sin(pitchRad);
        float cy = (float) Math.cos(yawRad);
        float sy = (float) Math.sin(yawRad);

        float dx = -sy * cp;
        float dy = -sp;
        float dz = cy * cp;

        float[] screenPos = projectNoRoll(dx, dy, dz);
        if (screenPos == null) return Float.MAX_VALUE;
        return screenPos[1];
    }

    /**
     * Project a world-relative direction vector to screen coordinates using the no-roll matrix.
     * Uses noRollMatrix (pure pitch+yaw rotation) as the modelview so that heading/pitch
     * positions are independent of camera roll.
     *
     * @return [x, y] in GUI-scaled coordinates, or null if not visible
     */
    public static float[] projectNoRoll(float dx, float dy, float dz) {
        if (!RenderMatrices.ready) return null;

        // Normalize direction so the point is at unit distance from camera,
        // well past the near plane to avoid projection artifacts
        float len = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (len < 1e-6f) return null;
        dx /= len;
        dy /= len;
        dz /= len;

        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution sr = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        int displayHeight = mc.displayHeight;
        float guiScale = sr.getScaleFactor();

        // Use noRollMatrix directly as modelview (pure rotation, no translation)
        modelBuf.clear();
        float[] noRoll = RenderMatrices.noRollMatrix;
        for (int i = 0; i < 16; i++) {
            modelBuf.put(noRoll[i]);
        }
        modelBuf.rewind();

        projBuf.clear();
        RenderMatrices.projectionMatrix.rewind();
        projBuf.put(RenderMatrices.projectionMatrix);
        projBuf.rewind();
        RenderMatrices.projectionMatrix.rewind();

        RenderMatrices.viewport.rewind();
        resultBuf.clear();

        boolean success = org.lwjgl.util.glu.GLU
            .gluProject(dx, dy, dz, modelBuf, projBuf, RenderMatrices.viewport, resultBuf);
        RenderMatrices.viewport.rewind();
        if (!success) return null;

        float screenX = resultBuf.get(0) / guiScale;
        float screenY = (displayHeight - resultBuf.get(1)) / guiScale;
        float screenZ = resultBuf.get(2);

        // screenZ in [0,1] means in front of camera; outside means behind
        if (screenX < 0 || screenX > sr.getScaledWidth()
            || screenY < 0
            || screenY > sr.getScaledHeight()
            || screenZ < 0
            || screenZ > 1) {
            return null;
        }

        return new float[] { screenX, screenY };
    }

    /**
     * Project a world-relative direction vector to screen coordinates using the full camera
     * rotation (including roll). Used for the Flight Path Vector which should follow camera roll.
     *
     * @return [x, y] in GUI-scaled coordinates, or null if not visible
     */
    public static float[] projectWithRoll(float dx, float dy, float dz) {
        if (!RenderMatrices.ready) return null;

        // Normalize direction so the point is at unit distance from camera
        float len = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (len < 1e-6f) return null;
        dx /= len;
        dy /= len;
        dz /= len;

        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution sr = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        int displayHeight = mc.displayHeight;
        float guiScale = sr.getScaleFactor();

        // Copy modelview matrix and strip translation (use rotation only for direction vectors)
        modelBuf.clear();
        RenderMatrices.modelViewMatrix.rewind();
        modelBuf.put(RenderMatrices.modelViewMatrix);
        RenderMatrices.modelViewMatrix.rewind();
        modelBuf.put(12, 0.0f);
        modelBuf.put(13, 0.0f);
        modelBuf.put(14, 0.0f);
        modelBuf.rewind();

        projBuf.clear();
        RenderMatrices.projectionMatrix.rewind();
        projBuf.put(RenderMatrices.projectionMatrix);
        projBuf.rewind();
        RenderMatrices.projectionMatrix.rewind();

        RenderMatrices.viewport.rewind();
        resultBuf.clear();

        boolean success = org.lwjgl.util.glu.GLU
            .gluProject(dx, dy, dz, modelBuf, projBuf, RenderMatrices.viewport, resultBuf);
        RenderMatrices.viewport.rewind();
        if (!success) return null;

        float screenX = resultBuf.get(0) / guiScale;
        float screenY = (displayHeight - resultBuf.get(1)) / guiScale;
        float screenZ = resultBuf.get(2);

        if (screenX < 0 || screenX > sr.getScaledWidth()
            || screenY < 0
            || screenY > sr.getScaledHeight()
            || screenZ < 0
            || screenZ > 1) {
            return null;
        }

        return new float[] { screenX, screenY };
    }

    /**
     * Project a world-space delta position to screen coordinates.
     *
     * @param dx Delta X from camera
     * @param dy Delta Y from camera
     * @param dz Delta Z from camera
     * @return [x, y] in GUI-scaled coordinates, or null if not visible
     */
    public static float[] projectWorldSpace(double dx, double dy, double dz) {
        if (!RenderMatrices.ready) return null;

        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution sr = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        int displayHeight = mc.displayHeight;
        float guiScale = sr.getScaleFactor();

        modelBuf.clear();
        projBuf.clear();

        RenderMatrices.modelViewMatrix.rewind();
        modelBuf.put(RenderMatrices.modelViewMatrix);
        modelBuf.rewind();
        RenderMatrices.modelViewMatrix.rewind();

        RenderMatrices.projectionMatrix.rewind();
        projBuf.put(RenderMatrices.projectionMatrix);
        projBuf.rewind();
        RenderMatrices.projectionMatrix.rewind();

        resultBuf.clear();

        RenderMatrices.viewport.rewind();
        boolean success = org.lwjgl.util.glu.GLU
            .gluProject((float) dx, (float) dy, (float) dz, modelBuf, projBuf, RenderMatrices.viewport, resultBuf);
        RenderMatrices.viewport.rewind();
        if (!success) return null;

        float screenX = resultBuf.get(0) / guiScale;
        float screenY = (displayHeight - resultBuf.get(1)) / guiScale;
        float screenZ = resultBuf.get(2);

        if (screenX < 0 || screenX > sr.getScaledWidth()
            || screenY < 0
            || screenY > sr.getScaledHeight()
            || screenZ < -1
            || screenZ > 1) {
            return null;
        }

        return new float[] { screenX, screenY };
    }
}
