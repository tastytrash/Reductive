package com.reductive.mixin;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientBundleTooltip;
import org.apache.commons.lang3.math.Fraction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientBundleTooltip.class)
public class BundleTooltipComponentMixin {

    @Inject(method = "extractProgressbar", at = @At("HEAD"), cancellable = true, remap = false)
    private static void reductive$hideProgressBar(int x, int y, Font font, GuiGraphicsExtractor graphics, Fraction weight, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "backgroundHeight", at = @At("HEAD"), cancellable = true, remap = false)
    private void reductive$adjustBackgroundHeight(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(24);
    }

    @Inject(method = "getContentXOffset", at = @At("HEAD"), cancellable = true, remap = false)
    private static void reductive$adjustXOffset(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(-74);
    }
}
