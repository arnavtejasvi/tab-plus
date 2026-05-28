package com.arnav.tabplus.mixin;

import com.arnav.tabplus.TabOverlayRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.network.chat.Component;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class InGameHudMixin {
    @Shadow @Final private Minecraft minecraft;
    @Shadow private PlayerTabOverlay tabList;

    @Inject(method = "renderTabList", at = @At("HEAD"), cancellable = true)
    private void onRenderTabList(GuiGraphicsExtractor ctx, CallbackInfo ci) {
        if (!minecraft.options.keyPlayerList.isDown()) return;

        Scoreboard scoreboard = minecraft.level.getScoreboard();
        @Nullable Objective objective = scoreboard.getDisplayObjective(DisplaySlot.LIST);

        PlayerListHudAccessor accessor = (PlayerListHudAccessor) tabList;
        @Nullable Component header = accessor.tabplus_getHeader();
        @Nullable Component footer = accessor.tabplus_getFooter();

        tabList.setVisible(true);
        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        TabOverlayRenderer.render(ctx, screenWidth, scoreboard, objective, header, footer);
        ci.cancel();
    }
}
