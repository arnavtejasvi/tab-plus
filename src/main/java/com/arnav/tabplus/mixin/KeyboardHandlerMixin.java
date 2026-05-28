package com.arnav.tabplus.mixin;

import com.arnav.tabplus.TabFilter;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public abstract class KeyboardHandlerMixin {
    @Shadow @Final private Minecraft minecraft;

    @Inject(method = "charTyped", at = @At("HEAD"), cancellable = true)
    private void onChar(long window, int codepoint, int modifiers, CallbackInfo ci) {
        if (minecraft.screen != null) return;
        if (!minecraft.options.keyPlayerList.isDown()) return;
        if (com.arnav.tabplus.TabPlusKeys.NOTE.isDown()) return;
        if (com.arnav.tabplus.TabPlusKeys.TAG.isDown()) return;
        if (codepoint >= 32 && codepoint < 127) {
            TabFilter.getInstance().appendChar((char) codepoint);
            ci.cancel();
        }
    }

    @Inject(method = "keyPress", at = @At("HEAD"), cancellable = true)
    private void onKey(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        if (minecraft.screen != null) return;
        if (!minecraft.options.keyPlayerList.isDown()) return;
        if (action == GLFW.GLFW_PRESS || action == GLFW.GLFW_REPEAT) {
            switch (key) {
                case GLFW.GLFW_KEY_BACKSPACE -> { TabFilter.getInstance().backspace();       ci.cancel(); }
                case GLFW.GLFW_KEY_ESCAPE    -> { TabFilter.getInstance().clear();           ci.cancel(); }
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
