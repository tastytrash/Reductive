package com.reductive.datagen;

import com.mojang.serialization.Codec;
import com.reductive.Reductive;
import net.minecraft.component.ComponentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ReductiveComponents {
    public static final ComponentType<String> TIP_TYPE = Registry.register(
            Registries.DATA_COMPONENT_TYPE,
            Identifier.of(Reductive.MOD_ID, "tip"),
            ComponentType.<String>builder().codec(Codec.STRING).build()
    );

    public static void initialize() {
        System.out.println("Registered ModComponents");
    }
}
