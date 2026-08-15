package com.reductive.registries;

import net.fabricmc.fabric.api.registry.FuelValueEvents;

public class ReductiveFuelRegistry {
    public static void initialize() {
        FuelValueEvents.BUILD.register((builder, context) -> {
            builder.add(ReductiveItemRegistry.COAL_CHUNK, 177);
        });
    }
}
