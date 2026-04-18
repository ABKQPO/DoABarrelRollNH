package com.hfstudio.flightassistant.display;

import com.hfstudio.flightassistant.FAConfig;
import com.hfstudio.flightassistant.computer.ComputerHost;
import com.hfstudio.flightassistant.util.FADrawHelper;

/**
 * Displays player coordinates at the bottom of the HUD.
 */
public class CoordinatesDisplay extends Display {

    public CoordinatesDisplay(ComputerHost computers) {
        super(computers);
    }

    @Override
    public boolean allowedByConfig() {
        return FAConfig.display.showCoordinates;
    }

    @Override
    public void render(HudFrame frame, float partial) {
        double x = computers.airData.getX();
        double z = computers.airData.getZ();

        int color = FADrawHelper.primaryColor();
        int displayY = frame.getBottom() + 5;
        int centerX = frame.getCenterX();

        String coords = String.format("%.0f / %.0f", x, z);
        FADrawHelper.drawMiddleAlignedString(coords, centerX, displayY, color);
    }
}
