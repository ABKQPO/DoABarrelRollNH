package com.hfstudio.flightassistant.display;

import com.hfstudio.flightassistant.FAConfig;
import com.hfstudio.flightassistant.computer.AutoFlightComputer;
import com.hfstudio.flightassistant.computer.ComputerHost;
import com.hfstudio.flightassistant.util.FADrawHelper;
import com.hfstudio.flightassistant.util.ScreenSpace;

/**
 * Displays flight director cross-hairs showing the target pitch/heading.
 */
public class FlightDirectorsDisplay extends Display {

    public FlightDirectorsDisplay(ComputerHost computers) {
        super(computers);
    }

    @Override
    public boolean allowedByConfig() {
        return FAConfig.display.showFlightDirectors;
    }

    @Override
    public void render(HudFrame frame, float partial) {
        AutoFlightComputer af = computers.autoFlight;
        if (!af.flightDirectors) return;

        int centerX = frame.getCenterX();
        int centerY = frame.getCenterY();
        int color = FADrawHelper.activeColor();

        // Flight director horizontal bar (pitch guidance)
        float targetPitch = af.targetPitch;
        float pitchY = ScreenSpace.getY(targetPitch);
        if (pitchY != Float.MAX_VALUE) {
            int y = centerY - (int) pitchY;
            int halfWidth = frame.getFrameWidth() / 6;
            FADrawHelper.hLine(centerX - halfWidth, centerX + halfWidth, y, color);
        }

        // Flight director vertical bar (heading guidance)
        if (af.activeLateralMode != null) {
            float targetHeading = af.activeLateralMode.getTargetHeading();
            float headingX = ScreenSpace.getX(targetHeading);
            if (headingX != Float.MAX_VALUE) {
                int x = (int) headingX;
                int halfHeight = frame.getFrameHeight() / 6;
                FADrawHelper.vLine(x, centerY - halfHeight, centerY + halfHeight, color);
            }
        }
    }
}
