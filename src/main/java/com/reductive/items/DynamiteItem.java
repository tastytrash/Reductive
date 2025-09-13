package com.reductive.items;

import com.reductive.ReductiveEntityRegistry;
import com.reductive.entities.DynamiteEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class DynamiteItem extends Item {
    public static final float POWER = 0.75F;

    public DynamiteItem(Item.Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        world.playSound(null, user.getX(), user.getY(), user.getZ(),
                SoundEvents.ENTITY_SNOWBALL_THROW, SoundCategory.NEUTRAL,
                0.5F, 0.4F / (world.getRandom().nextFloat() * 0.4F + 0.8F));

        if (world instanceof ServerWorld serverWorld) {
            DynamiteEntity dynamite = new DynamiteEntity(ReductiveEntityRegistry.DYNAMITE, serverWorld);
            dynamite.setItem(stack.copy());
            dynamite.setOwner(user);
            dynamite.setPos(user.getX(), user.getEyeY() - 0.1, user.getZ());
            dynamite.setVelocity(user, user.getPitch(), user.getYaw(), 0.0F, POWER, 1.0F);
            serverWorld.spawnEntity(dynamite);
        }

        user.incrementStat(Stats.USED.getOrCreateStat(this));
        stack.decrementUnlessCreative(1, user);
        return ActionResult.SUCCESS;
    }
}
