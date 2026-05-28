package com.arnav.tabplus;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class TabOverlayRenderer {

    private static final int ROW_HEIGHT  = 10;
    private static final int HEAD_SIZE   = 8;
    private static final int HEAD_MARGIN = 3;
    private static final int PING_WIDTH  = 36;
    private static final int COLUMN_GAP  = 6;
    private static final int MAX_COLUMNS = 4;
    private static final int MAX_ROWS    = 20;
    private static final int PADDING     = 4;

    private static final int BG_COLOR    = 0x99000000;
    private static final int HOVER_COLOR = 0x33FFFFFF;
    private static final int WHITE       = 0xFFFFFF;

    private static final Comparator<PlayerInfo> PLAYER_ORDER = Comparator
        .comparing((PlayerInfo e) -> {
            var team = e.getTeam();
            return team != null ? team.getName() : "";
        })
        .thenComparing(e -> e.getProfile().name().toLowerCase());

    private TabOverlayRenderer() {}

    public static void render(GuiGraphics guiGraphics, int screenWidth,
                              Scoreboard scoreboard, @Nullable Objective objective,
                              @Nullable Component header, @Nullable Component footer) {
        Minecraft mc = Minecraft.getInstance();
        ClientPacketListener connection = mc.getConnection();
        if (connection == null) return;

        Font font = mc.font;
        String filterStr = TabFilter.getInstance().getFilter().toLowerCase();

        List<PlayerInfo> players = connection.getListedOnlinePlayers().stream()
            .sorted(PLAYER_ORDER)
            .filter(e -> filterStr.isEmpty() || matchesFilter(e, filterStr))
            .limit((long) MAX_COLUMNS * MAX_ROWS)
            .toList();

        if (players.isEmpty() && filterStr.isEmpty()) return;

        TabFilter.getInstance().setListSize(players.size());
        int selectedIndex = TabFilter.getInstance().getSelectedIndex();

        int columnCount = Math.max(1, (players.size() + MAX_ROWS - 1) / MAX_ROWS);
        int rowCount    = columnCount == 0 ? 0 : (players.size() + columnCount - 1) / columnCount;

        int maxNameWidth = 0;
        for (PlayerInfo e : players) {
            maxNameWidth = Math.max(maxNameWidth, font.width(getDisplayText(e)));
        }
        int entryWidth = HEAD_SIZE + HEAD_MARGIN + maxNameWidth + HEAD_MARGIN + PING_WIDTH;
        entryWidth = Math.max(entryWidth, 80);

        int totalWidth  = entryWidth * columnCount + COLUMN_GAP * (columnCount - 1);
        int totalHeight = rowCount * ROW_HEIGHT;

        int startX = (screenWidth - totalWidth) / 2;
        int startY = 10;

        if (header != null) {
            int hw = font.width(header);
            int hx = (screenWidth - hw - PADDING * 2) / 2;
            guiGraphics.fill(hx, startY - PADDING, hx + hw + PADDING * 2, startY + font.lineHeight + PADDING, BG_COLOR);
            guiGraphics.drawString(font, header, hx + PADDING, startY, WHITE, true);
            startY += font.lineHeight + PADDING * 2 + 2;
        }

        guiGraphics.fill(startX - PADDING, startY - PADDING,
            startX + totalWidth + PADDING, startY + totalHeight + PADDING, BG_COLOR);

        for (int i = 0; i < players.size(); i++) {
            int col = i / MAX_ROWS;
            int row = i % MAX_ROWS;
            int x   = startX + col * (entryWidth + COLUMN_GAP);
            int y   = startY + row * ROW_HEIGHT;

            PlayerInfo entry = players.get(i);
            UUID uuid = entry.getProfile().id();
            PlayerData data = PlayerDataStore.getInstance().get(uuid);

            if (i == selectedIndex) {
                guiGraphics.fill(x - 1, y, x + entryWidth + 1, y + ROW_HEIGHT, HOVER_COLOR);
            }

            Identifier skinTex = entry.getSkin().body().texturePath();
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, skinTex, x, y + 1, 8.0f, 8.0f, 8, 8, 64, 64);
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED_PREMULTIPLIED_ALPHA, skinTex, x, y + 1, 40.0f, 8.0f, 8, 8, 64, 64);

            int nameColor = data.tag != RelationshipTag.NONE ? data.tag.color : WHITE;
            guiGraphics.drawString(font, getDisplayText(entry), x + HEAD_SIZE + HEAD_MARGIN, y + 1, nameColor, false);

            if (data.tag != RelationshipTag.NONE) {
                int dotX = x + HEAD_SIZE + HEAD_MARGIN - 3;
                guiGraphics.fill(dotX, y + 3, dotX + 2, y + 5, data.tag.color | 0xFF000000);
            }

            int ping = entry.getLatency();
            String pingStr = ping < 0 ? "?" : ping + "ms";
            guiGraphics.drawString(font, pingStr, x + entryWidth - font.width(pingStr), y + 1, pingColor(ping), false);
        }

        if (!filterStr.isEmpty() || players.isEmpty()) {
            int filterY = startY + totalHeight + PADDING + 2;
            String filterLabel = "Filter: " + TabFilter.getInstance().getFilter() + "_";
            int fw = font.width(filterLabel);
            int fx = (screenWidth - fw - PADDING * 2) / 2;
            guiGraphics.fill(fx, filterY - 2, fx + fw + PADDING * 2, filterY + font.lineHeight + 2, BG_COLOR);
            guiGraphics.drawString(font, filterLabel, fx + PADDING, filterY, 0xFFDD44, false);
        }

        if (footer != null) {
            int footerY = startY + totalHeight + PADDING + (filterStr.isEmpty() ? 2 : font.lineHeight + 8);
            int fw = font.width(footer);
            int fx = (screenWidth - fw - PADDING * 2) / 2;
            guiGraphics.fill(fx, footerY - PADDING, fx + fw + PADDING * 2, footerY + font.lineHeight + PADDING, BG_COLOR);
            guiGraphics.drawString(font, footer, fx + PADDING, footerY, WHITE, true);
        }
    }

    private static Component getDisplayText(PlayerInfo entry) {
        Component display = entry.getTabListDisplayName();
        if (display != null) return display;
        return Component.literal(entry.getProfile().name());
    }

    private static boolean matchesFilter(PlayerInfo entry, String filter) {
        if (entry.getProfile().name().toLowerCase().contains(filter)) return true;
        UUID uuid = entry.getProfile().id();
        PlayerData data = PlayerDataStore.getInstance().get(uuid);
        return data.tag != RelationshipTag.NONE && data.tag.label.toLowerCase().contains(filter);
    }

    private static int pingColor(int ping) {
        if (ping < 0)   return 0xAAAAAA;
        if (ping < 150) return 0x55FF55;
        if (ping < 300) return 0xFFFF55;
        if (ping < 600) return 0xFF5555;
        return 0xAA0000;
    }
}
