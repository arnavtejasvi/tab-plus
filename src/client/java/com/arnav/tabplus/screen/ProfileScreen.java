package com.arnav.tabplus.screen;

import com.arnav.tabplus.PlayerData;
import com.arnav.tabplus.PlayerDataStore;
import com.arnav.tabplus.RelationshipTag;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.UUID;

public class ProfileScreen extends Screen {
    private final UUID playerUuid;
    private final String playerName;
    private PlayerData data;
    private TextFieldWidget noteField;

    private static final int ROW_HEIGHT = 24;

    public ProfileScreen(UUID playerUuid, String playerName) {
        super(Text.literal("Profile"));
        this.playerUuid = playerUuid;
        this.playerName = playerName;
    }

    @Override
    protected void init() {
        data = PlayerDataStore.getInstance().get(playerUuid);

        int cx = width / 2;
        int topY = height / 2 - 50;

        // tag arrows — row 0
        int tagRowY = topY + ROW_HEIGHT * 2;
        addDrawableChild(ButtonWidget.builder(Text.literal("<"), btn -> cycleTag(-1))
            .dimensions(cx - 60, tagRowY, 20, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal(">"), btn -> cycleTag(1))
            .dimensions(cx + 40, tagRowY, 20, 20).build());

        // note field — row 1
        int noteRowY = topY + ROW_HEIGHT * 4;
        noteField = new TextFieldWidget(textRenderer, cx - 80, noteRowY, 160, 20,
            Text.literal("Note..."));
        noteField.setMaxLength(120);
        noteField.setText(data.note);
        noteField.setFocused(true);
        addDrawableChild(noteField);

        // done button
        addDrawableChild(ButtonWidget.builder(Text.literal("Done"), btn -> save())
            .dimensions(cx - 30, topY + ROW_HEIGHT * 6, 60, 20).build());
    }

    private void cycleTag(int dir) {
        RelationshipTag[] vals = RelationshipTag.values();
        data.tag = vals[Math.floorMod(data.tag.ordinal() + dir, vals.length)];
    }

    private void save() {
        data.note = noteField.getText().trim();
        PlayerDataStore.getInstance().set(playerUuid, data);
        close();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);

        int cx = width / 2;
        int topY = height / 2 - 50;

        // player name header
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(playerName), cx, topY, 0xFFFFFF);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("— profile —"), cx, topY + 10, 0x666666);

        // tag section label
        int tagRowY = topY + ROW_HEIGHT * 2;
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("Tag"), cx, tagRowY - 10, 0xAAAAAA);

        // tag name in its color, between the arrows
        String tagLabel = data.tag == RelationshipTag.NONE ? "None" : data.tag.label;
        int tagColor = data.tag == RelationshipTag.NONE ? 0xAAAAAA : data.tag.color;
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(tagLabel), cx, tagRowY + 6, tagColor);

        // note label
        int noteRowY = topY + ROW_HEIGHT * 4;
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("Note"), cx, noteRowY - 10, 0xAAAAAA);

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 257 || keyCode == 335) { save(); return true; }
        if (keyCode == 256) { close(); return true; }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
