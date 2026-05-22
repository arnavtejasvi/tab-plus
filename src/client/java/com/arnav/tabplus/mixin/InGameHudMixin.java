package com.arnav.tabplus.mixin;

import com.arnav.tabplus.TabOverlayRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
    @Shadow @Final private MinecraftClient client;
    @Shadow private PlayerListHud playerListHud;

    @Inject(method = "renderPlayerList", at = @At("HEAD"), cancellable = true)
    private void onRenderPlayerList(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (!client.options.playerListKey.isPressed()) return;

        Scoreboard scoreboard = client.world.getScoreboard();
        @Nullable ScoreboardObjective objective = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.LIST);

        PlayerListHudAccessor accessor = (PlayerListHudAccessor) playerListHud;
        @Nullable Text header = accessor.tabplus_getHeader();
        @Nullable Text footer = accessor.tabplus_getFooter();

        playerListHud.setVisible(true);
        TabOverlayRenderer.render(context, context.getScaledWindowWidth(), scoreboard, objective, header, footer);
        ci.cancel();
    }
}
