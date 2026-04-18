package com.hfstudio.flightassistant.display;

import com.hfstudio.flightassistant.FAConfig;
import com.hfstudio.flightassistant.computer.ComputerHost;
import com.hfstudio.flightassistant.computer.FlightPlanComputer;
import com.hfstudio.flightassistant.util.FADrawHelper;

/**
 * Displays course deviation indicator relative to current flight plan waypoint.
 */
public class CourseDeviationDisplay extends Display {

    public CourseDeviationDisplay(ComputerHost computers) {
        super(computers);
    }

    @Override
    public boolean allowedByConfig() {
        return FAConfig.display.showCourseDeviation;
    }

    @Override
    public void render(HudFrame frame, float partial) {
        FlightPlanComputer fp = computers.flightPlan;
        if (fp.targetWaypoint == null) return;

        int centerX = frame.getCenterX();
        int bottom = frame.getBottom();
        int y = bottom + 15;
        int color = FADrawHelper.primaryColor();

        // Course deviation bar
        float deviation = fp.courseDeviation;
        int maxDevPixels = frame.getFrameWidth() / 4;
        int devPixels = (int) Math.max(-maxDevPixels, Math.min(maxDevPixels, deviation * 2));

        // Draw scale dots
        for (int i = -2; i <= 2; i++) {
            int dotX = centerX + i * (maxDevPixels / 2);
            FADrawHelper.fill(dotX - 1, y - 1, dotX + 1, y + 1, color);
        }

        // Draw deviation diamond
        int diamondX = centerX + devPixels;
        FADrawHelper.fill(diamondX - 2, y - 2, diamondX + 2, y + 2, FADrawHelper.activeColor());

        // Waypoint name
        if (fp.targetWaypoint.name != null) {
            FADrawHelper.drawMiddleAlignedString(fp.targetWaypoint.name, centerX, y + 5, color);
        }

        // Distance
        String dist = String.format("%.0f", fp.distanceToTarget);
        FADrawHelper.drawMiddleAlignedString(dist, centerX, y + 15, color);
    }
}
