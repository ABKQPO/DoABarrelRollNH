package com.hfstudio.doabarrelroll.roll;

/**
 * Flight control input axis mapping.
 * Handles axis swapping and inversion between mouse and keyboard inputs.
 */
public final class FlightControlInput {

    private FlightControlInput() {}

    public static Axes resolve(double mousePitchInput, double mouseYawInput, double leftRightKeyAxis,
        boolean switchRollAndYaw, boolean invertPitch, boolean invertYaw, boolean invertRoll) {
        double pitch = mousePitchInput;
        double yaw;
        double roll;

        if (switchRollAndYaw) {
            yaw = mouseYawInput;
            roll = leftRightKeyAxis;
        } else {
            yaw = leftRightKeyAxis;
            roll = mouseYawInput;
        }

        if (invertPitch) {
            pitch = -pitch;
        }
        if (invertYaw) {
            yaw = -yaw;
        }
        if (invertRoll) {
            roll = -roll;
        }

        return new Axes(pitch, yaw, roll);
    }

    public static final class Axes {

        private final double pitch;
        private final double yaw;
        private final double roll;

        private Axes(double pitch, double yaw, double roll) {
            this.pitch = pitch;
            this.yaw = yaw;
            this.roll = roll;
        }

        public double getPitch() {
            return pitch;
        }

        public double getYaw() {
            return yaw;
        }

        public double getRoll() {
            return roll;
        }
    }
}
