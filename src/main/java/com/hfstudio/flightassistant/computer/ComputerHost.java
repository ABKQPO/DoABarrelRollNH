package com.hfstudio.flightassistant.computer;

/**
 * Manages all flight computers and coordinates their lifecycle.
 */
import com.hfstudio.flightassistant.util.FATickCounter;

public class ComputerHost {

    public final AirDataComputer airData;
    public final HudDisplayDataComputer displayData;
    public final StallComputer stall;
    public final GroundProximityComputer gpws;
    public final VoidProximityComputer voidProximity;
    public final ElytraStatusComputer elytraStatus;
    public final ChunkStatusComputer chunkStatus;
    public final FireworkComputer firework;
    public final FlightProtectionsComputer protections;
    public final AutoFlightComputer autoFlight;
    public final FlightPlanComputer flightPlan;
    public final PitchComputer pitch;
    public final HeadingComputer heading;
    public final RollComputer roll;
    public final ThrustComputer thrust;
    public final AlertComputer alert;

    private final Computer[] allComputers;

    public ComputerHost() {
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getMinecraft();
        airData = new AirDataComputer(mc);
        displayData = new HudDisplayDataComputer(mc, airData);
        stall = new StallComputer(airData);
        gpws = new GroundProximityComputer(airData);
        voidProximity = new VoidProximityComputer(airData);
        elytraStatus = new ElytraStatusComputer(airData);
        chunkStatus = new ChunkStatusComputer(airData);
        firework = new FireworkComputer(airData);
        protections = new FlightProtectionsComputer(airData, stall, voidProximity, gpws);
        autoFlight = new AutoFlightComputer(airData);
        flightPlan = new FlightPlanComputer(airData);
        pitch = new PitchComputer(airData, protections);
        heading = new HeadingComputer(airData, autoFlight);
        roll = new RollComputer(airData, autoFlight, heading);
        thrust = new ThrustComputer(airData, autoFlight, firework, stall, voidProximity, gpws);
        alert = new AlertComputer(
            airData,
            stall,
            gpws,
            voidProximity,
            elytraStatus,
            chunkStatus,
            firework,
            protections,
            autoFlight,
            flightPlan);

        allComputers = new Computer[] { airData, displayData, stall, gpws, voidProximity, elytraStatus, chunkStatus,
            firework, protections, autoFlight, flightPlan, pitch, heading, roll, thrust, alert };
    }

    /**
     * Called every client tick.
     */
    public void tick() {
        for (Computer computer : allComputers) {
            if (computer.enabled) {
                try {
                    computer.tick();
                    computer.faulted = false;
                } catch (Exception e) {
                    computer.faultCount++;
                    computer.faulted = true;
                    if (computer.faultCount > 10) {
                        computer.enabled = false;
                    }
                }
            }
        }
    }

    /**
     * Called every render frame for smooth interpolation.
     */
    public void renderTick(float partialTick) {
        FATickCounter.partialTick = partialTick;
        for (Computer computer : allComputers) {
            if (computer.enabled) {
                computer.renderTick();
            }
        }
    }

    /**
     * Reset all computers (e.g., on world change).
     */
    public void resetAll() {
        for (Computer computer : allComputers) {
            computer.reset();
            computer.enabled = true;
            computer.faulted = false;
            computer.faultCount = 0;
        }
    }
}
