package com.reductive.datagen;

import com.mojang.serialization.Codec;
import com.reductive.Reductive;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

public class ReductiveComponents {
    public static final DataComponentType<String> TIP_TYPE = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            Identifier.fromNamespaceAndPath(Reductive.MOD_ID, "tip"),
            DataComponentType.<String>builder().persistent(Codec.STRING).build()
    );

    public static void initialize() {
        System.out.println("Registered ModComponents");
    }
}
