package com.diosesmon.tracker;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.diosesmon.tracker.util.JsonStore;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * IMPORTANTE — leer antes de compilar:
 *
 * No conozco la clase exacta de la pantalla de GTS de Diosesmon (puede ser
 * un menu de Cobblemon vanilla, o un plugin custom del servidor). Este
 * archivo asume:
 *
 *   1. Que la pantalla de GTS es un HandledScreen comun (chest/inventario),
 *      lo cual es lo mas probable en la mayoria de servidores.
 *   2. Que el titulo de la pantalla o los nombres de los items contienen
 *      la palabra "GTS" o el precio en el "lore" (texto debajo del nombre
 *      del item), tipo "Precio: 500 monedas" o similar.
 *
 * Vas a tener que:
 *   - Abrir el GTS en el juego, fijarte el texto EXACTO que muestra
 *     (nombre del item + lore).
 *   - Ajustar GTS_SCREEN_TITLE_HINT y el regex PRICE_PATTERN de abajo
 *     para que matcheen ese formato real.
 *
 * Este tracker NO navega el GTS solo. Solo lee lo que ya está en pantalla
 * mientras vos pasás de página manualmente — por eso conviene quedarte
 * unos segundos en cada página para que el tick de escaneo la registre.
 */
public final class GtsPriceTracker {

    private static final String LISTINGS_FILE = "gts_listings.json";

    // TODO: ajustar a un fragmento real del titulo de la pantalla de GTS
    private static final String GTS_SCREEN_TITLE_HINT = "gts";

    // TODO: ajustar al formato real de precio que muestra Diosesmon
    // Este ejemplo matchea cosas como "Precio: 1.500 monedas" o "$1500"
    private static final Pattern PRICE_PATTERN =
            Pattern.compile("(?:Precio:?\\s*)?\\$?([0-9][0-9.,]*)\\s*(?:monedas|coins)?", Pattern.CASE_INSENSITIVE);

    private static boolean gtsScreenOpen = false;
    private static final Set<String> seenThisSession = new HashSet<>();

    private GtsPriceTracker() {}

    public static void register() {
        JsonStore.ensureReady();

        ScreenEvents.AFTER_INIT.register((client, screen, w, h) -> {
            String title = screen.getTitle() != null ? screen.getTitle().getString().toLowerCase() : "";
            gtsScreenOpen = title.contains(GTS_SCREEN_TITLE_HINT);
            if (gtsScreenOpen) {
                seenThisSession.clear();
                System.out.println("[DiosesmonTracker] Pantalla de GTS detectada, empezando a leer listados visibles.");
            }
        });

        ScreenEvents.BEFORE_CLOSE.register((client, screen) -> {
            gtsScreenOpen = false;
        });

        // Escaneamos cada tanto mientras la pantalla este abierta (no todos los ticks,
        // para no generar entradas duplicadas todo el tiempo por nada).
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!gtsScreenOpen) return;
            if (client.currentScreen == null) return;
            if (client.age % 40 != 0) return; // aprox cada 2 segundos

            if (client.currentScreen instanceof HandledScreen<?> handled) {
                scanScreen(handled);
            }
        });

        System.out.println("[DiosesmonTracker] GtsPriceTracker listo. Revisá los TODO de este "
                + "archivo antes de usarlo — el formato de precio/titulo hay que ajustarlo al real.");
    }

    private static void scanScreen(HandledScreen<?> screen) {
        for (Slot slot : screen.getScreenHandler().slots) {
            ItemStack stack = slot.getStack();
            if (stack == null || stack.isEmpty()) continue;

            String name = stack.getName().getString();
            String lore = extractLore(stack);
            String combined = name + " " + lore;

            Matcher m = PRICE_PATTERN.matcher(combined);
            if (!m.find()) continue;

            String priceRaw = m.group(1).replace(".", "").replace(",", "");
            long price;
            try {
                price = Long.parseLong(priceRaw);
            } catch (NumberFormatException e) {
                continue;
            }

            // Evita registrar el mismo item visible varias veces mientras
            // esta en la misma pagina (el tick de escaneo pasa cada 2s).
            String dedupeKey = name + "|" + price;
            if (seenThisSession.contains(dedupeKey)) continue;
            seenThisSession.add(dedupeKey);

            JsonObject entry = new JsonObject();
            entry.addProperty("item", name);
            entry.addProperty("precio", price);
            JsonStore.appendEntry(LISTINGS_FILE, entry);
        }
    }

    private static String extractLore(ItemStack stack) {
        // El lore (texto debajo del nombre) suele estar en los componentes
        // custom del item. La forma exacta de leerlo depende de la version
        // de Minecraft/Cobblemon — en 1.20.1 suele estar en el NBT bajo
        // "display.Lore". Ajustar segun corresponda a tu version.
        try {
            var nbt = stack.getNbt();
            if (nbt == null || !nbt.contains("display")) return "";
            var display = nbt.getCompound("display");
            if (!display.contains("Lore")) return "";
            var loreList = display.getList("Lore", 8); // 8 = NbtString type id
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < loreList.size(); i++) {
                sb.append(loreList.getString(i)).append(" ");
            }
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Calcula el precio promedio guardado para un item, en base a todo el
     * historial acumulado en gts_listings.json hasta ahora.
     */
    public static double averagePriceFor(String itemName) {
        JsonArray listings = JsonStore.readArray(LISTINGS_FILE);
        long sum = 0;
        int count = 0;
        for (var el : listings) {
            JsonObject obj = el.getAsJsonObject();
            if (obj.get("item").getAsString().equalsIgnoreCase(itemName)) {
                sum += obj.get("precio").getAsLong();
                count++;
            }
        }
        return count == 0 ? 0 : (double) sum / count;
    }
}
