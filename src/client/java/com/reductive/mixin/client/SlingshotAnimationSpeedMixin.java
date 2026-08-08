package com.reductive.mixin.client;

import com.reductive.items.SlingshotItem;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(LivingEntity.class)
public class SlingshotAnimationSpeedMixin {

    @Shadow protected int useItemRemaining;
    @Unique
    private float slingshotAnimationProgress = 0.0F;

    @Inject(method = "updateUsingItem", at = @At("HEAD"))
    private void speedUpSlingshotAnimationSmooth(ItemStack useItem, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;

        if (useItem.getItem() instanceof SlingshotItem && entity.isUsingItem()) {
            Holder<Enchantment> quickChargeHolder = entity.level().registryAccess()
                    .lookupOrThrow(Registries.ENCHANTMENT)
                    .getOrThrow(Enchantments.QUICK_CHARGE);
            int quickChargeLevel = EnchantmentHelper.getItemEnchantmentLevel(quickChargeHolder, useItem);

            float speedMultiplier = 0.5F + (quickChargeLevel * 0.25F);

            this.slingshotAnimationProgress += speedMultiplier;
            while (this.slingshotAnimationProgress >= 1.0F) {
                this.useItemRemaining--;
                this.slingshotAnimationProgress -= 1.0F;
            }
        } else {
            this.slingshotAnimationProgress = 0.0F;
        }
    }
}