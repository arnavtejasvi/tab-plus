package com.arnav.tabplus;

import com.arnav.tabplus.screen.ProfileScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(EnvType.CLIENT)
public final class TabPlusClient implements ClientModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger("tabplus");

    @Override
    public void onInitializeClient() {
        // force keybinding registration
        TabPlusKeys.NOTE.getClass();

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            String server = resolveServerAddress(client);
            PlayerDataStore.getInstance().load(server);
            TabFilter.getInstance().clear();
            LOGGER.info("Tab+ loaded data for server: {}", server);
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            TabFilter.getInstance().clear();
        });

        ClientTickEvents.END_CLIENT_TICK.register(this::onTick);

        LOGGER.info("Tab+ initialized.");
    }

    private void onTick(MinecraftClient client) {
        if (client.currentScreen != null) return;
        if (!client.options.playerListKey.isPressed()) return;
        if (client.getNetworkHandler() == null) return;

        var players = new java.util.ArrayList<>(client.getNetworkHandler().getListedPlayerListEntries());
        if (players.isEmpty()) return;

        int idx = Math.min(TabFilter.getInstance().getSelectedIndex(), players.size() - 1);
        PlayerListEntry target = players.get(idx);

        if (TabFilter.getInstance().consumeOpenProfile()) {
            client.setScreen(new ProfileScreen(
                target.getProfile().getId(),
                target.getProfile().getName()
            ));
            return;
        }

        while (TabPlusKeys.NOTE.wasPressed()) {
            client.setScreen(new ProfileScreen(
                target.getProfile().getId(),
                target.getProfile().getName()
            ));
        }

        while (TabPlusKeys.TAG.wasPressed()) {
            var uuid = target.getProfile().getId();
            var store = PlayerDataStore.getInstance();
            var data = store.get(uuid);
            data.tag = data.tag.next();
            store.set(uuid, data);
        }
    }

    private static String resolveServerAddress(MinecraftClient client) {
        if (client.isInSingleplayer()) return "singleplayer";
        var server = client.getCurrentServerEntry();
        if (server != null) return server.address;
        return "unknown";
    }
}
