package com.reductive.items;

import com.reductive.registries.ReductiveEntityRegistry;
import com.reductive.entities.projectiles.DynamiteProjectile;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class DynamiteItem extends Item {
    public static final float POWER = 0.75F;

    public DynamiteItem(Item.Properties settings) {
        super(settings);
    }

    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        ItemStack stack = user.getItemInHand(hand);
        world.playSound(null, user.getX(), user.getY(), user.getZ(),
                SoundEvents.SNOWBALL_THROW, SoundSource.NEUTRAL,
                0.5F, 0.4F / (world.getRandom().nextFloat() * 0.4F + 0.8F));

        if (world instanceof ServerLevel serverWorld) {
            DynamiteProjectile dynamite = new DynamiteProjectile(ReductiveEntityRegistry.DYNAMITE, serverWorld);
            dynamite.setItem(stack.copy());
            dynamite.setOwner(user);
            dynamite.setPosRaw(user.getX(), user.getEyeY() - 0.1, user.getZ());
            dynamite.shootFromRotation(user, user.getXRot(), user.getYRot(), 0.0F, POWER, 1.0F);
            serverWorld.addFreshEntity(dynamite);
        }

        user.awardStat(Stats.ITEM_USED.get(this));
        stack.consume(1, user);
        return InteractionResult.SUCCESS;
    }
}
