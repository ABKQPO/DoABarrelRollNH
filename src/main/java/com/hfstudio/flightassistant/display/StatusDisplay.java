package com.hfstudio.flightassistant.display;

import com.hfstudio.flightassistant.FAConfig;
import com.hfstudio.flightassistant.computer.ComputerHost;
import com.hfstudio.flightassistant.util.FADrawHelper;

/**
 * Displays status messages (e.g., stall, protection active).
 */
public class StatusDisplay extends Display {

    public StatusDisplay(ComputerHost computers) {
        super(computers);
    }

    @Override
    public boolean allowedByConfig() {
        return FAConfig.display.showStatusMessages;
    }

    @Override
    public void render(HudFrame frame, float partial) {
        int centerX = frame.getCenterX();
        int bottom = frame.getBottom();
        int y = bottom + 5;

        // Stall status
        if (computers.stall.isStalling) {
            FADrawHelper.drawHighlightedCenteredText("STALL", centerX, y, FADrawHelper.warningColor());
            y += 12;
        }

        // Protection status
        if (computers.protections.protectionActive) {
            FADrawHelper.drawHighlightedCenteredText("F/CTL PROT", centerX, y, FADrawHelper.cautionColor());
            y += 12;
        }

        // Chunk status
        if (computers.chunkStatus.chunksUnloaded) {
            FADrawHelper.drawHighlightedCenteredText("NAV ACCURACY", centerX, y, FADrawHelper.cautionColor());
        }
    }
}
