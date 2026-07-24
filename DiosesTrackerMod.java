package com.diosesmon.tracker;

import net.fabricmc.api.ClientModInitializer;

/**
 * Punto de entrada del mod. Fabric llama a onInitializeClient() al cargar
 * el juego (porque este mod es solo de cliente, ver "environment": "client"
 * en fabric.mod.json).
 */
public class DiosesTrackerMod implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        System.out.println("[DiosesmonTracker] Inicializando Diosesmon Tracker...");

        PersonalPokemonTracker.register();
        EventTracker.register();
        GtsPriceTracker.register();

        System.out.println("[DiosesmonTracker] Listo. Datos guardados en: "
                + com.diosesmon.tracker.util.JsonStore.baseDir());
    }
}
