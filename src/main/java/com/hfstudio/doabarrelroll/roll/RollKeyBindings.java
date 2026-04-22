package com.hfstudio.doabarrelroll.roll;

import net.minecraft.client.settings.KeyBinding;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.client.registry.ClientRegistry;

/**
 * Barrel roll keybindings.
 * Provides left/right barrel roll keys, unbound by default.
 */
public final class RollKeyBindings {

    private static final String CATEGORY = "key.categories.doabarrelroll";

    private static final KeyBinding ROLL_LEFT = new KeyBinding(
        "key.doabarrelroll.roll_left",
        Keyboard.KEY_NONE,
        CATEGORY);
    private static final KeyBinding ROLL_RIGHT = new KeyBinding(
        "key.doabarrelroll.roll_right",
        Keyboard.KEY_NONE,
        CATEGORY);

    private static boolean registered;

    private RollKeyBindings() {}

    public static void register() {
        if (registered) {
            return;
        }

        ClientRegistry.registerKeyBinding(ROLL_LEFT);
        ClientRegistry.registerKeyBinding(ROLL_RIGHT);
        registered = true;
    }

    public static int consumeBarrelRollDirection() {
        int direction = 0;

        while (ROLL_LEFT.isPressed()) {
            direction--;
        }
        while (ROLL_RIGHT.isPressed()) {
            direction++;
        }

        if (direction < 0) {
            return -1;
        }
        if (direction > 0) {
            return 1;
        }
        return 0;
    }
}
