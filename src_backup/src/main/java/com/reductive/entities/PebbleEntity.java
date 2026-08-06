package com.reductive.entities;

import com.reductive.ReductiveItemRegistry;
import net.minecraft.entity.*;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;

public class PebbleEntity extends ThrownItemEntity {
    public PebbleEntity(EntityType<? extends ThrownItemEntity> type, World world) {
        super(type, world);
    }

    @Override
    protected Item getDefaultItem() {
        return ReductiveItemRegistry.PEBBLE;
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
    }

    @Override
    protected double getGravity() {
        return 0.03;
    }

    @Override
    protected boolean canHit(Entity entity) {
        return super.canHit(entity) && entity != this.getOwner();
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        super.onEntityHit(entityHitResult);
        Entity target = entityHitResult.getEntity();

        if (!(this.getWorld() instanceof ServerWorld serverWorld)) return;


        float pebbleDamage = 2.0f * (float) this.getVelocity().length() + 1.0f;
        target.damage(
                serverWorld,
                this.getDamageSources().thrown(this, this.getOwner()),
                pebbleDamage
        );

        this.playSound(SoundEvents.BLOCK_STONE_HIT, 1.0f, 1.0f);

        this.discard();
    }

    @Override
    protected void onBlockHit(BlockHitResult result) {
        super.onBlockHit(result);
        this.playSound(SoundEvents.BLOCK_STONE_HIT, 1.0f, 1.0f);
        this.discard();
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        super.onCollision(hitResult);
        this.playSound(SoundEvents.BLOCK_STONE_HIT, 1.0f, 1.0f);
        this.discard();
    }

    @Override
    public ItemStack getStack() {
        return new ItemStack(ReductiveItemRegistry.PEBBLE);
    }
}
