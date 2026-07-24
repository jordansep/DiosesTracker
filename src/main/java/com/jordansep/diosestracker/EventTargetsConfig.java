package com.jordansep.diosestracker;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.jordansep.diosestracker.JsonStore;

import java.util.ArrayList;
import java.util.List;

/**
 * Los eventos de Crianza y Caza de Diosesmon cambian de especies objetivo
 * cada cierto tiempo. El mod NO puede adivinar cuales son (eso depende del
 * servidor), asi que las leemos de un archivo de configuracion editable:
 *
 *   .minecraft/config/diosesmon-tracker/event_targets.json
 *
 * Formato esperado:
 * {
 *   "crianza": ["charmander", "eevee"],
 *   "caza": ["pikachu", "snorlax", "gible"]
 * }
 *
 * Actualiza este archivo vos mismo cada vez que el servidor anuncie un
 * evento nuevo (o le agregamos despues un comando en el chat que lo
 * actualice solo si el server lo anuncia con un formato fijo).
 */
public final class EventTargetsConfig {

    private static final String FILE_NAME = "event_targets.json";

    private EventTargetsConfig() {}

    public static List<String> getCrianzaTargets() {
        return readList("crianza");
    }

    public static List<String> getCazaTargets() {
        return readList("caza");
    }

    private static List<String> readList(String key) {
        JsonObject obj = JsonStore.readObject(FILE_NAME);
        List<String> result = new ArrayList<>();
        if (obj.has(key) && obj.get(key).isJsonArray()) {
            JsonArray arr = obj.getAsJsonArray(key);
            for (JsonElement el : arr) {
                result.add(el.getAsString().toLowerCase());
            }
        }
        return result;
    }

    /** Crea el archivo con un ejemplo si todavia no existe, para que sea facil de editar. */
    public static void ensureDefaultFileExists() {
        JsonStore.ensureReady();
        java.nio.file.Path path = JsonStore.baseDir().resolve(FILE_NAME);
        if (java.nio.file.Files.exists(path)) return;

        JsonObject defaults = new JsonObject();
        JsonArray crianza = new JsonArray();
        crianza.add("charmander");
        crianza.add("eevee");
        JsonArray caza = new JsonArray();
        caza.add("pikachu");
        caza.add("snorlax");
        defaults.add("crianza", crianza);
        defaults.add("caza", caza);

        JsonStore.writeObject(FILE_NAME, defaults);
        System.out.println("[DiosesmonTracker] Se creo event_targets.json con datos de ejemplo. "
                + "Editalo con las especies reales del evento actual.");
    }
}
