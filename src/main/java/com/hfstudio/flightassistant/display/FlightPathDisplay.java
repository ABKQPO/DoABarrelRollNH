package com.hfstudio.flightassistant.display;

import com.hfstudio.flightassistant.FAConfig;
import com.hfstudio.flightassistant.computer.ComputerHost;
import com.hfstudio.flightassistant.util.FADrawHelper;
import com.hfstudio.flightassistant.util.ScreenSpace;

/**
 * Displays the Flight Path Vector (FPV) marker.
 * Shows where the aircraft is actually going, not where it's pointed.
 */
public class FlightPathDisplay extends Display {

    public FlightPathDisplay(ComputerHost computers) {
        super(computers);
    }

    @Override
    public boolean allowedByConfig() {
        return FAConfig.display.showFlightPathVector;
    }

    @Override
    public void render(HudFrame frame, float partial) {
        if (computers.airData.velocity == null) return;

        double vx = computers.airData.velocity.xCoord;
        double vy = computers.airData.velocity.yCoord;
        double vz = computers.airData.velocity.zCoord;
        double speed = Math.sqrt(vx * vx + vy * vy + vz * vz);
        if (speed < 0.01) return;

        // Project velocity vector into screen space (with roll, matching real flight path)
        float[] screen = ScreenSpace.projectWithRoll((float) vx, (float) vy, (float) vz);
        if (screen == null) return;

        int x = (int) screen[0];
        int y = (int) screen[1];

        int color = FADrawHelper.primaryColor();

        // Draw FPV symbol: circle with horizontal tails and a vertical fin
        int radius = 4;
        // Circle approximation using lines
        drawCircle(x, y, radius, color);

        // Horizontal tails
        FADrawHelper.hLine(x - radius - 6, x - radius, y, color);
        FADrawHelper.hLine(x + radius, x + radius + 6, y, color);

        // Vertical fin
        FADrawHelper.vLine(x, y - radius - 4, y - radius, color);
    }

    private void drawCircle(int cx, int cy, int radius, int color) {
        // Draw a circle approximation using small line segments
        int segments = 12;
        for (int i = 0; i < segments; i++) {
            double angle1 = 2 * Math.PI * i / segments;
            double angle2 = 2 * Math.PI * (i + 1) / segments;
            int x1 = cx + (int) (Math.cos(angle1) * radius);
            int y1 = cy + (int) (Math.sin(angle1) * radius);
            int x2 = cx + (int) (Math.cos(angle2) * radius);
            int y2 = cy + (int) (Math.sin(angle2) * radius);

            // Approximate with horizontal/vertical lines
            if (Math.abs(x2 - x1) >= Math.abs(y2 - y1)) {
                FADrawHelper.hLine(Math.min(x1, x2), Math.max(x1, x2), (y1 + y2) / 2, color);
            } else {
                FADrawHelper.vLine((x1 + x2) / 2, Math.min(y1, y2), Math.max(y1, y2), color);
            }
        }
    }
}
