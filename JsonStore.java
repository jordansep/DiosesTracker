package com.diosesmon.tracker.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

/**
 * Utilidad chica para leer/escribir archivos JSON dentro de
 * .minecraft/config/diosesmon-tracker/
 *
 * Cada tracker (pokemon, eventos, gts) usa su propio archivo, así no se pisan
 * entre si y es mas facil de inspeccionar/debuggear a mano.
 */
public final class JsonStore {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path BASE_DIR = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("diosesmon-tracker");

    private JsonStore() {}

    public static Path baseDir() {
        return BASE_DIR;
    }

    public static void ensureReady() {
        try {
            Files.createDirectories(BASE_DIR);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo crear la carpeta de datos de Diosesmon Tracker", e);
        }
    }

    /** Lee un archivo como JsonArray, o devuelve un array vacio si no existe todavia. */
    public static synchronized JsonArray readArray(String fileName) {
        Path path = BASE_DIR.resolve(fileName);
        if (!Files.exists(path)) {
            return new JsonArray();
        }
        try {
            String content = Files.readString(path);
            JsonElement el = JsonParser.parseString(content);
            return el.isJsonArray() ? el.getAsJsonArray() : new JsonArray();
        } catch (IOException e) {
            System.err.println("[DiosesmonTracker] No se pudo leer " + fileName + ": " + e.getMessage());
            return new JsonArray();
        }
    }

    /** Lee un archivo como JsonObject, o un objeto vacio si no existe todavia. */
    public static synchronized JsonObject readObject(String fileName) {
        Path path = BASE_DIR.resolve(fileName);
        if (!Files.exists(path)) {
            return new JsonObject();
        }
        try {
            String content = Files.readString(path);
            JsonElement el = JsonParser.parseString(content);
            return el.isJsonObject() ? el.getAsJsonObject() : new JsonObject();
        } catch (IOException e) {
            System.err.println("[DiosesmonTracker] No se pudo leer " + fileName + ": " + e.getMessage());
            return new JsonObject();
        }
    }

    public static synchronized void writeArray(String fileName, JsonArray array) {
        write(fileName, array);
    }

    public static synchronized void writeObject(String fileName, JsonObject obj) {
        write(fileName, obj);
    }

    private static void write(String fileName, JsonElement element) {
        ensureReady();
        Path path = BASE_DIR.resolve(fileName);
        try {
            Files.writeString(path, GSON.toJson(element));
        } catch (IOException e) {
            System.err.println("[DiosesmonTracker] No se pudo escribir " + fileName + ": " + e.getMessage());
        }
    }

    /** Agrega una entrada con timestamp a un archivo que funciona como log/lista. */
    public static synchronized void appendEntry(String fileName, JsonObject entry) {
        JsonArray arr = readArray(fileName);
        entry.addProperty("timestamp", Instant.now().toString());
        arr.add(entry);
        writeArray(fileName, arr);
    }
}
