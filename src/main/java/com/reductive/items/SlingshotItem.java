package com.reductive.items;

import com.reductive.ReductiveEntityRegistry;
import com.reductive.ReductiveItemRegistry;
import com.reductive.entities.projectiles.DynamiteProjectile;
import com.reductive.entities.projectiles.PebbleProjectile;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.entity.projectile.hurtingprojectile.windcharge.WindCharge;
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.phys.Vec3;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;

import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.jetbrains.annotations.Nullable;
import java.util.function.Predicate;

public class SlingshotItem extends ProjectileWeaponItem {

    public static final int RANGE = 15;
    public static final Predicate<ItemStack> SLING_PROJECTILES = (stack) ->
            stack.is(ReductiveItemRegistry.PEBBLE) || stack.is(Items.SNOWBALL) || stack.is(Items.WIND_CHARGE) || stack.is(Items.FIRE_CHARGE) || stack.is(ReductiveItemRegistry.DYNAMITE);

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

        if (!world.isClientSide() && world instanceof ServerLevel serverLevel) {
            float speed = pull * 1.5f;       // adjust speed multiplier if needed
            float divergence = 1.0f;         // spread/inaccuracy

            Holder<Enchantment> multishotHolder = world.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.MULTISHOT);
            int multishotLevel = EnchantmentHelper.getItemEnchantmentLevel(multishotHolder, stack);
            int projectileCount = multishotLevel > 0 ? 1 + (multishotLevel * 2) : 1;
            if (pull < 1f) projectileCount = 1;

            Holder<Enchantment> piercingHolder = world.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.PIERCING);
            byte piercingLevel = (byte) EnchantmentHelper.getItemEnchantmentLevel(piercingHolder, stack);

            float maxAngle = EnchantmentHelper.processProjectileSpread(serverLevel, stack, player, 0.0F);
            // fallback to 10
            if (maxAngle == 0.0F && multishotLevel > 0) maxAngle = 10.0F;

            float angleStep = projectileCount == 1 ? 0.0F : 2.0F * maxAngle / (float)(projectileCount - 1);
            float angleOffset = (float)((projectileCount - 1) % 2) * angleStep / 2.0F;
            float direction = 1.0F;

            // firing loop
            for (int i = 0; i < projectileCount; ++i) {
                float angle = angleOffset + direction * (float)((i + 1) / 2) * angleStep;
                direction = -direction;

                if (ammoStack.is(ReductiveItemRegistry.PEBBLE)) {
                    PebbleProjectile pebble = new PebbleProjectile(ReductiveEntityRegistry.PEBBLE, world);
                    pebble.setPierceLevel(piercingLevel);
                    spawnProjectile(world, player, pebble, speed, divergence, angle);
                } else if (ammoStack.is(Items.SNOWBALL)) {
                    Snowball snowball = new Snowball(EntityTypes.SNOWBALL, world);
                    // pierce mixin?
                    spawnProjectile(world, player, snowball, speed, divergence, angle);
                } else if (ammoStack.is(Items.WIND_CHARGE)) {
                    WindCharge windCharge = new WindCharge(EntityTypes.WIND_CHARGE, world);
                    spawnProjectile(world, player, windCharge, speed, divergence, angle);

                } else if (ammoStack.is(Items.FIRE_CHARGE)) {
                    Vec3 rotatedVector = getSpreadVelocityVector(player, angle);
                    SmallFireball fireball = new SmallFireball(world, player, rotatedVector);

                    fireball.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());
                    world.addFreshEntity(fireball);

                } else if (ammoStack.is(ReductiveItemRegistry.DYNAMITE)) {
                    DynamiteProjectile dynamite = new DynamiteProjectile(ReductiveEntityRegistry.DYNAMITE, world);
                    spawnProjectile(world, player, dynamite, speed, divergence, angle);
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
        Vec3 spreadVector = getSpreadVelocityVector(player, yawOffset);

        projectile.shoot(spreadVector.x, spreadVector.y, spreadVector.z, speed, divergence);

        world.addFreshEntity(projectile);
    }

    private Vec3 getSpreadVelocityVector(Player player, float angle) {
        Vec3 upVector = player.getUpVector(1.0F);

        Quaternionf upQuaternion = (new Quaternionf()).setAngleAxis(
                angle * ((float)Math.PI / 180F),
                upVector.x, upVector.y, upVector.z
        );

        Vec3 viewVec = player.getViewVector(1.0F);
        Vector3f shotVector = viewVec.toVector3f().rotate(upQuaternion);

        return new Vec3(shotVector);
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
