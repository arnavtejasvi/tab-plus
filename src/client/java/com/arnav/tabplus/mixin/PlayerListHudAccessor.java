package com.arnav.tabplus.mixin;

import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PlayerListHud.class)
public interface PlayerListHudAccessor {
    @Accessor("header")
    @Nullable Text tabplus_getHeader();

    @Accessor("footer")
    @Nullable Text tabplus_getFooter();
}
