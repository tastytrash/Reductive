package com.reductive.items;

import com.reductive.datagen.ReductiveComponents;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantable;
import net.minecraft.world.item.enchantment.Repairable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class IndustrialDrillItem extends Item {
    private final Item bodyType;

    public IndustrialDrillItem(Properties settings, Item bodyType) {
        super(settings);
        this.bodyType = bodyType;
    }


    @Override
    public boolean mineBlock(ItemStack stack, Level world, BlockState state, BlockPos pos, LivingEntity miner) {
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

        int entityId = miner.getId();

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

            // multiplayer (not tested)
            ClientboundBlockDestructionPacket packet = new ClientboundBlockDestructionPacket(entityId, targetPos, 10);
            double sendRadiusSq = 64.0 * 64.0;
            for (ServerPlayer other : serverWorld.players()) {
                if (other.distanceToSqr(targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5) <= sendRadiusSq) {
                    other.connection.send(packet);
                }
            }

            // break
            if (serverWorld.removeBlock(targetPos, false)) {
                Block.dropResources(targetState, serverWorld, targetPos, serverWorld.getBlockEntity(targetPos), miner, stack);
            }

            // damage for each block
            if (!stack.isEmpty()) {
                stack.hurtWithoutBreaking(1, (Player) miner);
                if (stack.isEmpty()) {
                    ItemStack bodyOnly = new ItemStack(bodyType);
                    miner.handleExtraItemsCreatedOnUse(bodyOnly);
                    serverWorld.playSound(null, miner.getX(), miner.getY(), miner.getZ(),
                            SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 1.0f, 1.0f);
                }
            }

            if (stack.isEmpty()) break;
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


    private void applyDynamicComponents(ItemStack stack) {
        String tip = stack.get(ReductiveComponents.TIP_TYPE);
        if (tip == null) return;

        // 1. Dynamic Durability Management
        int durability = switch (tip) {
            case "iron" -> 1024;
            case "gold" -> 384;
            case "diamond" -> 4608;
            case "netherite" -> 6912;
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
