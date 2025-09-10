package com.reductive.items;

import com.reductive.entities.PackedSnowballEntity;
import com.reductive.ModItemRegistry;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.item.consume.UseAction;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public class SlingshotItem extends RangedWeaponItem {

    public static final int TICKS_PER_SECOND = 20;
    public static final int RANGE = 15;
    public static final Predicate<ItemStack> SLING_PROJECTILES = (stack) -> stack.isOf(ModItemRegistry.PEBBLE);

    public SlingshotItem(Item.Settings settings) {
        super(settings);
    }

    @Override
    public boolean onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        if (!(user instanceof PlayerEntity player)) return false;

        // find a pebble in the player's inventory
        ItemStack pebbleStack = ItemStack.EMPTY;
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack s = player.getInventory().getStack(i);
            if (s.isOf(ModItemRegistry.PEBBLE)) {
                pebbleStack = s;
                break;
            }
        }
        if (pebbleStack.isEmpty()) return false; // no ammo

        int useTicks = this.getMaxUseTime(stack, user) - remainingUseTicks;
        float pull = getPullProgress(useTicks);
        if (pull < 0.1f) return false;

        if (!world.isClient) {
            // spawn the PackedSnowballEntity
            PackedSnowballEntity snowball = new PackedSnowballEntity(world, player, new ItemStack(ModItemRegistry.PEBBLE));
            snowball.setVelocity(player, player.getPitch(), player.getYaw(), 0f, pull * 1.5f, 2f);
            world.spawnEntity(snowball);

            // consume one pebble if not in creative
            if (!player.isCreative()) pebbleStack.decrement(1);
        }

        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENTITY_ARROW_SHOOT, SoundCategory.PLAYERS,
                1.0f, 1.0f / (world.getRandom().nextFloat() * 0.4f + 1.2f) + pull * 0.5f);

        player.incrementStat(Stats.USED.getOrCreateStat(this));
        return true;
    }

    protected void shoot(LivingEntity shooter, ProjectileEntity projectile, int index, float speed, float divergence, float yaw, @Nullable LivingEntity target) {
        projectile.setVelocity(shooter, shooter.getPitch(), shooter.getYaw() + yaw, 0.0F, speed, divergence);
    }

    public static float getPullProgress(int useTicks) {
        float f = (float)useTicks / 20.0F;
        f = (f * f + f * 2.0F) / 3.0F;
        if (f > 1.0F) {
            f = 1.0F;
        }

        return f;
    }

    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        boolean bl = !user.getProjectileType(itemStack).isEmpty();
        if (!user.isInCreativeMode() && !bl) {
            return ActionResult.FAIL;
        } else {
            user.setCurrentHand(hand);
            return ActionResult.CONSUME;
        }
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BOW;
    }

    public int getMaxUseTime(ItemStack stack, LivingEntity user) {
        return 72000 / 2;
    }

    @Override
    public Predicate<ItemStack> getProjectiles() {
        return SLING_PROJECTILES;
    }

    @Override
    public int getRange() {
        return RANGE;
    }
}
