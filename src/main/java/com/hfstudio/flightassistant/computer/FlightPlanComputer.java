package com.hfstudio.flightassistant.computer;

import java.util.ArrayList;
import java.util.List;

import com.hfstudio.flightassistant.FAConfig;

/**
 * Manages flight plan waypoints and navigation.
 */
public class FlightPlanComputer extends Computer {

    private final AirDataComputer data;

    public List<Waypoint> waypoints = new ArrayList<>();
    public int currentWaypointIndex = -1;
    public Waypoint targetWaypoint = null;
    public double distanceToTarget = 0;
    public float bearingToTarget = 0;
    public float courseDeviation = 0;
    public boolean belowGlideSlope = false;

    private static final double WAYPOINT_REACH_DISTANCE = 10.0;
    private static final double GLIDE_SLOPE_ANGLE = 3.0; // degrees

    public FlightPlanComputer(AirDataComputer data) {
        this.data = data;
    }

    @Override
    public void tick() {
        if (!data.isFlying() || waypoints.isEmpty()) {
            targetWaypoint = null;
            return;
        }

        if (currentWaypointIndex < 0 || currentWaypointIndex >= waypoints.size()) {
            currentWaypointIndex = 0;
        }

        targetWaypoint = waypoints.get(currentWaypointIndex);

        double dx = targetWaypoint.x - data.getX();
        double dz = targetWaypoint.z - data.getZ();
        distanceToTarget = Math.sqrt(dx * dx + dz * dz);

        bearingToTarget = (float) Math.toDegrees(Math.atan2(dx, dz));
        if (bearingToTarget < 0) bearingToTarget += 360;

        courseDeviation = bearingToTarget - data.getHeading();
        if (courseDeviation > 180) courseDeviation -= 360;
        if (courseDeviation < -180) courseDeviation += 360;

        // Glide slope check: if waypoint has altitude, check if current altitude is below optimal path
        belowGlideSlope = false;
        if (targetWaypoint.altitude > 0 && distanceToTarget > WAYPOINT_REACH_DISTANCE) {
            double altDiff = data.getAltitude() - targetWaypoint.altitude;
            double optimalAltAboveTarget = Math.tan(Math.toRadians(GLIDE_SLOPE_ANGLE)) * distanceToTarget;
            if (altDiff < optimalAltAboveTarget - 5.0) {
                int gsMode = FAConfig.safety.belowGlideSlopeAlertMode;
                belowGlideSlope = gsMode > 0;
            }
        }

        // Auto-advance to next waypoint
        if (distanceToTarget < WAYPOINT_REACH_DISTANCE) {
            if (currentWaypointIndex < waypoints.size() - 1) {
                currentWaypointIndex++;
            }
        }
    }

    @Override
    public void reset() {
        targetWaypoint = null;
        distanceToTarget = 0;
        bearingToTarget = 0;
        courseDeviation = 0;
        belowGlideSlope = false;
    }

    /**
     * Add a waypoint to the flight plan.
     */
    public void addWaypoint(double x, double z, String name) {
        addWaypoint(x, z, 0, name);
    }

    /**
     * Add a waypoint with altitude to the flight plan.
     */
    public void addWaypoint(double x, double z, double altitude, String name) {
        waypoints.add(new Waypoint(x, z, altitude, name));
        if (currentWaypointIndex < 0) {
            currentWaypointIndex = 0;
        }
    }

    /**
     * Clear all waypoints.
     */
    public void clearPlan() {
        waypoints.clear();
        currentWaypointIndex = -1;
        targetWaypoint = null;
    }

    /**
     * A navigation waypoint.
     */
    public static class Waypoint {

        public final double x;
        public final double z;
        public final double altitude;
        public final String name;

        public Waypoint(double x, double z, double altitude, String name) {
            this.x = x;
            this.z = z;
            this.altitude = altitude;
            this.name = name;
        }
    }
}
