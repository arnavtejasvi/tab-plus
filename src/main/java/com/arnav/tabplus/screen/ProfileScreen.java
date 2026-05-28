package com.arnav.tabplus.screen;

import com.arnav.tabplus.PlayerData;
import com.arnav.tabplus.PlayerDataStore;
import com.arnav.tabplus.RelationshipTag;
import net.minecraft.client.gui.GuiGraphics;
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
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        renderBackground(guiGraphics, mouseX, mouseY, delta);

        int cx = width / 2;
        int topY = height / 2 - 50;

        guiGraphics.drawCenteredString(font, playerName, cx, topY, 0xFFFFFF);
        guiGraphics.drawCenteredString(font, "— profile —", cx, topY + 10, 0x666666);

        int tagRowY = topY + ROW_HEIGHT * 2;
        guiGraphics.drawCenteredString(font, "Tag", cx, tagRowY - 10, 0xAAAAAA);

        String tagLabel = data.tag == RelationshipTag.NONE ? "None" : data.tag.label;
        int tagColor = data.tag == RelationshipTag.NONE ? 0xAAAAAA : data.tag.color;
        guiGraphics.drawCenteredString(font, tagLabel, cx, tagRowY + 6, tagColor);

        int noteRowY = topY + ROW_HEIGHT * 4;
        guiGraphics.drawCenteredString(font, "Note", cx, noteRowY - 10, 0xAAAAAA);

        super.render(guiGraphics, mouseX, mouseY, delta);
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
