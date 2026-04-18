package com.hfstudio.flightassistant.util;

/**
 * Interpolates a float value towards a target with a given speed.
 */
public class FloatLerper {

    private Float current;

    public Float get(Float target, float speed) {
        if (target == null) {
            current = null;
            return null;
        }
        if (current == null) {
            current = target;
            return current;
        }
        float delta = target - current;
        if (Math.abs(delta) < 0.01f) {
            current = target;
        } else {
            current += delta * Math.min(1.0f, speed);
        }
        return current;
    }

    public void reset() {
        current = null;
    }
}
