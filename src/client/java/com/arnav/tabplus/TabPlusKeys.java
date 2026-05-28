package com.arnav.tabplus;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;

@Environment(EnvType.CLIENT)
public class TabPlusKeys {
    public static final KeyMapping NOTE = KeyMappingHelper.registerKeyMapping(
        new KeyMapping("key.tabplus.note", InputConstants.Type.KEYSYM, 78, KeyMapping.Category.MISC));

    public static final KeyMapping TAG = KeyMappingHelper.registerKeyMapping(
        new KeyMapping("key.tabplus.tag", InputConstants.Type.KEYSYM, 84, KeyMapping.Category.MISC));

    private TabPlusKeys() {}
}
