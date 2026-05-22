package com.arnav.tabplus;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import org.lwjgl.glfw.GLFW;

public class TabPlusKeys {
    public static final KeyBinding NOTE = KeyBindingHelper.registerKeyBinding(new KeyBinding(
        "key.tabplus.note",
        GLFW.GLFW_KEY_N,
        "key.categories.tabplus"
    ));

    public static final KeyBinding TAG = KeyBindingHelper.registerKeyBinding(new KeyBinding(
        "key.tabplus.tag",
        GLFW.GLFW_KEY_T,
        "key.categories.tabplus"
    ));

    private TabPlusKeys() {}
}
