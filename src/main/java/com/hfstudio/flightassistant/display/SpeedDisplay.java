package com.hfstudio.flightassistant.display;

import com.hfstudio.flightassistant.FAConfig;
import com.hfstudio.flightassistant.computer.ComputerHost;
import com.hfstudio.flightassistant.util.FADrawHelper;

/**
 * Displays speed tape on the left side of the HUD.
 * Shows current speed with a scrolling scale.
 */
public class SpeedDisplay extends Display {

    private static final int SCALE_WIDTH = 30;
    private static final int TICK_SPACING = 10;

    public SpeedDisplay(ComputerHost computers) {
        super(computers);
    }

    @Override
    public boolean allowedByConfig() {
        return FAConfig.display.showSpeedReading || FAConfig.display.showSpeedScale;
    }

    @Override
    public void render(HudFrame frame, float partial) {
        int left = frame.getLeft();
        int centerY = frame.getCenterY();
        int top = frame.getTop();
        int bottom = frame.getBottom();

        float speed = (float) (computers.displayData.lerpedForwardVelocity.lengthVector() * 20.0); // blocks/tick →
                                                                                                   // blocks/sec
        int color = FADrawHelper.primaryColor();

        if (FAConfig.display.showSpeedScale) {
            renderScale(left, top, bottom, centerY, speed, color);
        }

        if (FAConfig.display.showSpeedReading) {
            renderReading(left, centerY, speed, color);
        }
    }

    private void renderScale(int left, int top, int bottom, int centerY, float speed, int color) {
        int scaleX = left - SCALE_WIDTH - 5;
        int scaleRight = left - 5;
        int scaleHeight = bottom - top;

        // Draw scale outline
        FADrawHelper.vLine(scaleRight, top, bottom, color);

        // Scrolling speed ticks
        int speedInt = (int) speed;
        float offset = speed - speedInt;

        for (int i = -10; i <= 10; i++) {
            int tickSpeed = speedInt + i;
            if (tickSpeed < 0) continue;

            int y = centerY - (int) ((i - offset) * TICK_SPACING);
            if (y < top || y > bottom) continue;

            if (tickSpeed % 5 == 0) {
                // Major tick
                FADrawHelper.hLine(scaleRight - 6, scaleRight, y, color);
                FADrawHelper.drawRightAlignedString(String.valueOf(tickSpeed), scaleRight - 8, y - 3, color);
            } else {
                // Minor tick
                FADrawHelper.hLine(scaleRight - 3, scaleRight, y, color);
            }
        }
    }

    private void renderReading(int left, int centerY, float speed, int color) {
        // Speed readout box
        String speedText = String.valueOf((int) speed);
        int textX = left - SCALE_WIDTH - 10;
        int boxY = centerY - 5;

        // Draw box around current speed
        FADrawHelper.fill(textX - 2, boxY - 1, left - 5, boxY + 9, 0x80000000);
        FADrawHelper.renderOutline(textX - 2, boxY - 1, left - 3 - textX, 11, color);
        FADrawHelper.drawRightAlignedString(speedText, left - 7, boxY, color);
    }

    @Override
    public void renderFaulted(HudFrame frame) {
        FADrawHelper
            .drawRightAlignedString("SPD", frame.getLeft() - 10, frame.getCenterY() - 3, FADrawHelper.warningColor());
    }
}
