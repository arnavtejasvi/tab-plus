package com.arnav.tabplus;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.SkinTextures;

import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class TabOverlayRenderer {

    private static final Logger LOGGER = LoggerFactory.getLogger("tabplus");
    private static final int ROW_HEIGHT = 10;
    private static final int HEAD_SIZE = 8;
    private static final int HEAD_MARGIN = 3;
    private static final int PING_WIDTH = 36;
    private static final int COLUMN_GAP = 6;
    private static final int MAX_COLUMNS = 4;
    private static final int MAX_ROWS = 20;
    private static final int PADDING = 4;

    private static final int BG_COLOR     = 0x99000000;
    private static final int HOVER_COLOR  = 0x33FFFFFF;
    private static final int WHITE        = 0xFFFFFF;
    private static final int GRAY         = 0xAAAAAA;

    private static final Comparator<PlayerListEntry> PLAYER_ORDER = Comparator
        .comparing((PlayerListEntry e) -> {
            var team = e.getScoreboardTeam();
            return team != null ? team.getName() : "";
        })
        .thenComparing(e -> e.getProfile().getName().toLowerCase());

    private TabOverlayRenderer() {}

    public static void render(DrawContext context, int screenWidth,
                              Scoreboard scoreboard, @Nullable ScoreboardObjective objective,
                              @Nullable Text header, @Nullable Text footer) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayNetworkHandler handler = client.getNetworkHandler();
        if (handler == null) { LOGGER.info("[Tab+] handler null"); return; }

        TextRenderer tr = client.textRenderer;

        // --- collect + filter players ---
        String filterStr = TabFilter.getInstance().getFilter().toLowerCase();
        LOGGER.info("[Tab+] render called, playerCount={}", handler.getListedPlayerListEntries().size());
        List<PlayerListEntry> players = handler.getListedPlayerListEntries().stream()
            .sorted(PLAYER_ORDER)
            .filter(e -> filterStr.isEmpty() || matchesFilter(e, filterStr))
            .limit((long) MAX_COLUMNS * MAX_ROWS)
            .toList();

        if (players.isEmpty() && filterStr.isEmpty()) return;

        TabFilter.getInstance().setListSize(players.size());
        int selectedIndex = TabFilter.getInstance().getSelectedIndex();

        // --- layout ---
        int columnCount = Math.max(1, (players.size() + MAX_ROWS - 1) / MAX_ROWS);
        int rowCount    = columnCount == 0 ? 0 : (players.size() + columnCount - 1) / columnCount;

        int maxNameWidth = 0;
        for (PlayerListEntry e : players) {
            maxNameWidth = Math.max(maxNameWidth, tr.getWidth(getDisplayText(e)));
        }
        int entryWidth = HEAD_SIZE + HEAD_MARGIN + maxNameWidth + HEAD_MARGIN + PING_WIDTH;
        entryWidth = Math.max(entryWidth, 80);

        int totalWidth  = entryWidth * columnCount + COLUMN_GAP * (columnCount - 1);
        int totalHeight = rowCount * ROW_HEIGHT;

        int startX = (screenWidth - totalWidth) / 2;
        int startY = 10;

        // --- header ---
        if (header != null) {
            int hw = tr.getWidth(header);
            int hx = (screenWidth - hw - PADDING * 2) / 2;
            context.fill(hx, startY - PADDING, hx + hw + PADDING * 2, startY + tr.fontHeight + PADDING, BG_COLOR);
            context.drawText(tr, header, hx + PADDING, startY, WHITE, true);
            startY += tr.fontHeight + PADDING * 2 + 2;
        }

        // --- player list background ---
        context.fill(startX - PADDING, startY - PADDING,
            startX + totalWidth + PADDING, startY + totalHeight + PADDING, BG_COLOR);

        // --- draw players ---
        for (int i = 0; i < players.size(); i++) {
            int col = i / MAX_ROWS;
            int row = i % MAX_ROWS;
            int x   = startX + col * (entryWidth + COLUMN_GAP);
            int y   = startY + row * ROW_HEIGHT;

            PlayerListEntry entry = players.get(i);
            UUID uuid = entry.getProfile().getId();
            PlayerData data = PlayerDataStore.getInstance().get(uuid);

            if (i == selectedIndex) {
                context.fill(x - 1, y, x + entryWidth + 1, y + ROW_HEIGHT, HOVER_COLOR);
            }

            // player head
            SkinTextures skin = entry.getSkinTextures();
            Identifier skinTex = skin.texture();
            context.drawTexture(skinTex, x, y + 1, HEAD_SIZE, HEAD_SIZE, 8.0f, 8.0f, 8, 8, 64, 64);
            // hat layer
            context.drawTexture(skinTex, x, y + 1, HEAD_SIZE, HEAD_SIZE, 40.0f, 8.0f, 8, 8, 64, 64);

            // name
            int nameColor = data.tag != RelationshipTag.NONE ? data.tag.color : WHITE;
            Text displayText = getDisplayText(entry);
            context.drawText(tr, displayText, x + HEAD_SIZE + HEAD_MARGIN, y + 1, nameColor, false);

            // tag dot (small colored square if tagged)
            if (data.tag != RelationshipTag.NONE) {
                int dotX = x + HEAD_SIZE + HEAD_MARGIN - 3;
                context.fill(dotX, y + 3, dotX + 2, y + 5, data.tag.color | 0xFF000000);
            }

            // ping
            int ping = entry.getLatency();
            String pingStr = ping < 0 ? "?" : ping + "ms";
            int pingColor = pingColor(ping);
            context.drawText(tr, pingStr, x + entryWidth - tr.getWidth(pingStr), y + 1, pingColor, false);
        }

        // --- filter bar ---
        if (!filterStr.isEmpty() || players.isEmpty()) {
            int filterY = startY + totalHeight + PADDING + 2;
            String filterLabel = "Filter: " + TabFilter.getInstance().getFilter() + "_";
            int fw = tr.getWidth(filterLabel);
            int fx = (screenWidth - fw - PADDING * 2) / 2;
            context.fill(fx, filterY - 2, fx + fw + PADDING * 2, filterY + tr.fontHeight + 2, BG_COLOR);
            context.drawText(tr, filterLabel, fx + PADDING, filterY, 0xFFDD44, false);
        }

        // --- footer ---
        if (footer != null) {
            int footerY = startY + totalHeight + PADDING + (filterStr.isEmpty() ? 2 : tr.fontHeight + 8);
            int fw = tr.getWidth(footer);
            int fx = (screenWidth - fw - PADDING * 2) / 2;
            context.fill(fx, footerY - PADDING, fx + fw + PADDING * 2, footerY + tr.fontHeight + PADDING, BG_COLOR);
            context.drawText(tr, footer, fx + PADDING, footerY, WHITE, true);
        }

    }

    private static Text getDisplayText(PlayerListEntry entry) {
        Text display = entry.getDisplayName();
        if (display != null) return display;
        return Text.literal(entry.getProfile().getName());
    }

    private static boolean matchesFilter(PlayerListEntry entry, String filter) {
        String name = entry.getProfile().getName().toLowerCase();
        if (name.contains(filter)) return true;
        UUID uuid = entry.getProfile().getId();
        PlayerData data = PlayerDataStore.getInstance().get(uuid);
        if (data.tag != RelationshipTag.NONE && data.tag.label.toLowerCase().contains(filter)) return true;
        return false;
    }

    private static int pingColor(int ping) {
        if (ping < 0)   return 0xAAAAAA;
        if (ping < 150) return 0x55FF55;
        if (ping < 300) return 0xFFFF55;
        if (ping < 600) return 0xFF5555;
        return 0xAA0000;
    }
}
