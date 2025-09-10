    package com.reductive;

    import net.fabricmc.api.ModInitializer;

    import net.minecraft.util.Identifier;
    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;

    public class Reductive implements ModInitializer {
        public static final String MOD_ID = "reductive";

        public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

        @Override
        public void onInitialize() {
            LOGGER.info("Initializing reductive");
            ModItemRegistry.initialize();
            ModEntityRegistry.initialize();
        }

        public static Identifier id(String path) {
            return Identifier.of(MOD_ID, path);
        }

    }