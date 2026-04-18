package com.hfstudio.flightassistant.display;

import org.lwjgl.opengl.GL11;

import com.hfstudio.flightassistant.FAConfig;
import com.hfstudio.flightassistant.computer.ComputerHost;
import com.hfstudio.flightassistant.util.FADrawHelper;
import com.hfstudio.flightassistant.util.ScreenSpace;

/**
 * Displays the artificial horizon and pitch ladder.
 * The core attitude reference for the HUD.
 */
public class AttitudeDisplay extends Display {

    private static final int DEGREE_STEP_DEFAULT = 5;

    public AttitudeDisplay(ComputerHost computers) {
        super(computers);
    }

    @Override
    public boolean allowedByConfig() {
        return FAConfig.display.showAttitude > 0;
    }

    @Override
    public void render(HudFrame frame, float partial) {
        int centerX = frame.getCenterX();
        int centerY = frame.getCenterY();
        int frameLeft = frame.getLeft();
        int frameRight = frame.getRight();
        int frameTop = frame.getTop();
        int frameBottom = frame.getBottom();

        int color = FADrawHelper.primaryColor();

        // Enable scissor to clip pitch ladder to frame
        if (!FAConfig.display.drawPitchOutsideFrame) {
            FADrawHelper.enableScissor(frameLeft, frameTop, frameRight, frameBottom);
        }

        int degreeStep = FAConfig.display.attitudeDegreeStep > 0 ? FAConfig.display.attitudeDegreeStep
            : DEGREE_STEP_DEFAULT;

        float currentPitch = computers.airData.getPitch();
        float currentYaw = computers.airData.getYaw();
        float roll = computers.displayData.roll;

        // Draw pitch ladder
        if (FAConfig.display.showAttitude >= 1) {
            GL11.glPushMatrix();
            GL11.glTranslatef(centerX, centerY, 0);
            GL11.glRotatef(-roll, 0, 0, 1);
            GL11.glTranslatef(-centerX, -centerY, 0);

            drawPitchLadder(centerX, centerY, frameLeft, frameRight, currentPitch, degreeStep, color);

            GL11.glPopMatrix();
        }

        // Draw roll indicator
        if (FAConfig.display.showAttitude >= 2) {
            drawRollIndicator(centerX, frameTop, roll, color);
        }

        if (!FAConfig.display.drawPitchOutsideFrame) {
            FADrawHelper.disableScissor();
        }

        // Draw center reference (aircraft symbol) - always drawn, not rotated
        drawCenterReference(centerX, centerY, color);
    }

    private void drawPitchLadder(int centerX, int centerY, int frameLeft, int frameRight, float currentPitch,
        int degreeStep, int color) {
        int halfWidth = (frameRight - frameLeft) / 4;

        // Draw lines for each degree step
        for (int deg = -90; deg <= 90; deg += degreeStep) {
            if (deg == 0) continue; // Skip horizon line, drawn separately

            float deltaY = ScreenSpace.getY(deg);
            if (deltaY == Float.MAX_VALUE) continue;

            int y = centerY - (int) deltaY;

            // Skip if too far off screen
            if (y < centerY - 200 || y > centerY + 200) continue;

            int lineHalfWidth;
            if (deg % 10 == 0) {
                lineHalfWidth = halfWidth;
            } else {
                lineHalfWidth = halfWidth / 2;
            }

            if (deg > 0) {
                // Above horizon: solid lines
                FADrawHelper.hLine(centerX - lineHalfWidth, centerX + lineHalfWidth, y, color);
            } else {
                // Below horizon: dashed lines
                FADrawHelper.hLineDashed(centerX - lineHalfWidth, centerX + lineHalfWidth, y, color, 3, 3);
            }

            // Degree labels at major steps
            if (deg % 10 == 0) {
                String label = String.valueOf(Math.abs(deg));
                FADrawHelper
                    .drawString(label, centerX - lineHalfWidth - 4 - FADrawHelper.getStringWidth(label), y - 3, color);
                FADrawHelper.drawString(label, centerX + lineHalfWidth + 4, y - 3, color);
            }
        }

        // Draw horizon line (0 degrees)
        float horizonY = ScreenSpace.getY(0);
        if (horizonY != Float.MAX_VALUE) {
            int y = centerY - (int) horizonY;
            FADrawHelper.hLine(centerX - halfWidth * 2, centerX + halfWidth * 2, y, color);
        }
    }

    private void drawRollIndicator(int centerX, int frameTop, float roll, int color) {
        int y = frameTop - 15;
        int radius = 30;

        // Draw roll scale arc ticks at 0, ±10, ±20, ±30, ±45, ±60, ±90
        int[] rollTicks = { 0, 10, -10, 20, -20, 30, -30, 45, -45, 60, -60 };
        for (int tick : rollTicks) {
            double rad = Math.toRadians(tick);
            int tx = centerX + (int) (Math.sin(rad) * radius);
            int ty = y - (int) (Math.cos(rad) * radius);
            int len = (tick % 30 == 0) ? 5 : 3;
            int tx2 = centerX + (int) (Math.sin(rad) * (radius + len));
            int ty2 = y - (int) (Math.cos(rad) * (radius + len));
            // Simple tick mark
            FADrawHelper.vLine(tx, Math.min(ty, ty2), Math.max(ty, ty2), color);
        }

        // Draw current roll pointer
        double rollRad = Math.toRadians(roll);
        int px = centerX + (int) (Math.sin(rollRad) * (radius - 3));
        int py = y - (int) (Math.cos(rollRad) * (radius - 3));
        FADrawHelper.fill(px - 2, py - 2, px + 2, py + 2, color);
    }

    private void drawCenterReference(int centerX, int centerY, int color) {
        // Aircraft symbol: short horizontal lines with a gap in the center
        int wingLen = 15;
        int gap = 3;

        FADrawHelper.hLine(centerX - wingLen - gap, centerX - gap, centerY, color);
        FADrawHelper.hLine(centerX + gap, centerX + wingLen + gap, centerY, color);
        FADrawHelper.vLine(centerX, centerY - 2, centerY + 2, color);
    }

    @Override
    public void renderFaulted(HudFrame frame) {
        FADrawHelper
            .drawMiddleAlignedString("ATT", frame.getCenterX(), frame.getCenterY(), FADrawHelper.warningColor());
    }
}
