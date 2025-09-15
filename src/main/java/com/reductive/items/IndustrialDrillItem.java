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
import net.minecraft.network.packet.s2c.play.BlockBreakingProgressS2CPacket;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class IndustrialDrillItem extends Item {
    private final Item bodyType;

    public IndustrialDrillItem(Settings settings, Item bodyType) {
        super(settings);
        this.bodyType = bodyType;
    }


    @Override
    public boolean postMine(ItemStack stack, World world, BlockState state, BlockPos pos, LivingEntity miner) {
        boolean result = super.postMine(stack, world, state, pos, miner);

        if (!(world instanceof ServerWorld serverWorld) || !(miner instanceof ServerPlayerEntity player)) {
            return result;
        }

        // only break area if block is mineable
        boolean isPickaxeBlock = state.isIn(BlockTags.PICKAXE_MINEABLE);
        boolean isShovelBlock = state.isIn(BlockTags.SHOVEL_MINEABLE);
        boolean isWoodenBlock = state.isIn(BlockTags.INCORRECT_FOR_WOODEN_TOOL);
        if (!isPickaxeBlock && !isShovelBlock && isWoodenBlock) {
            return result;
        }

        // get original block hardness
        float originalHardness = state.getHardness(world, pos);
        if (originalHardness <= 0) {
            return result;
        }

        // determine the 3x3 area
        Vec3d lookVec = miner.getRotationVector();
        double ax = Math.abs(lookVec.x);
        double ay = Math.abs(lookVec.y);
        double az = Math.abs(lookVec.z);

        List<BlockPos> toBreak = new ArrayList<>();
        if (ay >= ax && ay >= az) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    BlockPos p = pos.add(dx, 0, dz);
                    if (!p.equals(pos)) toBreak.add(p);
                }
            }
        } else if (ax >= az) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    BlockPos p = pos.add(0, dy, dz);
                    if (!p.equals(pos)) toBreak.add(p);
                }
            }
        } else {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    BlockPos p = pos.add(dx, dy, 0);
                    if (!p.equals(pos)) toBreak.add(p);
                }
            }
        }

        final int breakProgress = 10;
        int entityId = miner.getId();

        for (BlockPos targetPos : toBreak) {
            BlockState targetState = serverWorld.getBlockState(targetPos);

            if (targetState.isAir() || targetState.getHardness(world, targetPos) == -1.0f) continue;

            // check if its mineable
            boolean isPickaxe = targetState.isIn(BlockTags.PICKAXE_MINEABLE);
            boolean isShovel = targetState.isIn(BlockTags.SHOVEL_MINEABLE);
            boolean isWooden = targetState.isIn(BlockTags.INCORRECT_FOR_WOODEN_TOOL);
            if (!isPickaxe && !isShovel && isWooden) continue;

            // skip block if block hardness is greater than target
            float targetHardness = targetState.getHardness(world, targetPos);
            if (targetHardness > originalHardness) continue;

            // multiplayer (not tested)
            BlockBreakingProgressS2CPacket packet = new BlockBreakingProgressS2CPacket(entityId, targetPos, breakProgress);
            double sendRadiusSq = 64.0 * 64.0;
            for (ServerPlayerEntity other : serverWorld.getPlayers()) {
                if (other.squaredDistanceTo(targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5) <= sendRadiusSq) {
                    other.networkHandler.sendPacket(packet);
                }
            }

            // break
            serverWorld.breakBlock(targetPos, true, miner);

            // damage for each block
            if (!stack.isEmpty()) {
                stack.damage(1, (PlayerEntity) miner);
                if (stack.isEmpty()) {
                    ItemStack bodyOnly = new ItemStack(bodyType);
                    miner.giveOrDropStack(bodyOnly);
                    serverWorld.playSound(null, miner.getX(), miner.getY(), miner.getZ(),
                            SoundEvents.ENTITY_ITEM_BREAK, SoundCategory.PLAYERS, 1.0f, 1.0f);
                }
            }

            if (stack.isEmpty()) break;
        }

        return result;
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
