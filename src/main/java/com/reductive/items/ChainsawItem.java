package com.reductive.items;

import com.reductive.datagen.ReductiveComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantable;
import net.minecraft.world.item.enchantment.Repairable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class ChainsawItem extends Item {
    private final Item bodyType;

    public ChainsawItem(Properties settings, Item bodyType) {
        super(settings);
        this.bodyType = bodyType;
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level world, BlockState state, BlockPos pos, LivingEntity miner) {
        if (stack.nextDamageWillBreak()) {
                ItemStack bodyOnly = new ItemStack(bodyType);
                stack.shrink(1);
                miner.handleExtraItemsCreatedOnUse(bodyOnly);
                world.playSound(
                        null,
                        miner.getX(),
                        miner.getY(),
                        miner.getZ(),
                        SoundEvents.ITEM_BREAK,
                        SoundSource.PLAYERS,
                        1.0f,
                        1.0f
                );
        }

        return super.mineBlock(stack, world, state, pos, miner);
    }


    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        String blade = stack.get(ReductiveComponents.BLADE_TYPE);
        if (blade == null) return 1.0f;

        boolean isAxeBlock = state.is(BlockTags.MINEABLE_WITH_AXE);

        if (isAxeBlock) {
            return switch (blade) {
                case "iron" -> 14.0f;
                case "gold" -> 23.0f;
                case "diamond" -> 18.0f;
                case "netherite" -> 21.0f;
                default -> 1.0f;
            };
        }

        return 1.0f;
    }

    private void applyDynamicComponents(ItemStack stack) {
        String blade = stack.get(ReductiveComponents.BLADE_TYPE);
        if (blade == null) return;

        // 1. Dynamic Durability Management
        int durability = switch (blade) {
            case "iron" -> 512;
            case "gold" -> 192;
            case "diamond" -> 2304;
            case "netherite" -> 3456;
            default -> 0;
        };

        Integer currentMax = stack.get(DataComponents.MAX_DAMAGE);
        if (currentMax == null || currentMax != durability) {
            stack.set(DataComponents.MAX_DAMAGE, durability);
        }

        // 2. Dynamic Enchantable & Repairable Management (Moved from deleted postComponentsLoad method)
        switch (tip) {
            case "iron" -> {
                if (!stack.has(DataComponents.ENCHANTABLE)) stack.set(DataComponents.ENCHANTABLE, new Enchantable(14));
                if (!stack.has(DataComponents.REPAIRABLE)) stack.set(DataComponents.REPAIRABLE, new Repairable(HolderSet.direct(Items.IRON_INGOT.builtInRegistryHolder())));
            }
            case "gold" -> {
                if (!stack.has(DataComponents.ENCHANTABLE)) stack.set(DataComponents.ENCHANTABLE, new Enchantable(22));
                if (!stack.has(DataComponents.REPAIRABLE)) stack.set(DataComponents.REPAIRABLE, new Repairable(HolderSet.direct(Items.GOLD_INGOT.builtInRegistryHolder())));
            }
            case "diamond" -> {
                if (!stack.has(DataComponents.ENCHANTABLE)) stack.set(DataComponents.ENCHANTABLE, new Enchantable(10));
                if (!stack.has(DataComponents.REPAIRABLE)) stack.set(DataComponents.REPAIRABLE, new Repairable(HolderSet.direct(Items.DIAMOND.builtInRegistryHolder())));
            }
            case "netherite" -> {
                if (!stack.has(DataComponents.ENCHANTABLE)) stack.set(DataComponents.ENCHANTABLE, new Enchantable(15));
                if (!stack.has(DataComponents.REPAIRABLE)) stack.set(DataComponents.REPAIRABLE, new Repairable(HolderSet.direct(Items.NETHERITE_INGOT.builtInRegistryHolder())));
            }
        }
    }

    @Override
    public void onCraftedPostProcess(ItemStack stack, Level world) {
        applyDynamicComponents(stack);
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel world, Entity entity, @Nullable EquipmentSlot slot) {
        applyDynamicComponents(stack);
    }
}
