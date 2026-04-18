package com.hfstudio.flightassistant.alert;

/**
 * Represents a single alert from the flight computer.
 */
public class Alert {

    public final AlertCategory category;
    public final AlertLevel level;
    public final String id;
    public final String message;
    public boolean active = false;
    public int ticksSinceActive = 0;

    public Alert(AlertCategory category, AlertLevel level, String id, String message) {
        this.category = category;
        this.level = level;
        this.id = id;
        this.message = message;
    }

    public void setActive(boolean nowActive) {
        if (nowActive && !active) {
            ticksSinceActive = 0;
        }
        if (nowActive) {
            ticksSinceActive++;
        }
        active = nowActive;
    }

    /**
     * Alert severity levels.
     */
    public enum AlertLevel {
        /** Red - immediate action required */
        WARNING,
        /** Amber - awareness and possible action */
        CAUTION,
        /** Green/Cyan - informational */
        ADVISORY
    }
}
