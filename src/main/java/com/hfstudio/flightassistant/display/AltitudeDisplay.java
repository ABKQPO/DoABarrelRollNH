package com.hfstudio.flightassistant.display;

import com.hfstudio.flightassistant.FAConfig;
import com.hfstudio.flightassistant.computer.ComputerHost;
import com.hfstudio.flightassistant.util.FADrawHelper;

/**
 * Displays altitude tape on the right side of the HUD.
 * Shows current altitude with a scrolling scale.
 */
public class AltitudeDisplay extends Display {

    private static final int SCALE_WIDTH = 30;
    private static final int TICK_SPACING = 10;

    public AltitudeDisplay(ComputerHost computers) {
        super(computers);
    }

    @Override
    public boolean allowedByConfig() {
        return FAConfig.display.showAltitudeReading || FAConfig.display.showAltitudeScale;
    }

    @Override
    public void render(HudFrame frame, float partial) {
        int right = frame.getRight();
        int centerY = frame.getCenterY();
        int top = frame.getTop();
        int bottom = frame.getBottom();

        float altitude = (float) computers.displayData.lerpedAltitude;
        int color = FADrawHelper.primaryColor();

        if (FAConfig.display.showAltitudeScale) {
            renderScale(right, top, bottom, centerY, altitude, color);
        }

        if (FAConfig.display.showAltitudeReading) {
            renderReading(right, centerY, altitude, color);
        }
    }

    private void renderScale(int right, int top, int bottom, int centerY, float altitude, int color) {
        int scaleLeft = right + 5;

        // Draw scale outline
        FADrawHelper.vLine(scaleLeft, top, bottom, color);

        // Scrolling altitude ticks
        int altInt = (int) altitude;
        float offset = altitude - altInt;

        for (int i = -10; i <= 10; i++) {
            int tickAlt = altInt + i * 5;

            int y = centerY - (int) ((tickAlt - altitude) * TICK_SPACING / 5.0f);
            if (y < top || y > bottom) continue;

            if (tickAlt % 10 == 0) {
                // Major tick
                FADrawHelper.hLine(scaleLeft, scaleLeft + 6, y, color);
                FADrawHelper.drawString(String.valueOf(tickAlt), scaleLeft + 8, y - 3, color);
            } else {
                // Minor tick
                FADrawHelper.hLine(scaleLeft, scaleLeft + 3, y, color);
            }
        }
    }

    private void renderReading(int right, int centerY, float altitude, int color) {
        // Altitude readout box
        String altText = String.valueOf((int) altitude);
        int textX = right + 10;
        int boxY = centerY - 5;

        FADrawHelper.fill(right + 5, boxY - 1, textX + FADrawHelper.getStringWidth(altText) + 4, boxY + 9, 0x80000000);
        FADrawHelper
            .renderOutline(right + 5, boxY - 1, FADrawHelper.getStringWidth(altText) + textX - right - 1, 11, color);
        FADrawHelper.drawString(altText, textX, boxY, color);
    }

    @Override
    public void renderFaulted(HudFrame frame) {
        FADrawHelper.drawString("ALT", frame.getRight() + 10, frame.getCenterY() - 3, FADrawHelper.warningColor());
    }
}
