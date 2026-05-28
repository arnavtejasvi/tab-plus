package com.arnav.tabplus;

import com.arnav.tabplus.screen.ProfileScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(EnvType.CLIENT)
public final class TabPlusClient implements ClientModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger("tabplus");

    @Override
    public void onInitializeClient() {
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

    private void onTick(Minecraft client) {
        if (client.screen != null) return;
        if (!client.options.keyPlayerList.isDown()) return;
        if (client.getConnection() == null) return;

        var players = new java.util.ArrayList<>(client.getConnection().getListedOnlinePlayers());
        if (players.isEmpty()) return;

        int idx = Math.min(TabFilter.getInstance().getSelectedIndex(), players.size() - 1);
        PlayerInfo target = players.get(idx);

        if (TabFilter.getInstance().consumeOpenProfile()) {
            client.setScreen(new ProfileScreen(
                target.getProfile().id(),
                target.getProfile().name()
            ));
            return;
        }

        while (TabPlusKeys.NOTE.consumeClick()) {
            client.setScreen(new ProfileScreen(
                target.getProfile().id(),
                target.getProfile().name()
            ));
        }

        while (TabPlusKeys.TAG.consumeClick()) {
            var uuid = target.getProfile().id();
            var store = PlayerDataStore.getInstance();
            var data = store.get(uuid);
            data.tag = data.tag.next();
            store.set(uuid, data);
        }
    }

    private static String resolveServerAddress(Minecraft client) {
        if (client.isLocalServer()) return "singleplayer";
        var server = client.getCurrentServer();
        if (server != null) return server.ip;
        return "unknown";
    }
}
