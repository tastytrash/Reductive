package com.reductive.blocks;

import com.mojang.serialization.MapCodec;
import com.reductive.ReductiveItemRegistry;
import com.reductive.blockentity.ExperienceTankBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;

public class ExperienceTankBlock extends Block implements EntityBlock {
    public static final MapCodec<ExperienceTankBlock> CODEC = simpleCodec(ExperienceTankBlock::new);

    public static final IntegerProperty LEVEL = IntegerProperty.create("level", 0, 16);

    public static final int MAX_XP = 1600;
    public static final int XP_PER_LEVEL = 100;

    public ExperienceTankBlock(Properties settings) {
        super(settings);

        registerDefaultState(defaultBlockState().setValue(LEVEL, 0));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LEVEL);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ExperienceTankBlockEntity(pos, state);
    }

    @Override
    public InteractionResult useItemOn(final ItemStack itemStack, final BlockState state, final Level level, final BlockPos pos, final Player player, final InteractionHand hand, final BlockHitResult hitResult) {
        if (itemStack.is(Items.GLASS_BOTTLE) || itemStack.is(ReductiveItemRegistry.LAPIS_BOTTLE)) {
            if (!(level.getBlockEntity(pos) instanceof ExperienceTankBlockEntity tankEntity)) {
                return InteractionResult.PASS;
            }

            int xpPerBottle = itemStack.is(Items.GLASS_BOTTLE) ? 10 : 100;

            if (tankEntity.getStoredXp() < xpPerBottle) {
                return InteractionResult.PASS;
            }

            if (level.isClientSide()) {
                return InteractionResult.SUCCESS;
            }

            tankEntity.setStoredXp(tankEntity.getStoredXp() - xpPerBottle);

            int xpLevel = Math.min(16, tankEntity.getStoredXp() / XP_PER_LEVEL);
            level.setBlockAndUpdate(pos, state.setValue(LEVEL, xpLevel));

            itemStack.shrink(1);
            ItemStack experienceBottle = itemStack.is(Items.GLASS_BOTTLE) ? new ItemStack(Items.EXPERIENCE_BOTTLE) : new ItemStack(ReductiveItemRegistry.LAPIS_EXPERIENCE_BOTTLE);
            if (!player.getInventory().add(experienceBottle)) {
                player.drop(experienceBottle, false);
            }

            level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 0.8F, 1.0F);

            return InteractionResult.SUCCESS;

            // use without item
        } else {
            if (!player.getAbilities().mayBuild) {
                return InteractionResult.PASS;
            }
            if (level.getBlockEntity(pos) instanceof ExperienceTankBlockEntity tankEntity) {
                int storedXp = tankEntity.getStoredXp();
                int playerXp = getPlayerXp(player);

                if (storedXp >= MAX_XP || playerXp <= 0) return InteractionResult.PASS;

                int tankCapacity = MAX_XP - storedXp;

                int xpTaken;
                if (player.isShiftKeyDown()) {
                    xpTaken = playerXp;
                } else {
                    xpTaken = Math.min(playerXp, XP_PER_LEVEL);
                }
                xpTaken = Math.min(xpTaken, tankCapacity);

                player.giveExperiencePoints(-xpTaken); tankEntity.setStoredXp(storedXp + xpTaken);

                int xpLevel = Math.min(16, tankEntity.getStoredXp() / XP_PER_LEVEL);

                level.setBlockAndUpdate(pos, state.setValue(LEVEL, xpLevel));

                // sound
                level.playSound(player, pos, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.BLOCKS, 0.4F, ((level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.2F) + 1.0F);

                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        }
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, BlockEntity blockEntity, ItemStack tool) {
        // drop the block
        popResource(level, pos, new ItemStack(this));

        // drop the stored XP
        if (!level.isClientSide() && blockEntity instanceof ExperienceTankBlockEntity tankEntity) {
            int storedXp = tankEntity.getStoredXp();

            if (storedXp > 0 && level instanceof ServerLevel serverLevel) {
//              this.tryDropExperience(serverLevel, pos, tool, ConstantInt.of(storedXp));
                this.popExperience(serverLevel, pos, storedXp);
            }
        }
    }

    public static int getPlayerXp(Player player) {
        int level = player.experienceLevel;
        int xpFromLevels;
        if (level <= 16) {
            xpFromLevels = level * level + 6 * level;
        } else if (level <= 31) {
            xpFromLevels = (int) (2.5 * level * level - 40.5 * level + 360);
        } else {
            xpFromLevels = (int) (4.5 * level * level - 162.5 * level + 2220);
        }

        int currentProgressXp = Math.round(player.experienceProgress * player.getXpNeededForNextLevel());

        return xpFromLevels + currentProgressXp;
    }

    public static int getLuminance(BlockState currentBlockState) {
        // return light level based on xp level
        return Math.min(currentBlockState.getValue(ExperienceTankBlock.LEVEL), 15);
    }

    // don't affect lighting as if its transparent
    protected float getShadeBrightness(final BlockState state, final BlockGetter level, final BlockPos pos) {
        return 1.0F;
    }
    protected boolean propagatesSkylightDown(final BlockState state) {
        return true;
    }
}
