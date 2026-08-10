package com.reductive.items;

import com.reductive.datagen.ReductiveComponents;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
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

public class DrillItem extends Item {
    private final Item bodyType;

    public DrillItem(Properties settings, Item bodyType) {
        super(settings);
        this.bodyType = bodyType;
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level world, BlockState state, BlockPos pos, LivingEntity miner) {
        if (stack.nextDamageWillBreak()) {
            ItemStack bodyOnly = new ItemStack(bodyType);
            stack.shrink(1);
            miner.handleExtraItemsCreatedOnUse(bodyOnly);
            world.playSound(null, miner.getX(), miner.getY(), miner.getZ(), SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 1.0f, 1.0f);}

        return super.mineBlock(stack, world, state, pos, miner);
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        String tip = stack.get(ReductiveComponents.TIP_TYPE);
        if (tip == null) return 1.0f;

        boolean isPickaxeBlock = state.is(BlockTags.MINEABLE_WITH_PICKAXE);
        boolean isShovelBlock = state.is(BlockTags.MINEABLE_WITH_SHOVEL);

        if (isPickaxeBlock || isShovelBlock) {
            return switch (tip) {
                case "iron" -> 14.0f;
                case "gold" -> 23.0f;
                case "diamond" -> 18.0f;
                case "netherite" -> 21.0f;
                default -> 1.0f;
            };
        }

        return 1.0f;
    }

    public static void applyTipProperties(ItemStack stack) {
        String blade = stack.get(ReductiveComponents.TIP_TYPE);
        if (blade == null) return;

        DrillItem.TipProperties properties = switch (blade) {
            case "iron" -> new DrillItem.TipProperties(512, 14, Items.IRON_INGOT, 2.0F);
            case "gold" -> new DrillItem.TipProperties(192, 22, Items.GOLD_INGOT, 2.0F);
            case "diamond" -> new DrillItem.TipProperties(2304, 10, Items.DIAMOND, 3.0F);
            case "netherite" -> new DrillItem.TipProperties(3456, 15, Items.NETHERITE_INGOT, 4.0F);
            default -> null;
        };
        if (properties == null) return;

        Integer currentMax = stack.get(DataComponents.MAX_DAMAGE);
        if (currentMax == null || currentMax != properties.durability()) {
            stack.set(DataComponents.MAX_DAMAGE, properties.durability());
        }

        Enchantable enchantable = new Enchantable(properties.enchantability());
        if (!enchantable.equals(stack.get(DataComponents.ENCHANTABLE))) {
            stack.set(DataComponents.ENCHANTABLE, enchantable);
        }

        Repairable repairable = new Repairable(HolderSet.direct(BuiltInRegistries.ITEM.wrapAsHolder(properties.repairItem())));
        if (!repairable.equals(stack.get(DataComponents.REPAIRABLE))) {
            stack.set(DataComponents.REPAIRABLE, repairable);
        }
    }

    private record TipProperties(int durability, int enchantability, Item repairItem, float attackDamage) {}

    @Override
    public void onCraftedPostProcess(ItemStack stack, Level world) {
        applyTipProperties(stack);
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel world, Entity entity, @Nullable EquipmentSlot slot) {
        applyTipProperties(stack);
    }
}
