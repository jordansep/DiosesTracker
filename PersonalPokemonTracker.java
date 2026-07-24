package com.diosesmon.tracker;

import com.google.gson.JsonObject;
import com.diosesmon.tracker.util.JsonStore;

/**
 * Trackea capturas, evoluciones y eclosiones de huevos usando el bus de
 * eventos de Cobblemon.
 *
 * IMPORTANTE: los nombres exactos de las clases/eventos de abajo
 * (CobblemonEvents.POKEMON_CAPTURED, etc.) corresponden a la API publica
 * de Cobblemon, pero esa API cambia entre versiones del mod. Antes de
 * compilar esto:
 *
 *   1. Abri el jar de Cobblemon de tu version en tu IDE (o el codigo fuente
 *      en https://gitlab.com/cable-mc/cobblemon) y buscá la clase
 *      "CobblemonEvents" para confirmar los nombres reales disponibles en
 *      tu version.
 *   2. Ajustá los imports y nombres de eventos si difieren.
 *
 * La lógica de "que hacer con el evento" (guardarlo, contarlo, etc.) no
 * deberia cambiar mucho aunque los nombres exactos si.
 */
public final class PersonalPokemonTracker {

    private static final String LOG_FILE = "pokemon_log.json";

    private PersonalPokemonTracker() {}

    public static void register() {
        JsonStore.ensureReady();

        // --- Pseudocodigo de referencia para conectar con Cobblemon ---
        //
        // CobblemonEvents.POKEMON_CAPTURED.subscribe(event -> {
        //     Pokemon pokemon = event.getPokemon();
        //     onPokemonCaptured(
        //         pokemon.getSpecies().getName(),
        //         pokemon.getShiny(),
        //         pokemon.getLevel()
        //     );
        //     return Unit.INSTANCE;
        // });
        //
        // CobblemonEvents.EVOLUTION.subscribe(event -> {
        //     onEvolution(event.getPokemon().getSpecies().getName());
        //     return Unit.INSTANCE;
        // });
        //
        // CobblemonEvents.HATCH_EGG.subscribe(event -> {  // nombre exacto puede variar
        //     onEggHatched(event.getPokemon().getSpecies().getName());
        //     return Unit.INSTANCE;
        // });
        //
        // ----------------------------------------------------------------

        System.out.println("[DiosesmonTracker] PersonalPokemonTracker listo. "
                + "Conectá las suscripciones de CobblemonEvents de arriba una vez"
                + " que confirmes los nombres exactos para tu version.");
    }

    public static void onPokemonCaptured(String species, boolean shiny, int level) {
        JsonObject entry = new JsonObject();
        entry.addProperty("tipo", "captura");
        entry.addProperty("especie", species);
        entry.addProperty("shiny", shiny);
        entry.addProperty("nivel", level);
        JsonStore.appendEntry(LOG_FILE, entry);

        // También lo pasamos al tracker de eventos, por si esta especie
        // cuenta para el evento de Caza activo.
        EventTracker.registerCapture(species);
    }

    public static void onEvolution(String newSpecies) {
        JsonObject entry = new JsonObject();
        entry.addProperty("tipo", "evolucion");
        entry.addProperty("especie", newSpecies);
        JsonStore.appendEntry(LOG_FILE, entry);
    }

    public static void onEggHatched(String species) {
        JsonObject entry = new JsonObject();
        entry.addProperty("tipo", "eclosion");
        entry.addProperty("especie", species);
        JsonStore.appendEntry(LOG_FILE, entry);

        // Para el evento de Crianza.
        EventTracker.registerHatch(species);
    }
}
