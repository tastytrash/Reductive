package com.reductive.mixin.client;

import com.reductive.items.DrillItem;
import com.reductive.items.IndustrialDrillItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitResult.Type;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin {

    @Inject(method = "swingHand(Lnet/minecraft/util/Hand;)V", at = @At("HEAD"), cancellable = true)
    private void onSwingHand(Hand hand, CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null) return;

        HitResult target = mc.crosshairTarget;
        if (target == null || target.getType() != Type.BLOCK) {
            return;
        }

        // cancel swing animation for drills
        ItemStack stack = hand == Hand.MAIN_HAND ? mc.player.getMainHandStack() : mc.player.getOffHandStack();
        if (stack != null && stack.getItem() instanceof IndustrialDrillItem || stack.getItem() instanceof DrillItem) {
            ci.cancel();
        }
    }
}