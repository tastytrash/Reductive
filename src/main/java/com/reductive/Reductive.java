package com.reductive;

import com.reductive.datagen.ReductiveComponents;

import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Reductive implements ModInitializer {
    public static final String MOD_ID = "reductive";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing reductive");
        ReductiveItemRegistry.initialize();
        ReductiveEntityRegistry.initialize();
        ReductiveItemGroups.initialize();
        ReductiveComponents.initialize();
        ReductiveBlockRegistry.initialize();
        ReductiveBlockEntityRegistry.initialize();
        ReductiveLootTableRegistry.initialize();
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}