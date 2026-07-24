package com.jordansep.diosestracker;

import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
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

        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (!(screen instanceof HandledScreen<?> handledScreen)) return;
            if (!looksLikeEventScreen(handledScreen)) return;
            onCazasScreenOpened(handledScreen);
        });

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
    public static void onCazasScreenOpened(HandledScreen<?> screen) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        String eventKey = looksLikeCrianzaScreen(screen) ? "crianza" : "caza";
        List<String> targets = "crianza".equals(eventKey)
                ? EventTargetsConfig.getCrianzaTargets()
                : EventTargetsConfig.getCazaTargets();

        if (targets.isEmpty()) return;

        Set<String> matchedSpecies = new LinkedHashSet<>();
        for (Slot slot : screen.getScreenHandler().slots) {
            ItemStack stack = slot.getStack();
            if (stack.isEmpty()) continue;

            String itemName = stack.getName().getString();
            if (itemName == null || itemName.isBlank()) continue;
            if (itemName.contains("Panel") || itemName.contains("Cristal")) continue;

            for (String species : findMatchingSpecies(itemName, targets)) {
                matchedSpecies.add(species);
            }
        }

        for (String species : matchedSpecies) {
            incrementProgress(eventKey, species);
        }

        if (!matchedSpecies.isEmpty()) {
            System.out.println("[DiosesmonTracker] Se detectaron especies objetivo en la pantalla de "
                    + eventKey + ": " + matchedSpecies);
        }
    }

    public static List<String> findMatchingSpecies(String text, List<String> targets) {
        if (text == null || text.isBlank() || targets == null || targets.isEmpty()) {
            return List.of();
        }

        String normalizedText = text.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", " ").trim();
        List<String> matches = new ArrayList<>();
        for (String target : targets) {
            String normalizedTarget = target == null ? "" : target.toLowerCase(Locale.ROOT).trim();
            if (!normalizedTarget.isEmpty() && normalizedText.contains(normalizedTarget)) {
                matches.add(normalizedTarget);
            }
        }
        return matches;
    }

    private static boolean looksLikeEventScreen(HandledScreen<?> screen) {
        String title = screen.getTitle() != null ? screen.getTitle().getString() : "";
        String normalizedTitle = title.toLowerCase(Locale.ROOT);
        return normalizedTitle.contains("caza")
                || normalizedTitle.contains("crianza")
                || normalizedTitle.contains("breeding")
                || normalizedTitle.contains("hunt")
                || normalizedTitle.contains("hatch");
    }

    private static boolean looksLikeCrianzaScreen(HandledScreen<?> screen) {
        String title = screen.getTitle() != null ? screen.getTitle().getString() : "";
        String normalizedTitle = title.toLowerCase(Locale.ROOT);
        return normalizedTitle.contains("crianza")
                || normalizedTitle.contains("breeding")
                || normalizedTitle.contains("hatch");
    }
    private static synchronized void incrementProgress(String eventKey, String species) {
        JsonObject root = readProgressObject();
        JsonObject eventObj = root.has(eventKey) && root.get(eventKey).isJsonObject()
                ? root.getAsJsonObject(eventKey)
                : new JsonObject();

        int current = eventObj.has(species) ? eventObj.get(species).getAsInt() : 0;
        eventObj.addProperty(species, current + 1);
        root.add(eventKey, eventObj);

        writeProgressObject(root);

        System.out.println("[DiosesmonTracker] Progreso de " + eventKey + " para "
                + species + ": " + (current + 1));
    }

    private static JsonObject readProgressObject() {
        return JsonStore.readObject(PROGRESS_FILE);
    }

    private static void writeProgressObject(JsonObject root) {
        JsonStore.writeObject(PROGRESS_FILE, root);
    }
}
