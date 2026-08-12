package com.reductive;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ReductiveLootTableRegistry {
    private static final Set<ResourceKey<LootTable>> LOCATIONS = new HashSet<>();
    private static final Set<ResourceKey<LootTable>> IMMUTABLE_LOCATIONS;

    public static final ResourceKey<LootTable> BAIT_TREASURE;

    private static ResourceKey<LootTable> register(final String location) {
        return register(ResourceKey.create(Registries.LOOT_TABLE, Identifier.fromNamespaceAndPath("reductive", location)));
    }

    private static ResourceKey<LootTable> register(final ResourceKey<LootTable> location) {
        if (LOCATIONS.add(location)) {
            return location;
        } else {
            throw new IllegalArgumentException(String.valueOf(location.identifier()) + " is already a registered built-in loot table");
        }
    }

    public static Set<ResourceKey<LootTable>> all() {
        return IMMUTABLE_LOCATIONS;
    }

    static {
        IMMUTABLE_LOCATIONS = Collections.unmodifiableSet(LOCATIONS);
        BAIT_TREASURE = register("gameplay/fishing/bait_treasure");
    }

    public static void initialize() {}
}
