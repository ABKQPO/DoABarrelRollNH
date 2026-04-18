package com.hfstudio.flightassistant.display;

import com.hfstudio.flightassistant.computer.ComputerHost;
import com.hfstudio.flightassistant.util.FADrawHelper;

/**
 * Base class for all FlightAssistant HUD display elements.
 */
public abstract class Display {

    protected final ComputerHost computers;
    protected final FADrawHelper draw;

    public Display(ComputerHost computers) {
        this.computers = computers;
        this.draw = FADrawHelper.INSTANCE;
    }

    /**
     * Whether this display should be active based on config.
     */
    public abstract boolean allowedByConfig();

    /**
     * Render the display.
     *
     * @param frame   the HUD frame dimensions
     * @param partial partial tick for interpolation
     */
    public abstract void render(HudFrame frame, float partial);

    /**
     * Render a faulted display (placeholder when the computer has failed).
     *
     * @param frame the HUD frame dimensions
     */
    public void renderFaulted(HudFrame frame) {
        // Default: render nothing when faulted
    }
}
