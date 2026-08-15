package com.reductive.mixins.client;

import com.reductive.items.*;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public class ClientPlayerEntityMixin {

    @Inject(method = "swing", at = @At("HEAD"), cancellable = true)
    private void reductive$onSwingHand(InteractionHand hand, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();

        // cancel swing animation for drills
        ItemStack item;
        assert mc.player != null;
        if (hand == InteractionHand.MAIN_HAND) {
            item = mc.player.getMainHandItem();
        } else {
            item = mc.player.getOffhandItem();
        }
        if (item.getItem() instanceof IndustrialDrillItem || item.getItem() instanceof DrillItem) {
            ci.cancel();
        }

        // cancel swing animation for chainsaws
        if (item.getItem() instanceof IndustrialChainsawItem || item.getItem() instanceof ChainsawItem) {
            ci.cancel();
        }
    }
}