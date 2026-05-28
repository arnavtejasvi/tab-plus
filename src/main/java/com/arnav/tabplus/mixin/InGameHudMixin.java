package com.arnav.tabplus.mixin;

import com.arnav.tabplus.TabOverlayRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.network.chat.Component;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerTabOverlay.class)
public abstract class InGameHudMixin {

    @Inject(method = "extractRenderState", at = @At("HEAD"), cancellable = true)
    private void onExtractRenderState(GuiGraphicsExtractor ctx, int screenWidth,
                                      Scoreboard scoreboard, Objective objective, CallbackInfo ci) {
        if (!Minecraft.getInstance().options.keyPlayerList.isDown()) return;

        PlayerTabOverlayAccessor accessor = (PlayerTabOverlayAccessor)(Object)this;
        @Nullable Component header = accessor.tabplus_getHeader();
        @Nullable Component footer = accessor.tabplus_getFooter();

        TabOverlayRenderer.render(ctx, screenWidth, scoreboard, objective, header, footer);
        ci.cancel();
    }
}
