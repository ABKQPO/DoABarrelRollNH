package com.hfstudio.flightassistant.display;

import com.hfstudio.flightassistant.FAConfig;
import com.hfstudio.flightassistant.computer.ComputerHost;
import com.hfstudio.flightassistant.computer.GroundProximityComputer;
import com.hfstudio.flightassistant.util.FADrawHelper;

/**
 * Displays radar altitude (height above ground) below the altitude tape.
 */
public class RadarAltitudeDisplay extends Display {

    public RadarAltitudeDisplay(ComputerHost computers) {
        super(computers);
    }

    @Override
    public boolean allowedByConfig() {
        return FAConfig.display.showRadarAltitude;
    }

    @Override
    public void render(HudFrame frame, float partial) {
        GroundProximityComputer gpws = computers.gpws;

        if (gpws.radarAltitude == Double.MAX_VALUE) return;
        // Only show radar altitude when below a reasonable height
        if (gpws.radarAltitude > 500) return;

        int right = frame.getRight();
        int centerY = frame.getCenterY();
        int color = FADrawHelper.primaryColor();

        if (gpws.radarAltitude < 20) {
            color = FADrawHelper.cautionColor();
        }
        if (gpws.radarAltitude < 10) {
            color = FADrawHelper.warningColor();
        }

        String raText = String.format("RA %d", (int) gpws.radarAltitude);
        FADrawHelper.drawString(raText, right + 10, centerY + 15, color);
    }
}
