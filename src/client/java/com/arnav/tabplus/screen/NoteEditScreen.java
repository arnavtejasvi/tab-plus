package com.arnav.tabplus.screen;

import com.arnav.tabplus.PlayerData;
import com.arnav.tabplus.PlayerDataStore;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.UUID;

public class NoteEditScreen extends Screen {
    private final UUID playerUuid;
    private final String playerName;
    private TextFieldWidget noteField;

    public NoteEditScreen(UUID playerUuid, String playerName) {
        super(Text.translatable("tabplus.note.title"));
        this.playerUuid = playerUuid;
        this.playerName = playerName;
    }

    @Override
    protected void init() {
        int centerX = width / 2;
        int centerY = height / 2;

        noteField = new TextFieldWidget(textRenderer, centerX - 100, centerY - 10, 200, 20,
            Text.translatable("tabplus.note.placeholder"));
        noteField.setMaxLength(120);
        noteField.setText(PlayerDataStore.getInstance().get(playerUuid).note);
        noteField.setFocused(true);
        addDrawableChild(noteField);

        addDrawableChild(ButtonWidget.builder(Text.translatable("tabplus.note.save"), btn -> save())
            .dimensions(centerX - 105, centerY + 16, 100, 20)
            .build());

        addDrawableChild(ButtonWidget.builder(Text.translatable("tabplus.note.clear"), btn -> {
            noteField.setText("");
            save();
        }).dimensions(centerX + 5, centerY + 16, 100, 20).build());
    }

    private void save() {
        PlayerData data = PlayerDataStore.getInstance().get(playerUuid);
        data.note = noteField.getText().trim();
        PlayerDataStore.getInstance().set(playerUuid, data);
        close();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        int centerX = width / 2;
        int centerY = height / 2;
        context.drawCenteredTextWithShadow(textRenderer, title, centerX, centerY - 30, 0xFFFFFF);
        context.drawCenteredTextWithShadow(textRenderer,
            Text.literal(playerName), centerX, centerY - 20, 0xAAAAAA);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 257 || keyCode == 335) { // Enter / numpad enter
            save();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
