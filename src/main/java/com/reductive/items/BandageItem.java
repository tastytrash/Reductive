package com.reductive.items;

import net.minecraft.core.Holder;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class BandageItem extends Item {
    public BandageItem(final Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(final Level level, final Player player, final InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public int getUseDuration(final ItemStack stack, final LivingEntity entity) {
        return 10;
    }

    @Override
    public ItemStack finishUsingItem(final ItemStack stack, final Level level, final LivingEntity entity) {
        if (!level.isClientSide() && entity instanceof Player player) {
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 125, 1));

            List<Holder<MobEffect>> effectsToRemove = new ArrayList<>();

            for (MobEffectInstance instance : player.getActiveEffects()) {
                Holder<MobEffect> effectHolder = instance.getEffect();
                if (!effectHolder.value().isBeneficial()) {
                    effectsToRemove.add(effectHolder);
                }
            }

            for (Holder<MobEffect> effectHolder : effectsToRemove) {
                player.removeEffect(effectHolder);
            }
        }
        if (entity instanceof Player player) {
            if (player.isCreative()) {
                return stack;
            }
        }

        stack.shrink(1);
        return stack;
    }

    @Override
    public ItemUseAnimation getUseAnimation(final ItemStack stack) {
        return ItemUseAnimation.BLOCK;
    }
}
