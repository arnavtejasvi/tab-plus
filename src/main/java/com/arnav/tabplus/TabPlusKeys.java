package com.arnav.tabplus;

import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

public final class TabPlusKeys {
    public static final KeyMapping.Category CATEGORY =
        KeyMapping.Category.register(Identifier.fromNamespaceAndPath("tabplus", "general"));

    public static final KeyMapping NOTE = new KeyMapping(
        "key.tabplus.note",
        GLFW.GLFW_KEY_N,
        CATEGORY
    );

    public static final KeyMapping TAG = new KeyMapping(
        "key.tabplus.tag",
        GLFW.GLFW_KEY_T,
        CATEGORY
    );

    private TabPlusKeys() {}

    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.registerCategory(CATEGORY);
        event.register(NOTE);
        event.register(TAG);
    }
}
