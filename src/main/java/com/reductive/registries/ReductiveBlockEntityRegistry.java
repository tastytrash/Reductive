package com.reductive.registries;

import com.reductive.Reductive;
import com.reductive.blockentities.ExperienceTankBlockEntity;

import com.reductive.blockentities.MaterialRecyclerBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ReductiveBlockEntityRegistry {
    public static final BlockEntityType<ExperienceTankBlockEntity> EXPERIENCE_TANK_BLOCK_ENTITY =
            register("experience_tank", ExperienceTankBlockEntity::new, ReductiveBlockRegistry.EXPERIENCE_TANK);

    public static final BlockEntityType<MaterialRecyclerBlockEntity> MATERIAL_RECYCLER_BLOCK_ENTITY =
            register("material_recycler", MaterialRecyclerBlockEntity::new, ReductiveBlockRegistry.MATERIAL_RECYCLER);

    private static <T extends BlockEntity> BlockEntityType<T> register(String name, FabricBlockEntityTypeBuilder.Factory<? extends T> entityFactory, Block... blocks) {
        Identifier id = Identifier.fromNamespaceAndPath(Reductive.MOD_ID, name);
        return Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id, FabricBlockEntityTypeBuilder.<T>create(entityFactory, blocks).build());
    }

    public static void initialize() {}
}

