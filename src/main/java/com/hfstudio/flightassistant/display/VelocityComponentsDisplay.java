package com.hfstudio.flightassistant.display;

import com.hfstudio.flightassistant.FAConfig;
import com.hfstudio.flightassistant.computer.ComputerHost;
import com.hfstudio.flightassistant.util.FADrawHelper;

/**
 * Displays ground speed and vertical speed components.
 */
public class VelocityComponentsDisplay extends Display {

    public VelocityComponentsDisplay(ComputerHost computers) {
        super(computers);
    }

    @Override
    public boolean allowedByConfig() {
        return FAConfig.display.showGroundSpeed || FAConfig.display.showVerticalSpeed;
    }

    @Override
    public void render(HudFrame frame, float partial) {
        int left = frame.getLeft();
        int bottom = frame.getBottom();
        int color = FADrawHelper.primaryColor();
        int y = bottom + 5;

        if (FAConfig.display.showGroundSpeed) {
            double vx = computers.airData.velocity.xCoord;
            double vz = computers.airData.velocity.zCoord;
            double groundSpeed = Math.sqrt(vx * vx + vz * vz) * 20.0; // blocks/sec
            String gsText = String.format("GS %.0f", groundSpeed);
            FADrawHelper.drawString(gsText, left - 50, y, color);
            y += 10;
        }

        if (FAConfig.display.showVerticalSpeed) {
            double vs = computers.airData.velocity.yCoord * 20.0; // blocks/sec
            int vsColor = vs < -10 ? FADrawHelper.cautionColor() : color;
            String sign = vs >= 0 ? "+" : "";
            String vsText = String.format("VS %s%.0f", sign, vs);
            FADrawHelper.drawString(vsText, left - 50, y, vsColor);
        }
    }
}
