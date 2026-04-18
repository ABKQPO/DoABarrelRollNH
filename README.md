# Do a Barrel Roll NH

A port of the [Do a Barrel Roll](https://modrinth.com/mod/do-a-barrel-roll) mod for Minecraft 1.7.10.

This mod overhauls elytra flight controls, giving the player full 3-axis (pitch, yaw, roll) camera and movement control during elytra flight — just like a real aircraft.

## Features

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

## Dependencies

| Dependency | Required | Purpose |
|---|---|---|
| [Et Futurum Requiem](https://github.com/GTNewHorizons/Et-Futurum-Requiem) | **Yes** | Provides elytra item and flight mechanics for 1.7.10 |
| [GTNHLib](https://github.com/GTNewHorizons/GTNHLib) | **Yes** | Configuration system (`@Config` annotations) |

## Configuration

All settings are available in-game via the Forge config GUI or by editing the config file at `config/DoABarrelRoll/doabarrelroll.cfg`.

Key options:

| Option | Default | Description |
|---|---|---|
| `modEnabled` | `true` | Master toggle for the mod |
| `switchRollAndYaw` | `false` | Swap roll and yaw mouse axes |
| `invertPitch / invertYaw / invertRoll` | `false` | Invert individual axes |
| `rollReturnDamping` | `0.85` | How fast the camera levels after landing (lower = faster) |
| `yawRateDegPerTick` | `13.5` | Yaw rotation speed in degrees per tick |
| Sensitivity (pitch/yaw/roll) | `1.0 / 0.4 / 0.5` | Per-axis sensitivity multipliers |
| Smoothing (pitch/yaw/roll) | `1.0 / 2.5 / 1.0` | Per-axis smoothing strength |
| Banking strength | `40.0` | How much yaw input causes automatic roll |

## Keybindings

| Action | Default Key |
|---|---|
| Roll Left | *Unbound* |
| Roll Right | *Unbound* |

Barrel rolls are triggered by tapping the roll keybinds during elytra flight.

## Building

```bash
./gradlew build
```

The output jar will be in `build/libs/`.

## Technical Details

- Uses **Mixin** to inject into `EntityRenderer` (camera roll, mouse input redirect) and `RenderPlayer` (third-person model rotation)
- Flight rotation is computed via **Rodrigues' rotation formula** operating on the player's facing and left vectors
- Camera roll is applied by redirecting the `GL11.glRotatef` call in `orientCamera`
- Client-side only — `acceptableRemoteVersions = "*"` allows connecting to any server

## Credits

- Original mod: [Do a Barrel Roll](https://modrinth.com/mod/do-a-barrel-roll) by Enjarai
- 1.12.2 reference port: [RollTheSky](https://github.com/qiMengStars/RollTheSky) by qiMengStars
- 1.7.10 GTNH port: HFstudio

## License

[GPL-3.0](LICENSE.txt)
