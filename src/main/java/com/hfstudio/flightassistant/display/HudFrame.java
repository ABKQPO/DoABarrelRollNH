package com.hfstudio.flightassistant.display;

import com.hfstudio.flightassistant.FAConfig;

/**
 * Defines the HUD frame dimensions and provides coordinate helpers.
 */
public class HudFrame {

    private int screenWidth;
    private int screenHeight;

    public HudFrame() {}

    /**
     * Update frame based on current screen dimensions.
     */
    public void update(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
    }

    public int getFrameWidth() {
        return (int) (FAConfig.display.frameWidth * screenWidth);
    }

    public int getFrameHeight() {
        return (int) (FAConfig.display.frameHeight * screenHeight);
    }

    /**
     * Center X of the HUD frame on screen.
     */
    public int getCenterX() {
        return screenWidth / 2;
    }

    /**
     * Center Y of the HUD frame on screen.
     */
    public int getCenterY() {
        return screenHeight / 2;
    }

    /**
     * Left edge of the HUD frame.
     */
    public int getLeft() {
        return getCenterX() - getFrameWidth() / 2;
    }

    /**
     * Right edge of the HUD frame.
     */
    public int getRight() {
        return getCenterX() + getFrameWidth() / 2;
    }

    /**
     * Top edge of the HUD frame.
     */
    public int getTop() {
        return getCenterY() - getFrameHeight() / 2;
    }

    /**
     * Bottom edge of the HUD frame.
     */
    public int getBottom() {
        return getCenterY() + getFrameHeight() / 2;
    }
}
