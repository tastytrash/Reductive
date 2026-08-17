package com.reductive.items;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class MagnetItem extends Item {
    public MagnetItem(final Item.Properties properties) {
        super(properties);
    }

    @Override
    public void inventoryTick(final ItemStack stack, final ServerLevel level, final Entity owner, final @Nullable EquipmentSlot slot) {
        if (level.isClientSide() || !(owner instanceof Player player) || (slot != EquipmentSlot.MAINHAND && slot != EquipmentSlot.OFFHAND)) {
            return;
        }

        double radius = 8.0;
        Vec3 playerPos = player.position();

        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, player.getBoundingBox().inflate(radius), ItemEntity::isAlive)) {
            double distanceSq = item.distanceToSqr(playerPos);

            if (distanceSq <= radius * radius) {
                double distance = Math.max(0.5, Math.sqrt(distanceSq));
                Vec3 toPlayer = playerPos.subtract(item.position());

                double pullStrength = Math.min(0.30, 0.15 / (distance / 2));

                Vec3 newVelocity = item.getDeltaMovement().scale(0.8).add(toPlayer.normalize().scale(pullStrength));
                if (newVelocity.y < 0.1) {
                    newVelocity = newVelocity.add(0, 0.04, 0);
                }

                item.setDeltaMovement(newVelocity);
                item.hurtMarked = true;
            }
        }
    }
}
