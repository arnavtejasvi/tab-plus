package com.arnav.tabplus;

import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

public final class TabPlusKeys {
    public static final KeyMapping NOTE = new KeyMapping(
        "key.tabplus.note",
        GLFW.GLFW_KEY_N,
        "key.categories.tabplus"
    );

    public static final KeyMapping TAG = new KeyMapping(
        "key.tabplus.tag",
        GLFW.GLFW_KEY_T,
        "key.categories.tabplus"
    );

    private TabPlusKeys() {}

    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(NOTE);
        event.register(TAG);
    }
}
