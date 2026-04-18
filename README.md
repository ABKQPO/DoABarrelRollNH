# Do a Barrel Roll NH

A port of the [Do a Barrel Roll](https://modrinth.com/mod/do-a-barrel-roll) mod for Minecraft 1.7.10, with integrated [FlightAssistant](https://github.com/Octol1ttle/FlightAssistant) and [ElytraHUD](https://modrinth.com/mod/elytra-hud) modules.

This mod overhauls elytra flight controls, giving the player full 3-axis (pitch, yaw, roll) camera and movement control during elytra flight — just like a real aircraft. The built-in FlightAssistant module adds an aviation-style HUD with speed/altitude tapes, heading indicator, attitude display, GPWS, stall warnings, and autopilot. The ElytraHUD module provides a simpler gauge-style HUD alternative.

## Features

### Do a Barrel Roll (Core)
- **Full 3-axis flight control** — Mouse controls pitch, yaw, and roll simultaneously using Rodrigues rotation
- **Camera roll** — First-person camera tilts to match the player's roll angle
- **Barrel roll** — Dedicated keybinds to perform a full 360° barrel roll with easing animation
- **Configurable sensitivity** — Independent sensitivity multipliers for pitch, yaw, and roll axes
- **Input smoothing** — Exponential smoothing on all axes to reduce jittery camera movement
- **Banking** — Optional automatic roll based on yaw input, simulating coordinated turns
- **Automatic righting** — Optionally levels the roll angle when no roll input is given
- **Axis inversion** — Toggle inversion for each axis independently
- **Roll recovery on landing** — Smooth exponential damping returns the camera to level after flight ends
- **Third-person support** — Player model rotates correctly in third-person view during flight

### FlightAssistant (Aviation HUD)
- **Attitude display** — Artificial horizon with pitch ladder, roll indicator
- **Speed tape** — Scrolling speed scale with numeric readout (blocks/sec)
- **Altitude tape** — Scrolling altitude scale with numeric readout
- **Heading tape** — Compass heading with cardinal directions
- **Flight path vector** — Shows actual flight direction vs nose direction
- **Radar altitude** — Height above ground via raycasting
- **GPWS (Ground Proximity Warning System)** — Sink rate and terrain proximity alerts
- **Stall warning** — Detects low forward speed during flight
- **Void proximity alert** — Warning when approaching the void
- **Elytra durability monitor** — Tracks elytra durability with configurable units (raw/percentage/time)
- **Alert system** — Prioritized WARNING/CAUTION/ADVISORY alerts with blinking indicators
- **Flight protections** — Automatic pitch limits during stall, void proximity, and terrain warnings
- **Autopilot** — AP, FD (Flight Directors), and A/THR (Auto-Thrust) modes with keybinds
- **Flight plan** — Waypoint navigation with course deviation indicator
- **Coordinates display** — Current X/Z position at bottom of HUD
- **Velocity components** — Ground speed and vertical speed readouts
- **Automation modes display** — Shows active AP/FD/A-THR status
- **Fully configurable** — Colors, frame size, individual display toggles, alert modes

### ElytraHUD (Gauge HUD)
- **Airspeed gauge** — Analog gauge showing current speed
- **Altitude gauge** — Dual-pointer gauge for altitude
- **Vertical speed gauge** — Climb/descent rate indicator
- **Firework rate gauge** — Boost rate tracking
- **Elytra durability bar** — Visual durability indicator
- **Compass** — Rotating compass with heading degrees
- **Configurable** — Toggle each gauge, titles, and numeric values independently

### Mutual Exclusion
- FlightAssistant and ElytraHUD cannot be active simultaneously
- If both are enabled in config, FlightAssistant HUD takes priority and ElytraHUD is disabled
- If FlightAssistant HUD is disabled (but safety is on), ElytraHUD can still render

## Dependencies

| Dependency | Required | Purpose |
|---|---|---|
| [Et Futurum Requiem](https://github.com/GTNewHorizons/Et-Futurum-Requiem) | **Yes** | Provides elytra item and flight mechanics for 1.7.10 |
| [GTNHLib](https://github.com/GTNewHorizons/GTNHLib) | **Yes** | Configuration system (`@Config` annotations) |

## Configuration

All settings are available in-game via the Forge config GUI or by editing the config file at `config/DoABarrelRoll/doabarrelroll.cfg`.

### Core Options

| Option | Default | Description |
|---|---|---|
| `modEnabled` | `true` | Master toggle for flight controls |
| `switchRollAndYaw` | `false` | Swap roll and yaw mouse axes |
| `invertPitch / invertYaw / invertRoll` | `false` | Invert individual axes |
| `rollReturnDamping` | `0.85` | How fast the camera levels after landing |
| `yawRateDegPerTick` | `13.5` | Yaw rotation speed in degrees per tick |
| Sensitivity (pitch/yaw/roll) | `1.0 / 0.4 / 0.5` | Per-axis sensitivity multipliers |
| Smoothing (pitch/yaw/roll) | `1.0 / 2.5 / 1.0` | Per-axis smoothing strength |
| Banking strength | `40.0` | How much yaw input causes automatic roll |

### FlightAssistant Options

| Option | Default | Description |
|---|---|---|
| `enabled` | `true` | Enable FlightAssistant module |
| `hudEnabled` | `true` | Enable HUD rendering |
| `safetyEnabled` | `true` | Enable safety systems |
| `frameWidth / frameHeight` | `0.6 / 0.5` | HUD frame size (fraction of screen) |
| `showAttitude` | `2` | 0=Disabled, 1=Horizon only, 2=Horizon + pitch ladder |
| `showHeadingReading/Scale` | `true` | Heading tape and numeric readout |
| `showSpeedReading/Scale` | `true` | Speed tape and numeric readout |
| `showAltitudeReading/Scale` | `true` | Altitude tape and numeric readout |
| Safety alert modes | `3` | 0=Off, 1=Caution, 2=Warning, 3=Both |

### ElytraHUD Options

| Option | Default | Description |
|---|---|---|
| `enabled` | `false` | Enable ElytraHUD module (disabled by default) |
| `renderAirspeed/Altitude/Vertical/Compass` | `true` | Toggle individual gauges |
| `renderTitles` | `true` | Show gauge labels |
| `renderValues` | `false` | Show numeric values on gauges |

## Keybindings

| Action | Default Key | Category |
|---|---|---|
| Roll Left | *Unbound* | Do a Barrel Roll |
| Roll Right | *Unbound* | Do a Barrel Roll |
| Toggle Autopilot | *Unbound* | FlightAssistant |
| Toggle Flight Directors | *Unbound* | FlightAssistant |
| Toggle Auto-Thrust | *Unbound* | FlightAssistant |

## Building

```bash
./gradlew build
```

The output jar will be in `build/libs/`.

## Technical Details

- Uses **Mixin** to inject into `EntityRenderer` (camera roll, mouse input redirect) and `RenderPlayer` (third-person model rotation)
- Flight rotation is computed via **Rodrigues' rotation formula** operating on the player's facing and left vectors
- Camera roll is applied by redirecting the `GL11.glRotatef` call in `orientCamera`
- FlightAssistant captures GL modelview/projection matrices during `RenderWorldLastEvent` for world-to-screen projection via `GLU.gluProject`
- HUD rendering uses `RenderGameOverlayEvent.Post` with GL11 immediate mode drawing
- Client-side only — `acceptableRemoteVersions = "*"` allows connecting to any server

## Credits

- Original mod: [Do a Barrel Roll](https://modrinth.com/mod/do-a-barrel-roll) by Enjarai
- FlightAssistant: [FlightAssistant](https://github.com/Octol1ttle/FlightAssistant) by Octol1ttle
- ElytraHUD: [ElytraHUD](https://modrinth.com/mod/elytra-hud) by jewtvet
- 1.12.2 reference port: [RollTheSky](https://github.com/qiMengStars/RollTheSky) by qiMengStars
- 1.7.10 GTNH port: HFstudio

## License

[GPL-3.0](LICENSE.txt)
