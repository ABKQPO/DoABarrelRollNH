package com.hfstudio.flightassistant.display;

import java.util.List;

import com.hfstudio.flightassistant.FAConfig;
import com.hfstudio.flightassistant.alert.Alert;
import com.hfstudio.flightassistant.alert.Alert.AlertLevel;
import com.hfstudio.flightassistant.computer.ComputerHost;
import com.hfstudio.flightassistant.util.FADrawHelper;

/**
 * Displays active alerts on the right side of the HUD.
 */
public class AlertDisplay extends Display {

    private static final int MAX_VISIBLE_ALERTS = 5;
    private static final int LINE_HEIGHT = 10;

    public AlertDisplay(ComputerHost computers) {
        super(computers);
    }

    @Override
    public boolean allowedByConfig() {
        return FAConfig.display.showAlerts;
    }

    @Override
    public void render(HudFrame frame, float partial) {
        List<Alert> alerts = computers.alert.activeAlerts;
        if (alerts.isEmpty()) return;

        int x = frame.getRight() + 10;
        int y = frame.getTop();

        // Title
        if (computers.alert.hasWarnings()) {
            FADrawHelper.drawString("WARNING", x, y, FADrawHelper.warningColor());
        } else if (computers.alert.hasCautions()) {
            FADrawHelper.drawString("CAUTION", x, y, FADrawHelper.cautionColor());
        }
        y += LINE_HEIGHT + 2;

        // Alert list
        int count = 0;
        for (Alert alert : alerts) {
            if (count >= MAX_VISIBLE_ALERTS) break;

            int alertColor = switch (alert.level) {
                case WARNING -> FADrawHelper.warningColor();
                case CAUTION -> FADrawHelper.cautionColor();
                default -> FADrawHelper.primaryColor();
            };

            // Blink warnings
            if (alert.level == AlertLevel.WARNING && (alert.ticksSinceActive / 10) % 2 == 0) {
                FADrawHelper.drawString(alert.category.name(), x, y, alertColor);
                FADrawHelper.drawString(alert.message, x + 40, y, alertColor);
            } else if (alert.level != AlertLevel.WARNING) {
                FADrawHelper.drawString(alert.category.name(), x, y, alertColor);
                FADrawHelper.drawString(alert.message, x + 40, y, alertColor);
            }

            y += LINE_HEIGHT;
            count++;
        }
    }
}
