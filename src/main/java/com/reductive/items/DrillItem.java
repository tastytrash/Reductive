package com.reductive.items;

import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class DrillItem extends Item {
    private final Item bodyType;

    public DrillItem(ToolMaterial material, Item.Settings settings, Item bodyType) {
        super(settings.maxDamage(material.durability()));
        this.bodyType = bodyType;
    }

    @Override
    public boolean postMine(ItemStack stack, World world, BlockState state, BlockPos pos, LivingEntity miner) {
        if (stack.willBreakNextUse()) {
            if (!world.isClient) {
                ItemStack bodyOnly = new ItemStack(bodyType);
                stack.decrement(1);
                miner.giveOrDropStack(bodyOnly);
                world.playSound(
                        null,
                        miner.getX(),
                        miner.getY(),
                        miner.getZ(),
                        SoundEvents.ENTITY_ITEM_BREAK,
                        SoundCategory.PLAYERS,
                        1.0f,
                        1.0f
                );
            }
        }

        return super.postMine(stack, world, state, pos, miner);
    }
}
