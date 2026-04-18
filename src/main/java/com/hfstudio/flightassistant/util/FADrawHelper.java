package com.hfstudio.flightassistant.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;

import org.lwjgl.opengl.GL11;

import com.hfstudio.flightassistant.FAConfig;

/**
 * Drawing utilities for FlightAssistant HUD.
 * Provides hLine, vLine, fill, drawString, scissor, and other primitives
 * using 1.7.10 OpenGL and Gui methods.
 */
public final class FADrawHelper extends Gui {

    public static final FADrawHelper INSTANCE = new FADrawHelper();

    private int currentScreenWidth;
    private int currentScreenHeight;

    private FADrawHelper() {}

    public static FADrawHelper get() {
        return INSTANCE;
    }

    /**
     * Update cached screen dimensions. Called each render frame.
     */
    public void updateScreen(int width, int height) {
        this.currentScreenWidth = width;
        this.currentScreenHeight = height;
    }

    // ========== Color Constants ==========

    public static int primaryColor() {
        return FAConfig.display.getPrimaryColorAlpha();
    }

    public static int secondaryColor() {
        return FAConfig.display.getSecondaryColorAlpha();
    }

    public static int primaryAdvisoryColor() {
        return FAConfig.display.getPrimaryAdvisoryColorAlpha();
    }

    public static int secondaryAdvisoryColor() {
        return FAConfig.display.getSecondaryAdvisoryColorAlpha();
    }

    public static int cautionColor() {
        return FAConfig.display.getCautionColorAlpha();
    }

    public static int warningColor() {
        return FAConfig.display.getWarningColorAlpha();
    }

    /**
     * Active/engaged mode color (green).
     */
    public static int activeColor() {
        return 0xFF00FF00;
    }

    public static final int WHITE = 0xFFFFFFFF;

    // ========== Screen Dimensions ==========

    public static int guiWidth() {
        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution sr = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        return sr.getScaledWidth();
    }

    public static int guiHeight() {
        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution sr = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        return sr.getScaledHeight();
    }

    public static float centerXF() {
        return guiWidth() * 0.5f;
    }

    public static int centerX() {
        return (int) centerXF();
    }

    public static float centerYF() {
        return guiHeight() * 0.5f;
    }

    public static int centerY() {
        return (int) centerYF();
    }

    public static float halfWidth() {
        return guiWidth() * 0.5f;
    }

    public static int lineHeight() {
        return Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT;
    }

    // ========== Line Drawing ==========

    /**
     * Draw a horizontal line from x1 to x2 at y.
     */
    public static void hLine(int x1, int x2, int y, int color) {
        if (x2 < x1) {
            int tmp = x1;
            x1 = x2;
            x2 = tmp;
        }
        drawRect(x1, y, x2 + 1, y + 1, color);
    }

    /**
     * Draw a vertical line at x from y1 to y2.
     */
    public static void vLine(int x, int y1, int y2, int color) {
        if (y2 < y1) {
            int tmp = y1;
            y1 = y2;
            y2 = tmp;
        }
        drawRect(x, y1, x + 1, y2 + 1, color);
    }

    /**
     * Draw a dashed horizontal line.
     */
    public static void hLineDashed(int x1, int x2, int y, int color, int dashLen, int spaceLen) {
        if (x2 < x1) {
            int tmp = x1;
            x1 = x2;
            x2 = tmp;
        }
        boolean drawing = true;
        int x = x1;
        while (x <= x2) {
            if (drawing) {
                int end = Math.min(x + dashLen - 1, x2);
                hLine(x, end, y, color);
                x = end + 1;
            } else {
                x += spaceLen;
            }
            drawing = !drawing;
        }
    }

