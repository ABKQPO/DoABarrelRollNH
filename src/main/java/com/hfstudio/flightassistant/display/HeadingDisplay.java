package com.hfstudio.flightassistant.display;

import com.hfstudio.flightassistant.FAConfig;
import com.hfstudio.flightassistant.computer.ComputerHost;
import com.hfstudio.flightassistant.util.FADrawHelper;
import com.hfstudio.flightassistant.util.ScreenSpace;

/**
 * Displays the heading tape at the top of the HUD.
 */
public class HeadingDisplay extends Display {

    private static final int TICK_SPACING = 10;
    private static final String[] CARDINAL = { "N", "NE", "E", "SE", "S", "SW", "W", "NW" };

    public HeadingDisplay(ComputerHost computers) {
        super(computers);
    }

    @Override
    public boolean allowedByConfig() {
        return FAConfig.display.showHeadingReading || FAConfig.display.showHeadingScale;
    }

    @Override
    public void render(HudFrame frame, float partial) {
        int centerX = frame.getCenterX();
        int top = frame.getTop();
        int left = frame.getLeft();
        int right = frame.getRight();

        float heading = computers.airData.getHeading();
        int color = FADrawHelper.primaryColor();

        int tapeY = top - 15;

        if (FAConfig.display.showHeadingScale) {
            FADrawHelper.enableScissor(left, tapeY - 5, right, tapeY + 15);
            renderScale(centerX, tapeY, heading, color);
            FADrawHelper.disableScissor();
        }

        if (FAConfig.display.showHeadingReading) {
            renderReading(centerX, tapeY, heading, color);
        }
    }

    private void renderScale(int centerX, int tapeY, float heading, int color) {
        // Draw heading ticks
        for (int deg = 0; deg < 360; deg += 5) {
            float screenX = ScreenSpace.getX(deg);
            if (screenX == Float.MAX_VALUE) continue;

            int x = (int) screenX;

            if (deg % 10 == 0) {
                FADrawHelper.vLine(x, tapeY, tapeY + 6, color);
                String label;
                if (deg % 45 == 0) {
                    label = CARDINAL[deg / 45];
                } else {
                    label = String.valueOf(deg);
                }
                FADrawHelper.drawMiddleAlignedString(label, x, tapeY + 8, color);
            } else {
                FADrawHelper.vLine(x, tapeY, tapeY + 3, color);
            }
        }
    }

    private void renderReading(int centerX, int tapeY, float heading, int color) {
        String headingText = String.format("%03d", (int) ((heading + 360) % 360));
        int boxWidth = FADrawHelper.getStringWidth(headingText) + 6;
        int boxX = centerX - boxWidth / 2;
        int boxY = tapeY - 12;

        FADrawHelper.fill(boxX, boxY, boxX + boxWidth, boxY + 11, 0x80000000);
        FADrawHelper.renderOutline(boxX, boxY, boxWidth, 11, color);
        FADrawHelper.drawMiddleAlignedString(headingText, centerX, boxY + 2, color);
    }

    @Override
    public void renderFaulted(HudFrame frame) {
        FADrawHelper
            .drawMiddleAlignedString("HDG", frame.getCenterX(), frame.getTop() - 15, FADrawHelper.warningColor());
    }
}
