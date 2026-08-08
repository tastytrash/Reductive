package com.reductive.items;

import com.reductive.Reductive;
import com.reductive.ReductiveItemRegistry;
import com.reductive.datagen.ReductiveComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.Enchantable;
import net.minecraft.world.item.enchantment.Repairable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class IndustrialChainsawItem extends Item {
    private final Item bodyType;

    public IndustrialChainsawItem(Properties settings, Item bodyType) {
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
        if (!state.is(BlockTags.MINEABLE_WITH_AXE)) {
            return result;
        }

        // get original block hardness
        float originalHardness = state.getDestroySpeed(world, pos);
        if (originalHardness <= 0) {
            return result;
        }

        // bfs
        int maxBlocks = getMaxBlocks(stack);

        List<BlockPos> toBreak = new ArrayList<>();
        Queue<BlockPos> queue = new LinkedList<>();
        Set<BlockPos> visited = new HashSet<>();

        // original position
        queue.add(pos);
        visited.add(pos);

        // check all neighbours, including adjacent
        List<int[]> directions = new ArrayList<>();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dy == 0 && dz == 0) continue; // Skip the center block itself
                    directions.add(new int[]{dx, dy, dz});
                }
            }
        }

        while (!queue.isEmpty() && toBreak.size() < maxBlocks) {
            BlockPos current = queue.poll();

            if (!current.equals(pos)) {
                toBreak.add(current);
            }

            for (int[] dir : directions) {
                BlockPos neighbor = current.offset(dir[0], dir[1], dir[2]);

                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    BlockState neighborState = serverWorld.getBlockState(neighbor);

                    // must be mineable
                    if (!neighborState.isAir() && neighborState.getDestroySpeed(world, neighbor) != -1.0f) {
                        if (neighborState.is(BlockTags.MINEABLE_WITH_AXE)) {

                            // check the block hardness
                            float targetHardness = neighborState.getDestroySpeed(world, neighbor);
                            if (targetHardness <= originalHardness) {
                                queue.add(neighbor);
                            }
                        }
                    }
                }
            }
        }

        // break all blocks
        for (BlockPos targetPos : toBreak) {
            BlockEntity blockEntity = serverWorld.getBlockEntity(targetPos);
            BlockState targetState = serverWorld.getBlockState(targetPos);
            Block.dropResources(targetState, serverWorld, targetPos, blockEntity, miner, stack);
            serverWorld.destroyBlock(targetPos, false, miner);

            if (stack.isEmpty()) break;

        }
        stack.hurtWithoutBreaking(1, player);

        return result;
    }

    private static int getMaxBlocks(ItemStack stack) {
        String blade = stack.get(ReductiveComponents.BLADE_TYPE);

        if (blade == null) {
            return 0;
        }

        return switch (blade) {
            case "iron" -> 7;
            case "gold" -> 3;
            case "diamond" -> 11;
            case "netherite" -> 15;
            default -> 0;
        };
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        String blade = stack.get(ReductiveComponents.BLADE_TYPE);
        if (blade == null) return 1.0f;

        boolean isAxeBlock = state.is(BlockTags.MINEABLE_WITH_AXE);

        if (isAxeBlock) {
            return switch (blade) {
                case "iron" -> 20.0f;
                case "gold" -> 44.0f;
                case "diamond" -> 28.0f;
                case "netherite" -> 34.0f;
                default -> 1.0f;
            };
        }

        return 1.0f;
    }

    public static void applyBladeProperties(ItemStack stack) {
        String blade = stack.get(ReductiveComponents.BLADE_TYPE);
        if (blade == null) return;

        BladeProperties properties = switch (blade) {
            case "iron" -> new BladeProperties(512, 14, Items.IRON_INGOT, 7.0F);
            case "gold" -> new BladeProperties(192, 22, Items.GOLD_INGOT, 6.0F);
            case "diamond" -> new BladeProperties(2304, 10, Items.DIAMOND, 8.0F);
            case "netherite" -> new BladeProperties(3456, 15, Items.NETHERITE_INGOT, 9.0F);
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

        Repairable repairable = new Repairable(HolderSet.direct(properties.repairItem().builtInRegistryHolder()));
        if (!repairable.equals(stack.get(DataComponents.REPAIRABLE))) {
            stack.set(DataComponents.REPAIRABLE, repairable);
        }
    }

    private record BladeProperties(int durability, int enchantability, Item repairItem, float attackDamage) {}

    @Override
    public void onCraftedPostProcess(ItemStack stack, Level world) {
        applyBladeProperties(stack);
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel world, Entity entity, @Nullable EquipmentSlot slot) {
        applyBladeProperties(stack);
    }
}
