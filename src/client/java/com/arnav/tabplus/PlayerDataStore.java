package com.arnav.tabplus;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerDataStore {
    private static final PlayerDataStore INSTANCE = new PlayerDataStore();
    private static final Gson GSON = new Gson();

    private final Map<UUID, PlayerData> data = new HashMap<>();
    private String currentServer = null;

    private PlayerDataStore() {}

    public static PlayerDataStore getInstance() {
        return INSTANCE;
    }

    public void load(String serverAddress) {
        currentServer = sanitize(serverAddress);
        data.clear();
        Path file = dataFile();
        if (!Files.exists(file)) return;
        try {
            String json = Files.readString(file);
            JsonObject obj = GSON.fromJson(json, JsonObject.class);
            if (obj == null) return;
            for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
                try {
                    UUID uuid = UUID.fromString(entry.getKey());
                    JsonObject d = entry.getValue().getAsJsonObject();
                    PlayerData pd = new PlayerData();
                    if (d.has("note")) pd.note = d.get("note").getAsString();
                    if (d.has("tag")) {
                        try { pd.tag = RelationshipTag.valueOf(d.get("tag").getAsString()); }
                        catch (IllegalArgumentException ignored) {}
                    }
                    if (!pd.isEmpty()) data.put(uuid, pd);
                } catch (IllegalArgumentException ignored) {}
            }
        } catch (IOException ignored) {}
    }

    public void save() {
        if (currentServer == null) return;
        JsonObject obj = new JsonObject();
        for (Map.Entry<UUID, PlayerData> entry : data.entrySet()) {
            JsonObject d = new JsonObject();
            d.addProperty("note", entry.getValue().note);
            d.addProperty("tag", entry.getValue().tag.name());
            obj.add(entry.getKey().toString(), d);
        }
        try {
            Path file = dataFile();
            Files.createDirectories(file.getParent());
            Files.writeString(file, GSON.toJson(obj));
        } catch (IOException ignored) {}
    }

    public PlayerData get(UUID uuid) {
        return data.getOrDefault(uuid, new PlayerData());
    }

    public void set(UUID uuid, PlayerData pd) {
        if (pd.isEmpty()) {
            data.remove(uuid);
        } else {
            data.put(uuid, pd);
        }
        save();
    }

    public void clear() {
        data.clear();
        currentServer = null;
    }

    private Path dataFile() {
        return FabricLoader.getInstance().getConfigDir()
            .resolve("tabplus")
            .resolve(currentServer + ".json");
    }

    private static String sanitize(String address) {
        return address.replaceAll("[^a-zA-Z0-9._\\-]", "_");
    }
}
