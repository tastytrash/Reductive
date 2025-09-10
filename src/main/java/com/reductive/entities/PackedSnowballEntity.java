package com.reductive.entities;

import com.reductive.ModEntityRegistry;
import com.reductive.ModItemRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;

public class PackedSnowballEntity extends ThrownItemEntity {

    public PackedSnowballEntity(EntityType<? extends PackedSnowballEntity> entityType, World world) {
        super(entityType, world);
    }

    public PackedSnowballEntity(World world, LivingEntity owner, ItemStack stack) {
        super(ModEntityRegistry.PACKED_SNOWBALL, owner, world, stack);
    }

    public PackedSnowballEntity(World world, double x, double y, double z, ItemStack stack) {
        super(ModEntityRegistry.PACKED_SNOWBALL, x, y, z, world, stack);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItemRegistry.PEBBLE;
    }

    @Override
    public void handleStatus(byte status) {
        if (status == EntityStatuses.PLAY_DEATH_SOUND_OR_ADD_PROJECTILE_HIT_PARTICLES) {
            ParticleEffect particleEffect = this.getParticleEffect();
            for (int i = 0; i < 8; i++) {
                this.getWorld().addParticleClient(
                        particleEffect,
                        this.getX(), this.getY(), this.getZ(),
                        0.0, 0.0, 0.0
                );
            }
        }
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        super.onEntityHit(entityHitResult);
        Entity target = entityHitResult.getEntity();



        target.serverDamage(this.getDamageSources().thrown(this, this.getOwner()), 1.0f);
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        super.onCollision(hitResult);
        World world = getWorld();
        if (!world.isClient) {
            world.sendEntityStatus(this, EntityStatuses.PLAY_DEATH_SOUND_OR_ADD_PROJECTILE_HIT_PARTICLES);
            this.discard();
        }
    }

    private ParticleEffect getParticleEffect() {
        ItemStack itemStack = this.getStack();
        return itemStack.isEmpty()
                ? ParticleTypes.ITEM_SNOWBALL
                : new ItemStackParticleEffect(ParticleTypes.ITEM, itemStack);
    }

}