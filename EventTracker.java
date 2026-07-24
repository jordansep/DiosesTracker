package com.diosesmon.tracker;

import com.google.gson.JsonObject;
import com.diosesmon.tracker.config.EventTargetsConfig;
import com.diosesmon.tracker.util.JsonStore;

import java.util.List;

/**
 * Cuenta tu progreso en los eventos de Crianza y Caza de Diosesmon,
 * comparando cada eclosion/captura contra la lista de especies objetivo
 * que vos configuraste en event_targets.json.
 *
 * Guarda el progreso en event_progress.json, algo como:
 * {
 *   "caza": {"pikachu": 3, "snorlax": 1},
 *   "crianza": {"charmander": 2, "eevee": 0}
 * }
 */
public final class EventTracker {

    private static final String PROGRESS_FILE = "event_progress.json";

    private EventTracker() {}

    public static void register() {
        EventTargetsConfig.ensureDefaultFileExists();
        System.out.println("[DiosesmonTracker] EventTracker listo. Revisa "
                + JsonStore.baseDir().resolve("event_targets.json")
                + " para configurar las especies del evento actual.");
    }

    public static void registerCapture(String species) {
        List<String> targets = EventTargetsConfig.getCazaTargets();
        if (targets.contains(species.toLowerCase())) {
            incrementProgress("caza", species.toLowerCase());
        }
    }

    public static void registerHatch(String species) {
        List<String> targets = EventTargetsConfig.getCrianzaTargets();
        if (targets.contains(species.toLowerCase())) {
            incrementProgress("crianza", species.toLowerCase());
        }
    }

    private static synchronized void incrementProgress(String eventKey, String species) {
        JsonObject root = JsonStore.readObject(PROGRESS_FILE);
        JsonObject eventObj = root.has(eventKey) && root.get(eventKey).isJsonObject()
                ? root.getAsJsonObject(eventKey)
                : new JsonObject();

        int current = eventObj.has(species) ? eventObj.get(species).getAsInt() : 0;
        eventObj.addProperty(species, current + 1);
        root.add(eventKey, eventObj);

        JsonStore.writeObject(PROGRESS_FILE, root);

        System.out.println("[DiosesmonTracker] Progreso de " + eventKey + " para "
                + species + ": " + (current + 1));
    }
}
