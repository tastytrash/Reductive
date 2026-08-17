package com.reductive.entities.projectiles;

import com.reductive.registries.ReductiveItemRegistry;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class TorchProjectile extends ThrowableItemProjectile {
    public TorchProjectile(EntityType<? extends ThrowableItemProjectile> type, Level world) {
        super(type, world);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.TORCH;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
    }

    @Override
    protected double getDefaultGravity() {
        return 0.03;
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        return super.canHitEntity(entity) && entity != this.getOwner();
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        super.onHitEntity(entityHitResult);
        Entity entity = entityHitResult.getEntity();

        if (!(this.level() instanceof ServerLevel serverWorld)) return;

        entity.hurtServer(
                serverWorld,
                this.damageSources().thrown(this, this.getOwner()),
                0.0f
        );

        this.playSound(SoundEvents.WOOD_HIT, 1.0f, 1.0f);
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        this.playSound(SoundEvents.WOOD_HIT, 1.0f, 1.0f);
        
        if (!this.level().isClientSide()) {
            Direction hitDirection = result.getDirection();
            BlockPos pos = result.getBlockPos().relative(hitDirection);
            
            if (!tryPlaceTorch(pos, hitDirection, result.getBlockPos())) {
                if (this.getOwner() instanceof Player player && !player.isCreative()) {
                    player.addItem(new ItemStack(Items.TORCH, 1));
                }
            }
        }
        
        this.discard();
    }

    private boolean tryPlaceTorch(BlockPos pos, Direction hitDirection, BlockPos hitPos) {
        if (!this.level().getBlockState(pos).canBeReplaced()) {
            return false;
        }
        
        if (!this.level().getBlockState(hitPos).isFaceSturdy(this.level(), hitPos, hitDirection.getOpposite())) {
            return false;
        }
        
        if (hitDirection == Direction.UP) {
            this.level().setBlock(pos, Blocks.TORCH.defaultBlockState(), 3);
            return true;
        }
        
        if (hitDirection == Direction.DOWN) return false;
        
        this.level().setBlock(pos, Blocks.WALL_TORCH.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, hitDirection), 3);
        return true;
    }

    @Override
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);
        this.playSound(SoundEvents.STONE_HIT, 1.0f, 1.0f);
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(Items.TORCH);
    }
}
