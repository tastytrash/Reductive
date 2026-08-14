package com.reductive;

import com.reductive.blocks.ExperienceTankBlock;

import com.reductive.blocks.MaterialRecyclerBlock;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.references.BlockItemId;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Function;

public class ReductiveBlockRegistry {
    public static final Block EXPERIENCE_TANK = register(
            ReductiveBlockItemIds.EXPERIENCE_TANK,
            ExperienceTankBlock::new,
            BlockBehaviour.Properties.of()
                    .requiresCorrectToolForDrops()
                    .strength(2.0F)
                    .sound(SoundType.IRON)
                    .noOcclusion()
                    .isSuffocating(Blocks::never)
                    .lightLevel(ExperienceTankBlock::getLuminance)
    );

    public static final Block MATERIAL_RECYCLER = register(
            ReductiveBlockItemIds.MATERIAL_RECYCLER,
            MaterialRecyclerBlock::new,
            BlockBehaviour.Properties.of()
                    .lightLevel(state -> state.getValue(MaterialRecyclerBlock.LIT) ? 13 : 0)
    );

    private static Block register(BlockItemId id, Function<BlockBehaviour.Properties, Block> blockFactory, BlockBehaviour.Properties properties) {
        Block block = register(id.block(), blockFactory, properties);

        BlockItem blockItem = new BlockItem(block, new Item.Properties().useBlockDescriptionPrefix().setId(id.item()));
        Registry.register(BuiltInRegistries.ITEM, id.item(), blockItem);

        return block;
    }

    public static Block register(ResourceKey<Block> id, Function<BlockBehaviour.Properties, Block> blockFactory, BlockBehaviour.Properties properties) {
        Block block = blockFactory.apply(properties.setId(id));

        return Registry.register(BuiltInRegistries.BLOCK, id, block);
    }

    public static void initialize() {}
}
