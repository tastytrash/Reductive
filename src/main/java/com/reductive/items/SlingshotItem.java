package com.reductive.items;

import com.reductive.ReductiveEntityRegistry;
import com.reductive.ReductiveItemRegistry;
import com.reductive.entities.projectiles.DynamiteProjectile;
import com.reductive.entities.projectiles.PebbleProjectile;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.jetbrains.annotations.Nullable;
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

    public SlingshotItem(Properties settings) {
        super(settings);
    }

    @Override
    public boolean releaseUsing(ItemStack stack, Level world, LivingEntity user, int remainingUseTicks) {
        if (!(user instanceof Player player)) return false;

        // find a projectile in inventory
        ItemStack ammoStack = player.getProjectile(stack);

        if (ammoStack.getItem() == Items.ARROW && player.isCreative()) {
            ammoStack = new ItemStack(ReductiveItemRegistry.PEBBLE);
        } else if (ammoStack.getItem() == Items.ARROW) {
            return false; // no ammo and survival
        }

        int useTicks = this.getUseDuration(stack, user) - remainingUseTicks;

        float pull = getPullProgress(useTicks);
        if (pull < 0.25f) return false;

        if (!world.isClientSide()) {
            float speed = pull * 1.5f;       // adjust speed multiplier if needed
            float divergence = 1.0f;         // spread/inaccuracy

            Holder<Enchantment> multishotHolder = world.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.MULTISHOT);
            boolean hasMultishot = EnchantmentHelper.getItemEnchantmentLevel(multishotHolder, stack) > 0;

            Holder<Enchantment> piercingHolder = world.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.PIERCING);
            byte piercingLevel = (byte) EnchantmentHelper.getItemEnchantmentLevel(piercingHolder, stack);

            if (ammoStack.is(ReductiveItemRegistry.PEBBLE)) {
                spawnPebble(world, player, speed, divergence, 0.0f, piercingLevel);

                if (hasMultishot && pull >= 1.0F) {
                    spawnPebble(world, player, speed, divergence, -8.0f, piercingLevel);
                    spawnPebble(world, player, speed, divergence, 8.0f, piercingLevel);
                }

            } else if (ammoStack.is(Items.FIRE_CHARGE)) {
                spawnProjectile(world, player, new SmallFireball(world, player, player.getLookAngle()), speed, divergence, 0.0f);

                if (hasMultishot && pull >= 1.0F) {
                    spawnProjectile(world, player, new SmallFireball(world, player, player.getLookAngle()), speed, divergence, -8.0f);
                    spawnProjectile(world, player, new SmallFireball(world, player, player.getLookAngle()), speed, divergence, 8.0f);
                }
            } else if (ammoStack.is(ReductiveItemRegistry.DYNAMITE)) {
                spawnProjectile(world, player, new DynamiteProjectile(ReductiveEntityRegistry.DYNAMITE, world), speed, divergence, 0.0f);

                if (hasMultishot && pull >= 1.0F) {
                    spawnProjectile(world, player, new DynamiteProjectile(ReductiveEntityRegistry.DYNAMITE, world), speed, divergence, -8.0f);
                    spawnProjectile(world, player, new DynamiteProjectile(ReductiveEntityRegistry.DYNAMITE, world), speed, divergence, 8.0f);
                }
            }

            if (!player.isCreative()) draw(stack, ammoStack, player);
            stack.hurtAndBreak(1, player, user.getUsedItemHand());
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

    private void spawnProjectile(Level world, Player player, Projectile projectile, float speed, float divergence, float yawOffset) {
        projectile.setOwner(player);
        projectile.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());
        projectile.shootFromRotation(player, player.getXRot(), player.getYRot() + yawOffset, 0.0F, speed, divergence);

        world.addFreshEntity(projectile);
    }

    private void spawnPebble(Level world, Player player, float speed, float divergence, float yawOffset, byte pierceLevel) {
        PebbleProjectile pebble = new PebbleProjectile(ReductiveEntityRegistry.PEBBLE, world);
        pebble.setPierceLevel(pierceLevel);

        spawnProjectile(world, player, pebble, speed, divergence, yawOffset);
    }

    public void shootProjectile(LivingEntity shooter, Projectile projectile, int index, float speed, float divergence, float yaw, @Nullable LivingEntity target) {
        projectile.shootFromRotation(shooter, shooter.getXRot(), shooter.getYRot() + yaw, 0.0F, speed, divergence);
    }

    public static float getPullProgress(int useTicks) {
        float f = (float) useTicks / 20.0F;
        f = (f * f + f * 2.0F) / 3.0F;
        if (f > 1.0F) {
            f = 1.0F;
        }

        return f;
    }

    public InteractionResult use(final Level world, final Player player, final InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        boolean bl = !player.getProjectile(itemStack).isEmpty();
        if (!player.hasInfiniteMaterials() && !bl) {
            return InteractionResult.FAIL;
        } else {
            player.startUsingItem(hand);
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
