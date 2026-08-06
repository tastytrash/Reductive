package com.reductive.items;

import com.reductive.ReductiveEntityRegistry;
import com.reductive.ReductiveItemRegistry;
import com.reductive.entities.DynamiteEntity;
import com.reductive.entities.PebbleEntity;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.item.*;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;

public class SlingshotItem extends ProjectileWeaponItem {

    public static final int RANGE = 15;
    public static final Predicate<ItemStack> SLING_PROJECTILES = (stack) ->
            stack.is(ReductiveItemRegistry.PEBBLE) || stack.is(Items.FIRE_CHARGE) || stack.is(ReductiveItemRegistry.DYNAMITE);

    public SlingshotItem(Item.Properties settings) {
        super(settings);
    }

    @Override
    public boolean releaseUsing(ItemStack stack, Level world, LivingEntity user, int remainingUseTicks) {
        if (!(user instanceof Player player)) return false;

        // find a projectile in inventory
        ItemStack ammoStack = player.getProjectile(stack);

        if (ammoStack.isEmpty() && player.isCreative()) {
            ammoStack = new ItemStack(ReductiveItemRegistry.PEBBLE);
        } else if (ammoStack.isEmpty()) {
            return false; // no ammo and survival
        }

        int useTicks = this.getUseDuration(stack, user) - remainingUseTicks;
        float pull = getPullProgress(useTicks);
        if (pull < 0.1f) return false;

        if (!world.isClientSide()) {
            if (!player.isCreative()) draw(stack, ammoStack, player);

            float speed = pull * 1.5f;        // adjust speed multiplier if needed
            float divergence = 1.0f;          // spread/inaccuracy

            if (ammoStack.is(ReductiveItemRegistry.PEBBLE)) {
                // create the pebble entity
                PebbleEntity pebble = new PebbleEntity(ReductiveEntityRegistry.PEBBLE, world);
                pebble.setOwner(player);
                pebble.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());

                // set velocity
                pebble.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0f, speed, divergence);

                // spawn it
                world.addFreshEntity(pebble);
            } else if (ammoStack.is(Items.FIRE_CHARGE)) {
                // create entity
                SmallFireball fireball = new SmallFireball(world, player, player.getLookAngle());
                fireball.setOwner(player);
                fireball.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());

                // direction/velocity
                fireball.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0f, speed, divergence);

                // spawn
                world.addFreshEntity(fireball);
            } else if (ammoStack.is(ReductiveItemRegistry.DYNAMITE)) {
                // create the pebble entity
                DynamiteEntity dynamite = new DynamiteEntity(ReductiveEntityRegistry.DYNAMITE, world);
                dynamite.setOwner(player);
                dynamite.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());

                // set velocity
                dynamite.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0f, speed, divergence);

                // spawn it
                world.addFreshEntity(dynamite);
            }

        }

        if (ammoStack.is(Items.FIRE_CHARGE)) {
            world.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.GHAST_SHOOT, SoundSource.PLAYERS,
                    1.0f, 1.0f / (world.getRandom().nextFloat() * 0.4f + 1.2f) + pull * 0.5f);
        } else {
            world.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS,
                    1.0f, 1.0f / (world.getRandom().nextFloat() * 0.4f + 1.2f) + pull * 0.5f);
        }


        player.awardStat(Stats.ITEM_USED.get(this));
        return true;
    }


    protected void shootProjectile(LivingEntity shooter, Projectile projectile, int index, float speed, float divergence, float yaw, @Nullable LivingEntity target) {
        projectile.shootFromRotation(shooter, shooter.getXRot(), shooter.getYRot() + yaw, 0.0F, speed, divergence);
    }

    public static float getPullProgress(int useTicks) {
        float f = (float)useTicks / 20.0F;
        f = (f * f + f * 2.0F) / 3.0F;
        if (f > 1.0F) {
            f = 1.0F;
        }

        return f;
    }

    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        ItemStack itemStack = user.getItemInHand(hand);
        boolean bl = !user.getProjectile(itemStack).isEmpty();
        if (!user.hasInfiniteMaterials() && !bl) {
            return InteractionResult.FAIL;
        } else {
            user.startUsingItem(hand);
            return InteractionResult.CONSUME;
        }
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.BOW;
    }


    public int getUseDuration(ItemStack stack, LivingEntity user) {
        return 72000;
    }

    @Override
    public Predicate<ItemStack> getAllSupportedProjectiles() {
        return SLING_PROJECTILES;
    }

    @Override
    public int getDefaultProjectileRange() {
        return RANGE;
    }
}
