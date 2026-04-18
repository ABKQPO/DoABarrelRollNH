package com.hfstudio.flightassistant.computer;

/**
 * Manages pitch limits from various flight protection sources.
 */
public class PitchComputer extends Computer {

    private final AirDataComputer data;
    private final FlightProtectionsComputer protections;

    public Float minimumPitch = null;
    public Float maximumPitch = null;

    public PitchComputer(AirDataComputer data, FlightProtectionsComputer protections) {
        this.data = data;
        this.protections = protections;
    }

    @Override
    public void tick() {
        minimumPitch = protections.minimumPitch;
        maximumPitch = protections.maximumPitch;
    }

    @Override
    public void reset() {
        minimumPitch = null;
        maximumPitch = null;
    }
}
