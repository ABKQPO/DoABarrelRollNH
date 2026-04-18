package com.hfstudio.flightassistant.display;

import com.hfstudio.flightassistant.FAConfig;
import com.hfstudio.flightassistant.computer.AutoFlightComputer;
import com.hfstudio.flightassistant.computer.ComputerHost;
import com.hfstudio.flightassistant.util.FADrawHelper;

/**
 * Displays current automation modes (AP, FD, A/THR) at the top of the HUD.
 */
public class AutomationModesDisplay extends Display {

    public AutomationModesDisplay(ComputerHost computers) {
        super(computers);
    }

    @Override
    public boolean allowedByConfig() {
        return FAConfig.display.showAutomationModes;
    }

    @Override
    public void render(HudFrame frame, float partial) {
        AutoFlightComputer af = computers.autoFlight;
        int y = frame.getTop() - 30;
        int centerX = frame.getCenterX();
        int color = FADrawHelper.primaryColor();
        int activeColor = FADrawHelper.activeColor();

        // Left column: vertical mode
        int leftX = frame.getLeft();
        if (af.activeVerticalMode != null) {
            FADrawHelper.drawString(af.activeVerticalMode.getModeName(), leftX, y, activeColor);
        }

        // Center: AP status
        if (af.autopilot) {
            FADrawHelper.drawMiddleAlignedString("AP", centerX - 30, y, activeColor);
        }
        if (af.flightDirectors) {
            FADrawHelper.drawMiddleAlignedString("FD", centerX, y, activeColor);
        }
        if (af.autoThrust) {
            FADrawHelper.drawMiddleAlignedString("A/THR", centerX + 30, y, activeColor);
        }

        // Right column: lateral mode
        int rightX = frame.getRight();
        if (af.activeLateralMode != null) {
            FADrawHelper.drawRightAlignedString(af.activeLateralMode.getModeName(), rightX, y, activeColor);
        }
    }
}
