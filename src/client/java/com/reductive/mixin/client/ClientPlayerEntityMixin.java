package com.reductive.mixin.client;

import com.reductive.items.DrillItem;
import com.reductive.items.IndustrialDrillItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public class ClientPlayerEntityMixin {

    @Inject(method = "swing", at = @At("HEAD"), cancellable = true)
    private void onSwingHand(InteractionHand hand, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null) return;

        HitResult target = mc.hitResult;
        if (target == null || target.getType() != Type.BLOCK) {
            return;
        }

        // cancel swing animation for drills
        ItemStack stack = hand == InteractionHand.MAIN_HAND ? mc.player.getMainHandItem() : mc.player.getOffhandItem();
        if (stack != null && stack.getItem() instanceof IndustrialDrillItem || stack.getItem() instanceof DrillItem) {
            ci.cancel();
        }
    }
}