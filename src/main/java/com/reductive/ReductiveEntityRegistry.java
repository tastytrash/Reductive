package com.reductive;

import com.reductive.entities.projectiles.DynamiteProjectile;
import com.reductive.entities.projectiles.PebbleProjectile;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class ReductiveEntityRegistry {
    public static final EntityType<PebbleProjectile> PEBBLE
            = register("pebble", EntityType.Builder.of(PebbleProjectile::new, MobCategory.MISC).sized(0.25f, 0.25f));

    public static final EntityType<DynamiteProjectile> DYNAMITE
            = register("dynamite", EntityType.Builder.of(DynamiteProjectile::new, MobCategory.MISC).sized(0.25f, 0.25f));

    public static void initialize() {}

    private static <T extends Entity> EntityType<T> register(ResourceKey<EntityType<?>> key, EntityType.Builder<T> type) {
        return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, type.build(key));
    }

    private static <T extends Entity> EntityType<T> register(String id, EntityType.Builder<T> type) {
        return register(keyOf(id), type);
    }

    private static ResourceKey<EntityType<?>> keyOf(String id) {
        return ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(Reductive.MOD_ID, id));
    }

}
