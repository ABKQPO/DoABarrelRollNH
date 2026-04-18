package com.hfstudio.flightassistant.computer;

/**
 * Base class for all flight computers.
 * Computers compute and track flight data, safety parameters, and automation state.
 */
public abstract class Computer {

    public boolean enabled = true;
    public boolean faulted = false;
    public int faultCount = 0;

    /**
     * Called every game tick when the computer is enabled and not faulted.
     */
    public abstract void tick();

    /**
     * Called every render tick (can be multiple times per game tick).
     */
    public void renderTick() {}

    /**
     * Reset the computer's state.
     */
    public abstract void reset();

    public boolean isDisabledOrFaulted() {
        return !enabled || faulted;
    }
}
