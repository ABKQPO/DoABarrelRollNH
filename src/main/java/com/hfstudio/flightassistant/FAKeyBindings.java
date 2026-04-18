package com.hfstudio.flightassistant;

import net.minecraft.client.settings.KeyBinding;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.client.registry.ClientRegistry;

/**
 * Key bindings for FlightAssistant autopilot controls.
 */
public class FAKeyBindings {

    private static final String CATEGORY = "key.categories.flightassistant";

    public static KeyBinding toggleAP;
    public static KeyBinding toggleFD;
    public static KeyBinding toggleATHR;

    public static void register() {
        toggleAP = new KeyBinding("key.flightassistant.toggleAP", Keyboard.KEY_NONE, CATEGORY);
        toggleFD = new KeyBinding("key.flightassistant.toggleFD", Keyboard.KEY_NONE, CATEGORY);
        toggleATHR = new KeyBinding("key.flightassistant.toggleATHR", Keyboard.KEY_NONE, CATEGORY);

        ClientRegistry.registerKeyBinding(toggleAP);
        ClientRegistry.registerKeyBinding(toggleFD);
        ClientRegistry.registerKeyBinding(toggleATHR);
    }

    /**
     * Process key presses. Called from client tick.
     */
    public static void processKeyBindings(FAEventHandler handler) {
        if (toggleAP.isPressed()) {
            handler.getComputers().autoFlight.toggleAP();
        }
        if (toggleFD.isPressed()) {
            handler.getComputers().autoFlight.toggleFD();
        }
        if (toggleATHR.isPressed()) {
            handler.getComputers().autoFlight.toggleATHR();
        }
    }
}
