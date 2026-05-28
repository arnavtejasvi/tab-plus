package com.arnav.tabplus.screen;

import com.arnav.tabplus.PlayerData;
import com.arnav.tabplus.PlayerDataStore;
import com.arnav.tabplus.RelationshipTag;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.UUID;

public class ProfileScreen extends Screen {
    private final UUID playerUuid;
    private final String playerName;
    private PlayerData data;
    private EditBox noteField;

    private static final int ROW_HEIGHT = 24;

    public ProfileScreen(UUID playerUuid, String playerName) {
        super(Component.literal("Profile"));
        this.playerUuid = playerUuid;
        this.playerName = playerName;
    }

    @Override
    protected void init() {
        data = PlayerDataStore.getInstance().get(playerUuid);

        int cx = width / 2;
        int topY = height / 2 - 50;

        int tagRowY = topY + ROW_HEIGHT * 2;
        addRenderableWidget(Button.builder(Component.literal("<"), btn -> cycleTag(-1))
            .bounds(cx - 60, tagRowY, 20, 20).build());
        addRenderableWidget(Button.builder(Component.literal(">"), btn -> cycleTag(1))
            .bounds(cx + 40, tagRowY, 20, 20).build());

        int noteRowY = topY + ROW_HEIGHT * 4;
        noteField = new EditBox(font, cx - 80, noteRowY, 160, 20, Component.literal("Note..."));
        noteField.setMaxLength(120);
        noteField.setValue(data.note);
        noteField.setFocused(true);
        addRenderableWidget(noteField);

        addRenderableWidget(Button.builder(Component.literal("Done"), btn -> save())
            .bounds(cx - 30, topY + ROW_HEIGHT * 6, 60, 20).build());
    }

    private void cycleTag(int dir) {
        RelationshipTag[] vals = RelationshipTag.values();
        data.tag = vals[Math.floorMod(data.tag.ordinal() + dir, vals.length)];
    }

    private void save() {
        data.note = noteField.getValue().trim();
        PlayerDataStore.getInstance().set(playerUuid, data);
        onClose();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, width, height, 0xC0000000);
        super.extractRenderState(ctx, mouseX, mouseY, delta);

        int cx = width / 2;
        int topY = height / 2 - 50;

        ctx.text(font, playerName, cx - font.width(playerName) / 2, topY, 0xFFFFFF, true);
        ctx.text(font, "— profile —", cx - font.width("— profile —") / 2, topY + 10, 0x666666, true);

        int tagRowY = topY + ROW_HEIGHT * 2;
        ctx.text(font, "Tag", cx - font.width("Tag") / 2, tagRowY - 10, 0xAAAAAA, true);

        String tagLabel = data.tag == RelationshipTag.NONE ? "None" : data.tag.label;
        int tagColor = data.tag == RelationshipTag.NONE ? 0xAAAAAA : data.tag.color;
        ctx.text(font, tagLabel, cx - font.width(tagLabel) / 2, tagRowY + 6, tagColor, true);

        int noteRowY = topY + ROW_HEIGHT * 4;
        ctx.text(font, "Note", cx - font.width("Note") / 2, noteRowY - 10, 0xAAAAAA, true);
    }

    @Override
    public boolean keyPressed(net.minecraft.client.input.KeyEvent event) {
        if (event.key() == 257 || event.key() == 335) { save(); return true; }
        if (event.key() == 256) { onClose(); return true; }
        return super.keyPressed(event);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
