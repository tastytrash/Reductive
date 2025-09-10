package com.reductive.entities.renderer;

import com.reductive.ModEntityRegistry;
import com.reductive.ModItemRegistry;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.world.World;
import net.minecraft.item.Item;

public class PebbleEntityRenderer extends ThrownItemEntity {

    public PebbleEntityRenderer(EntityType<? extends ThrownItemEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected Item getDefaultItem() {
        return null;
    }
}
