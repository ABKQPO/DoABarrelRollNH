package com.hfstudio.doabarrelroll.roll;

/**
 * Approximate implementation of Mojang's modern Smoother.
 * Based on 1.12's MouseFilter, using double precision.
 */
public final class DabrSmoother {

    private static final double ZERO_EPSILON = 1.0e-6;

    private double targetValue;
    private double remainingValue;
    private double lastAmount;
    private boolean lastInputNonZero;

    /**
     * When input stops, reduce the internal backlog (targetValue - remainingValue)
     * to shorten smoothing tail without affecting active turning.
     */
    public void applyStopBoost(double stopBoost) {
        if (stopBoost <= 1.0) {
            return;
        }

        double factor = 1.0 / stopBoost;
        targetValue = remainingValue + (targetValue - remainingValue) * factor;
    }

    public double smooth(double input, double amount, double stopBoost) {
        boolean inputNonZero = Math.abs(input) > ZERO_EPSILON;
        if (!inputNonZero && lastInputNonZero) {
            applyStopBoost(stopBoost);
        }
        lastInputNonZero = inputNonZero;

        targetValue += input;
        input = (targetValue - remainingValue) * amount;
        lastAmount += (input - lastAmount) * 0.5;

        if ((input > 0.0 && input > lastAmount) || (input < 0.0 && input < lastAmount)) {
            input = lastAmount;
        }

        remainingValue += input;
        return input;
    }

    public void clear() {
        targetValue = 0.0;
        remainingValue = 0.0;
        lastAmount = 0.0;
        lastInputNonZero = false;
    }
}
