package com.arnav.tabplus.mixin;

import com.arnav.tabplus.TabFilter;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public abstract class KeyboardMixin {
    @Shadow @Final private MinecraftClient client;

    @Inject(method = "onChar", at = @At("HEAD"), cancellable = true)
    private void onChar(long window, int codepoint, int modifiers, CallbackInfo ci) {
        if (client.currentScreen != null) return;
        if (!client.options.playerListKey.isPressed()) return;
        if (com.arnav.tabplus.TabPlusKeys.NOTE.isPressed()) return;
        if (com.arnav.tabplus.TabPlusKeys.TAG.isPressed()) return;
        if (codepoint >= 32 && codepoint < 127) {
            TabFilter.getInstance().appendChar((char) codepoint);
            ci.cancel();
        }
    }

    @Inject(method = "onKey", at = @At("HEAD"), cancellable = true)
    private void onKey(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        if (client.currentScreen != null) return;
        if (!client.options.playerListKey.isPressed()) return;
        if (action == GLFW.GLFW_PRESS || action == GLFW.GLFW_REPEAT) {
            switch (key) {
                case GLFW.GLFW_KEY_BACKSPACE -> { TabFilter.getInstance().backspace(); ci.cancel(); }
                case GLFW.GLFW_KEY_ESCAPE    -> { TabFilter.getInstance().clear();     ci.cancel(); }
                case GLFW.GLFW_KEY_UP        -> { TabFilter.getInstance().moveSelection(-1); ci.cancel(); }
                case GLFW.GLFW_KEY_DOWN      -> { TabFilter.getInstance().moveSelection(1);  ci.cancel(); }
                case GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_KP_ENTER -> {
                    TabFilter.getInstance().requestOpenProfile();
                    ci.cancel();
                }
            }
        }
    }
}
