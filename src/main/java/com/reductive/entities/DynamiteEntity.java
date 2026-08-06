package com.reductive.entities;

import com.reductive.ReductiveItemRegistry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

public class DynamiteEntity extends  ThrowableItemProjectile {
    public DynamiteEntity(EntityType<? extends DynamiteEntity> type, Level world) {
        super(type, world);
    }

    private ParticleOptions getParticleParameters() {
        ItemStack itemStack = this.getItem();
        return null;
    }


    @Override
    protected Item getDefaultItem() {
        return ReductiveItemRegistry.DYNAMITE;
    }

    @Override
    protected double getDefaultGravity() {
        return 0.03;
    }

    @Override
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);

        if (!this.level().isClientSide()) {
            this.level().explode(
                    this,
                    this.getX(), this.getY(), this.getZ(),
                    3.0f,
                    Level.ExplosionInteraction.TNT
            );
            this.discard();
        }
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(ReductiveItemRegistry.DYNAMITE);
    }
}
