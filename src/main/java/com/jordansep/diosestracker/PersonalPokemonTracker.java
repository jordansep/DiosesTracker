package com.jordansep.diosestracker;

import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.events.pokemon.PokemonCapturedEvent;
import com.cobblemon.mod.common.api.events.pokemon.evolution.EvolutionCompleteEvent;
import com.cobblemon.mod.common.api.events.pokemon.HatchEggEvent;
import com.google.gson.JsonObject;
import com.jordansep.diosestracker.JsonStore;

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

        // Escuchar capturas de Pokémon
        CobblemonEvents.POKEMON_CAPTURED.subscribe((PokemonCapturedEvent event) -> {
            try {
                String species = event.getPokemon().getSpecies().getName();
                boolean shiny = event.getPokemon().getShiny();
                int level = event.getPokemon().getLevel();
                onPokemonCaptured(species, shiny, level);
            } catch (Exception e) {
                System.err.println("[DiosesmonTracker] Error capturando evento de captura: " + e.getMessage());
            }
        });

        // Escuchar evoluciones
        CobblemonEvents.EVOLUTION_COMPLETE.subscribe((EvolutionCompleteEvent event) -> {
            try {
                String species = event.getPokemon().getSpecies().getName();
                onEvolution(species);
            } catch (Exception e) {
                System.err.println("[DiosesmonTracker] Error capturando evento de evolución: " + e.getMessage());
            }
        });

        // Escuchar eclosiones de huevos
        CobblemonEvents.HATCH_EGG_POST.subscribe((HatchEggEvent.Post event) -> {
            try {
                String species = event.getPlayer().getName().getString();
                onEggHatched(species);
            } catch (Exception e) {
                System.err.println("[DiosesmonTracker] Error capturando evento de eclosión: " + e.getMessage());
            }
        });

        System.out.println("[DiosesmonTracker] PersonalPokemonTracker listo. Escuchando eventos de Cobblemon...");
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
