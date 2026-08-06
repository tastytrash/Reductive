package com.reductive.entities;

import com.reductive.ReductiveItemRegistry;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class PebbleEntity extends ThrowableItemProjectile {
    public PebbleEntity(EntityType<? extends ThrowableItemProjectile> type, Level world) {
        super(type, world);
    }

    @Override
    protected Item getDefaultItem() {
        return ReductiveItemRegistry.PEBBLE;
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
        Entity target = entityHitResult.getEntity();

        if (!(this.level() instanceof ServerLevel serverWorld)) return;


        float pebbleDamage = 2.0f * (float) this.getDeltaMovement().length() + 1.0f;
        target.hurtServer(
                serverWorld,
                this.damageSources().thrown(this, this.getOwner()),
                pebbleDamage
        );

        this.playSound(SoundEvents.STONE_HIT, 1.0f, 1.0f);

        this.discard();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        this.playSound(SoundEvents.STONE_HIT, 1.0f, 1.0f);
        this.discard();
    }

    @Override
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);
        this.playSound(SoundEvents.STONE_HIT, 1.0f, 1.0f);
        this.discard();
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(ReductiveItemRegistry.PEBBLE);
    }
}
