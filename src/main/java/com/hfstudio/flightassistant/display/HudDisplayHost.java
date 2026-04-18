package com.hfstudio.flightassistant.display;

import java.util.ArrayList;
import java.util.List;

import com.hfstudio.flightassistant.FAConfig;
import com.hfstudio.flightassistant.computer.ComputerHost;

/**
 * Manages all HUD display elements and their rendering.
 */
public class HudDisplayHost {

    private final HudFrame frame;
    private final List<Display> displays = new ArrayList<>();

    public HudDisplayHost(ComputerHost computers) {
        this.frame = new HudFrame();

        // Create all displays (order matters for rendering)
        displays.add(new AttitudeDisplay(computers));
        displays.add(new SpeedDisplay(computers));
        displays.add(new AltitudeDisplay(computers));
        displays.add(new HeadingDisplay(computers));
        displays.add(new FlightPathDisplay(computers));
        displays.add(new AlertDisplay(computers));
        displays.add(new AutomationModesDisplay(computers));
        displays.add(new CoordinatesDisplay(computers));
        displays.add(new CourseDeviationDisplay(computers));
        displays.add(new ElytraDurabilityDisplay(computers));
        displays.add(new FlightDirectorsDisplay(computers));
        displays.add(new RadarAltitudeDisplay(computers));
        displays.add(new StatusDisplay(computers));
        displays.add(new VelocityComponentsDisplay(computers));
    }

    /**
     * Render all HUD displays.
     */
    public void render(int screenWidth, int screenHeight, float partial) {
        if (!FAConfig.hudEnabled) return;

        frame.update(screenWidth, screenHeight);

        for (Display display : displays) {
            if (display.allowedByConfig()) {
                try {
                    display.render(frame, partial);
                } catch (Exception e) {
                    display.renderFaulted(frame);
                }
            }
        }
    }
}
