package com.reductive.mixin;

import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownExperienceBottle;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ThrownExperienceBottle.class)
public class ThrownExperienceBottleMixin {

    @ModifyVariable(
            method = "onHit",
            at = @At("STORE"),
            name = "xpCount")
    private int setXpCount(int xpCount) {
        return 10;
    }
}