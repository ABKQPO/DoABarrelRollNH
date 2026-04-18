package com.hfstudio.flightassistant.display;

import com.hfstudio.flightassistant.FAConfig;
import com.hfstudio.flightassistant.computer.ComputerHost;
import com.hfstudio.flightassistant.util.FADrawHelper;

/**
 * Displays elytra durability below the altitude tape.
 */
public class ElytraDurabilityDisplay extends Display {

    public ElytraDurabilityDisplay(ComputerHost computers) {
        super(computers);
    }

    @Override
    public boolean allowedByConfig() {
        return FAConfig.display.showElytraDurability;
    }

    @Override
    public void render(HudFrame frame, float partial) {
        int right = frame.getRight();
        int bottom = frame.getBottom();
        int color = FADrawHelper.primaryColor();

        String durText = computers.elytraStatus.getDurabilityText();
        float pct = computers.elytraStatus.durabilityPercent;

        // Color based on durability
        if (pct < 0.1f) {
            color = FADrawHelper.warningColor();
        } else if (pct < 0.2f) {
            color = FADrawHelper.cautionColor();
        }

        FADrawHelper.drawString("ELY", right + 10, bottom + 5, color);
        FADrawHelper.drawString(durText, right + 10, bottom + 15, color);

        // Durability bar
        int barWidth = 30;
        int barHeight = 3;
        int barX = right + 10;
        int barY = bottom + 26;
        FADrawHelper.renderOutline(barX, barY, barWidth, barHeight, color);
        int fillWidth = (int) (barWidth * pct);
        FADrawHelper.fill(barX, barY, barX + fillWidth, barY + barHeight, color);
    }
}
