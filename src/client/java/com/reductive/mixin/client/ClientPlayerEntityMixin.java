package com.reductive.mixin.client;

import com.reductive.items.ChainsawItem;
import com.reductive.items.DrillItem;
import com.reductive.items.IndustrialChainsawItem;
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

//        HitResult target = mc.hitResult;
//        if (target == null || target.getType() != Type.BLOCK) {
//            return;
//        }

        // cancel swing animation for drills
        ItemStack drill = hand == InteractionHand.MAIN_HAND ? mc.player.getMainHandItem() : mc.player.getOffhandItem();
        if (drill != null && drill.getItem() instanceof IndustrialDrillItem || drill.getItem() instanceof DrillItem) {
            ci.cancel();
        }

        // cancel swing animation for chainsaws
        ItemStack chainsaw = hand == InteractionHand.MAIN_HAND ? mc.player.getMainHandItem() : mc.player.getOffhandItem();
        if (chainsaw != null && chainsaw.getItem() instanceof IndustrialChainsawItem || chainsaw.getItem() instanceof ChainsawItem) {
            ci.cancel();
        }
    }
}