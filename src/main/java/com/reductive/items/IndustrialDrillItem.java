package com.reductive.items;

import com.reductive.Reductive;
import com.reductive.datagen.ReductiveComponents;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.Nullable;

public class IndustrialDrillItem extends Item {
    private final Item bodyType;

    public IndustrialDrillItem(Properties settings, Item bodyType) {
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

        boolean result = super.mineBlock(stack, world, state, pos, miner);

        if (!(world instanceof ServerLevel serverWorld) || !(miner instanceof ServerPlayer player)) {
            return result;
        }

        // only break area if main block is mineable
        boolean isPickaxeBlock = state.is(BlockTags.MINEABLE_WITH_PICKAXE);
        boolean isShovelBlock = state.is(BlockTags.MINEABLE_WITH_SHOVEL);
        boolean isNotWoodenBlock = state.is(BlockTags.INCORRECT_FOR_WOODEN_TOOL);
        if (!isPickaxeBlock && !isShovelBlock || isNotWoodenBlock) {
            return result;
        }

        // get original block hardness
        float originalHardness = state.getDestroySpeed(world, pos);
        if (originalHardness <= 0) {
            return result;
        }

        // determine the 3x3 area
        Vec3 lookVec = miner.getLookAngle();
        double ax = Math.abs(lookVec.x);
        double ay = Math.abs(lookVec.y);
        double az = Math.abs(lookVec.z);

        List<BlockPos> toBreak = new ArrayList<>();
        if (ay >= ax && ay >= az) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    BlockPos p = pos.offset(dx, 0, dz);
                    if (!p.equals(pos)) toBreak.add(p);
                }
            }
        } else if (ax >= az) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    BlockPos p = pos.offset(0, dy, dz);
                    if (!p.equals(pos)) toBreak.add(p);
                }
            }
        } else {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    BlockPos p = pos.offset(dx, dy, 0);
                    if (!p.equals(pos)) toBreak.add(p);
                }
            }
        }

        for (BlockPos targetPos : toBreak) {
            BlockState targetState = serverWorld.getBlockState(targetPos);

            if (targetState.isAir() || targetState.getDestroySpeed(world, targetPos) == -1.0f) continue;

            // check if its mineable
            boolean isPickaxe = targetState.is(BlockTags.MINEABLE_WITH_PICKAXE);
            boolean isShovel = targetState.is(BlockTags.MINEABLE_WITH_SHOVEL);
            boolean isNotWooden = targetState.is(BlockTags.INCORRECT_FOR_WOODEN_TOOL);
            if (!(isPickaxe || isShovel) || isNotWooden) continue;

            // skip block if block hardness is greater than target
            float targetHardness = targetState.getDestroySpeed(world, targetPos);
            if (targetHardness > originalHardness) continue;

            BlockEntity blockEntity = serverWorld.getBlockEntity(targetPos);
            targetState.getBlock().playerDestroy(serverWorld, player, targetPos, targetState, blockEntity, stack);
            serverWorld.destroyBlock(targetPos, false);

            if (stack.isEmpty()) break;

            stack.hurtWithoutBreaking(1, player);
        }

        return result;
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

        IndustrialDrillItem.TipProperties properties = switch (blade) {
            case "iron" -> new IndustrialDrillItem.TipProperties(512, 14, Items.IRON_INGOT, 2.0F);
            case "gold" -> new IndustrialDrillItem.TipProperties(192, 22, Items.GOLD_INGOT, 2.0F);
            case "diamond" -> new IndustrialDrillItem.TipProperties(2304, 10, Items.DIAMOND, 3.0F);
            case "netherite" -> new IndustrialDrillItem.TipProperties(3456, 15, Items.NETHERITE_INGOT, 4.0F);
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

        ItemAttributeModifiers attributes = ItemAttributeModifiers.builder()
                .add(
                        Attributes.ATTACK_DAMAGE,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath(Reductive.MOD_ID, "drill_tip_damage"),
                                properties.attackDamage(),
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        EquipmentSlotGroup.MAINHAND
                )
                .add(
                        Attributes.ATTACK_SPEED,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath(Reductive.MOD_ID, "drill_attack_speed"),
                                -2.0F,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        EquipmentSlotGroup.MAINHAND
                )
                .build();

        if (!attributes.equals(stack.get(DataComponents.ATTRIBUTE_MODIFIERS))) {
            stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attributes);
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
