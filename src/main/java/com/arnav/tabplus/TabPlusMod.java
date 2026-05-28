package com.arnav.tabplus;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;

import java.util.ArrayList;

@Mod("tabplus")
public class TabPlusMod {

    public TabPlusMod(IEventBus modEventBus) {
        modEventBus.addListener(this::onRegisterKeyMappings);
        NeoForge.EVENT_BUS.addListener(this::onClientTick);
        NeoForge.EVENT_BUS.addListener(this::onPlayerLogin);
        NeoForge.EVENT_BUS.addListener(this::onPlayerLogout);
    }

    private void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        TabPlusKeys.onRegisterKeyMappings(event);
    }

    private void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen != null) return;
        if (!mc.options.keyPlayerList.isDown()) return;
        if (mc.getConnection() == null) return;

        var players = new ArrayList<>(mc.getConnection().getListedOnlinePlayers());
        if (players.isEmpty()) return;

        int idx = Math.min(TabFilter.getInstance().getSelectedIndex(), players.size() - 1);
        PlayerInfo target = players.get(idx);

        if (TabFilter.getInstance().consumeOpenProfile()) {
            mc.setScreen(new com.arnav.tabplus.screen.ProfileScreen(
                target.getProfile().id(),
                target.getProfile().name()
            ));
            return;
        }

        while (TabPlusKeys.NOTE.consumeClick()) {
            mc.setScreen(new com.arnav.tabplus.screen.ProfileScreen(
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

    private void onPlayerLogin(ClientPlayerNetworkEvent.LoggingIn event) {
        Minecraft mc = Minecraft.getInstance();
        String server = resolveServerAddress(mc);
        PlayerDataStore.getInstance().load(server);
        TabFilter.getInstance().clear();
    }

    private void onPlayerLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        TabFilter.getInstance().clear();
    }

    private static String resolveServerAddress(Minecraft mc) {
        if (mc.isLocalServer()) return "singleplayer";
        var server = mc.getCurrentServer();
        if (server != null) return server.ip;
        return "unknown";
    }
}
