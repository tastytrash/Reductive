package com.reductive.entities;

import com.reductive.ModItemRegistry;
import net.minecraft.entity.*;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;

public class DynamiteEntity extends ThrownItemEntity {
    public DynamiteEntity(EntityType<? extends DynamiteEntity> type, World world) {
        super(type, world);
    }

    private ParticleEffect getParticleParameters() {
        ItemStack itemStack = this.getStack();
        return null;
    }


    @Override
    protected Item getDefaultItem() {
        return ModItemRegistry.DYNAMITE;
    }

    @Override
    protected double getGravity() {
        return 0.03;
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        super.onCollision(hitResult);

        if (!this.getWorld().isClient) {
            this.getWorld().createExplosion(
                    this,
                    this.getX(), this.getY(), this.getZ(),
                    3.0f,
                    World.ExplosionSourceType.TNT
            );
            this.discard();
        }
    }

    @Override
    public ItemStack getStack() {
        return new ItemStack(ModItemRegistry.DYNAMITE);
    }
}
