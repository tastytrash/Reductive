package com.reductive.items;

import com.reductive.datagen.ReductiveComponents;
import net.minecraft.block.BlockState;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.EnchantableComponent;
import net.minecraft.component.type.RepairableComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class DrillItem extends Item {
    private final Item bodyType;

    public DrillItem(Settings settings, Item bodyType) {
        super(settings);
        this.bodyType = bodyType;
    }


    @Override
    public boolean postMine(ItemStack stack, World world, BlockState state, BlockPos pos, LivingEntity miner) {
        if (stack.willBreakNextUse()) {
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

        return super.postMine(stack, world, state, pos, miner);
    }


    @Override
    public float getMiningSpeed(ItemStack stack, BlockState state) {
        String tip = stack.get(ReductiveComponents.TIP_TYPE);
        if (tip == null) return 1.0f;

        boolean isPickaxeBlock = state.isIn(BlockTags.PICKAXE_MINEABLE);
        boolean isShovelBlock = state.isIn(BlockTags.SHOVEL_MINEABLE);

        if (isPickaxeBlock || isShovelBlock) {
            return switch (tip) {
                case "iron" -> 14.0f;
                case "gold" -> 20.0f;
                case "diamond" -> 16.0f;
                case "netherite" -> 18.0f;
                default -> 1.0f;
            };
        }

        return 1.0f;
    }

    @Override
    public void postProcessComponents(ItemStack stack) {
        String tip = stack.get(ReductiveComponents.TIP_TYPE);

        if (tip == null) return;

        switch (tip) {
            case "iron" -> {
                stack.set(DataComponentTypes.ENCHANTABLE, new EnchantableComponent(14));
                stack.set(DataComponentTypes.REPAIRABLE,
                        new RepairableComponent(RegistryEntryList.of(Items.IRON_INGOT.getRegistryEntry())));
            }
            case "gold" -> {
                stack.set(DataComponentTypes.ENCHANTABLE, new EnchantableComponent(22));
                stack.set(DataComponentTypes.REPAIRABLE,
                        new RepairableComponent(RegistryEntryList.of(Items.GOLD_INGOT.getRegistryEntry())));
            }
            case "diamond" -> {
                stack.set(DataComponentTypes.ENCHANTABLE, new EnchantableComponent(10));
                stack.set(DataComponentTypes.REPAIRABLE,
                        new RepairableComponent(RegistryEntryList.of(Items.DIAMOND.getRegistryEntry())));
            }
            case "netherite" -> {
                stack.set(DataComponentTypes.ENCHANTABLE, new EnchantableComponent(15));
                stack.set(DataComponentTypes.REPAIRABLE,
                        new RepairableComponent(RegistryEntryList.of(Items.NETHERITE_INGOT.getRegistryEntry())));
            }
        }
    }

    @Override
    public void onCraft(ItemStack stack, World world) {
        applyTipComponents(stack);
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerWorld world, Entity entity, @Nullable EquipmentSlot slot) {
        applyTipComponents(stack);
    }

    private void applyTipComponents(ItemStack stack) {
        String tip = stack.get(ReductiveComponents.TIP_TYPE);
        if (tip == null) return;

        int durability = switch (tip) {
            case "iron" -> 512;
            case "gold" -> 192;
            case "diamond" -> 2304;
            case "netherite"-> 3456;
            default -> 0;
        };

        stack.set(DataComponentTypes.MAX_DAMAGE, durability);
    }
}
