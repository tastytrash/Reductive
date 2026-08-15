package com.reductive.blocks;

import com.mojang.serialization.MapCodec;
import com.reductive.registries.ReductiveBlockEntityRegistry;
import com.reductive.blockentities.MaterialRecyclerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class MaterialRecyclerBlock extends BaseEntityBlock {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    public MaterialRecyclerBlock(Properties settings) {
        super(settings);
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH).setValue(LIT, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(MaterialRecyclerBlock::new);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, LIT);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MaterialRecyclerBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (world.getBlockEntity(pos) instanceof MaterialRecyclerBlockEntity materialRecyclerBlockEntity) {
            player.openMenu(materialRecyclerBlockEntity);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type == ReductiveBlockEntityRegistry.MATERIAL_RECYCLER_BLOCK_ENTITY) {
            return (lvl, pos, blockState, blockEntity) -> {
                if (blockEntity instanceof MaterialRecyclerBlockEntity recycler) {
                    MaterialRecyclerBlockEntity.tick(
                            lvl,
                            pos,
                            blockState,
                            recycler
                    );
                }
            };
        }

        return null;
    }

    public void animateTick(final BlockState state, final Level level, final BlockPos pos, final RandomSource random) {
        if ((Boolean)state.getValue(LIT)) {
            double x = (double)pos.getX() + (double)0.5F;
            double y = (double)pos.getY();
            double z = (double)pos.getZ() + (double)0.5F;
            if (random.nextDouble() < 0.1) {
                level.playLocalSound(x, y, z, SoundEvents.FURNACE_FIRE_CRACKLE, SoundSource.BLOCKS, 1.0F, 1.0F, false);
            }

            Direction direction = (Direction)state.getValue(FACING);
            Direction.Axis axis = direction.getAxis();
            double r = 0.52;
            double ss = random.nextDouble() * 0.6 - 0.3;
            double dx = axis == Direction.Axis.X ? (double)direction.getStepX() * 0.52 : ss;
            double dy = random.nextDouble() * (double)6.0F / (double)16.0F;
            double dz = axis == Direction.Axis.Z ? (double)direction.getStepZ() * 0.52 : ss;
            level.addParticle(ParticleTypes.SMOKE, x + dx, y + dy, z + dz, (double)0.0F, (double)0.0F, (double)0.0F);
            level.addParticle(ParticleTypes.FLAME, x + dx, y + dy, z + dz, (double)0.0F, (double)0.0F, (double)0.0F);
        }
    }
}
