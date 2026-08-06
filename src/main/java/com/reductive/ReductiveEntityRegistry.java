package com.reductive;

import com.reductive.entities.DynamiteEntity;
import com.reductive.entities.PebbleEntity;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class ReductiveEntityRegistry {
    public static final EntityType<PebbleEntity> PEBBLE
            = register("pebble", EntityType.Builder.of(PebbleEntity::new, MobCategory.MISC).sized(0.25f, 0.25f));
    public static final EntityType<DynamiteEntity> DYNAMITE
            = register("dynamite", EntityType.Builder.of(DynamiteEntity::new, MobCategory.MISC).sized(0.25f, 0.25f));


    public static void initialize() {

    }

    public static void register() {
    }

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
