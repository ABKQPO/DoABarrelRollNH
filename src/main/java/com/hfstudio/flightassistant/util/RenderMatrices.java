package com.hfstudio.flightassistant.util;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

/**
 * Stores render matrices captured during world rendering for use during HUD rendering.
 */
public final class RenderMatrices {

    public static final FloatBuffer projectionMatrix = BufferUtils.createFloatBuffer(16);
    public static final FloatBuffer modelViewMatrix = BufferUtils.createFloatBuffer(16);
    public static final IntBuffer viewport = BufferUtils.createIntBuffer(16);

    // No-roll modelview matrix (only pitch + yaw, no camera roll)
    public static final float[] noRollMatrix = new float[16];

    public static boolean ready = false;

    private RenderMatrices() {}

    /**
     * Capture current OpenGL matrices. Call during RenderWorldLastEvent.
     */
    public static void captureMatrices() {
        projectionMatrix.clear();
        modelViewMatrix.clear();
        viewport.clear();

        GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, projectionMatrix);
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, modelViewMatrix);
        GL11.glGetInteger(GL11.GL_VIEWPORT, viewport);

        projectionMatrix.rewind();
        modelViewMatrix.rewind();
        viewport.rewind();

        ready = true;
    }

    /**
     * Build a no-roll view matrix from just pitch and yaw.
     */
    public static void buildNoRollMatrix(float pitch, float yaw) {
        // Start with identity
        for (int i = 0; i < 16; i++) noRollMatrix[i] = 0;
        noRollMatrix[0] = 1;
        noRollMatrix[5] = 1;
        noRollMatrix[10] = 1;
        noRollMatrix[15] = 1;

        // Rotate by pitch (around X)
        float pitchRad = (float) Math.toRadians(pitch);
        float cp = (float) Math.cos(pitchRad);
        float sp = (float) Math.sin(pitchRad);

        float[] pitchMat = new float[16];
        setIdentity(pitchMat);
        pitchMat[5] = cp;
        pitchMat[6] = sp;
        pitchMat[9] = -sp;
        pitchMat[10] = cp;

        // Rotate by yaw+180 (around Y)
        float yawRad = (float) Math.toRadians(yaw + 180.0f);
        float cy = (float) Math.cos(yawRad);
        float sy = (float) Math.sin(yawRad);

        float[] yawMat = new float[16];
        setIdentity(yawMat);
        yawMat[0] = cy;
        yawMat[2] = -sy;
        yawMat[8] = sy;
        yawMat[10] = cy;

        // noRollMatrix = pitchMat * yawMat
        multiply4x4(pitchMat, yawMat, noRollMatrix);
    }

    private static void setIdentity(float[] m) {
        for (int i = 0; i < 16; i++) m[i] = 0;
        m[0] = 1;
        m[5] = 1;
        m[10] = 1;
        m[15] = 1;
    }

    /**
     * Multiply two 4x4 column-major matrices: result = a * b
     */
    public static void multiply4x4(float[] a, float[] b, float[] result) {
        for (int col = 0; col < 4; col++) {
            for (int row = 0; row < 4; row++) {
                float sum = 0;
                for (int k = 0; k < 4; k++) {
                    sum += a[k * 4 + row] * b[col * 4 + k];
                }
                result[col * 4 + row] = sum;
            }
        }
    }
}