    /**
     * Draw a dashed horizontal line with specified dash count.
     */
    public static void hLineDashed(int x1, int x2, int y, int dashCount, int color) {
        int width = x2 - x1;
        if (width <= dashCount) {
            hLine(x1, x2, y, color);
            return;
        }
        int spaces = dashCount - 1;
        int spaceOffset = Math.max(2, width / (dashCount * 2));
        while ((width - spaces * spaceOffset) % dashCount != 0) {
            spaceOffset++;
        }
        int singleWidth = (width - spaces * spaceOffset) / dashCount;
        for (int i = 0; i < dashCount; i++) {
            int fromLastDash = (singleWidth + spaceOffset) * i;
            hLine(x1 + fromLastDash, x1 + fromLastDash + singleWidth, y, color);
        }
    }

    /**
     * Fill a rectangle.
     */
    public static void fill(int x1, int y1, int x2, int y2, int color) {
        drawRect(x1, y1, x2, y2, color);
    }

    /**
     * Draw an outline rectangle.
     */
    public static void renderOutline(int x, int y, int width, int height, int color) {
        hLine(x, x + width - 1, y, color);
        hLine(x, x + width - 1, y + height, color);
        vLine(x, y, y + height, color);
        vLine(x + width, y, y + height, color);
    }

    // ========== Text Drawing ==========

    public static int textWidth(String text) {
        return Minecraft.getMinecraft().fontRenderer.getStringWidth(text);
    }

    public static int getStringWidth(String text) {
        return textWidth(text);
    }

    public static void drawString(String text, int x, int y, int color) {
        Minecraft.getMinecraft().fontRenderer.drawString(text, x, y, color);
    }

    public static void drawStringWithShadow(String text, int x, int y, int color) {
        Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(text, x, y, color);
    }

    public static void drawRightAlignedString(String text, int x, int y, int color) {
        int w = textWidth(text);
        drawString(text, x - w, y, color);
    }

    public static void drawMiddleAlignedString(String text, int x, int y, int color) {
        int w = textWidth(text);
        drawString(text, x - w / 2 + 1, y, color);
    }

    /**
     * Draw highlighted centered text (filled background + contrasting text).
     */
    public static void drawHighlightedCenteredText(String text, int x, int y, int color, boolean highlight) {
        if (highlight) {
            int halfWidth = textWidth(text) / 2;
            fill(x - halfWidth - 1, y - 1, x + halfWidth + 2, y + 8, color);
            int contrasting = getContrasting(color);
            drawMiddleAlignedString(text, x, y, contrasting);
        } else {
            drawMiddleAlignedString(text, x, y, color);
        }
    }

    /**
     * Draw highlighted centered text (always highlighted).
     */
    public static void drawHighlightedCenteredText(String text, int x, int y, int color) {
        drawHighlightedCenteredText(text, x, y, color, true);
    }

    private static int getContrasting(int color) {
        int red = (color >> 16) & 255;
        int green = (color >> 8) & 255;
        int blue = color & 255;
        double luma = (0.299 * red + 0.587 * green + 0.114 * blue) / 255.0;
        return luma > 0.5 ? 0xFF000000 : 0xFFFFFFFF;
    }

    // ========== GL State Helpers ==========

    /**
     * Enable scissor test to clip rendering to a rectangle.
     * Coordinates are in GUI-scaled space.
     */
    public static void enableScissor(int x1, int y1, int x2, int y2) {
        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution sr = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        int scaleFactor = sr.getScaleFactor();

        int scissorX = x1 * scaleFactor;
        int scissorY = mc.displayHeight - y2 * scaleFactor;
        int scissorW = (x2 - x1) * scaleFactor;
        int scissorH = (y2 - y1) * scaleFactor;

        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(scissorX, scissorY, scissorW, scissorH);
    }

    public static void disableScissor() {
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    /**
     * Push matrix, translate and scale.
     */
    public static void fusedTranslateScale(float x, float y, float scale) {
        GL11.glTranslatef(x, y, 0.0f);
        GL11.glScalef(scale, scale, 1.0f);
    }
}
