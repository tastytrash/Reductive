package com.reductive.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.reductive.Reductive;
import com.reductive.ReductiveItemRegistry;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.FishingHookRenderer;
import net.minecraft.client.renderer.entity.state.FishingHookRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

@Mixin(FishingHookRenderer.class)
public class FishingHookRendererMixin {
    @Unique
    private static final Identifier REDUCTIVE_FISHING_NET_TEXTURE = Reductive.id("textures/entity/fishing_net.png");
    @Unique
    private static final ThreadLocal<FishingHookRenderState> REDUCTIVE_RENDER_STATE = new ThreadLocal<>();
    @Unique
    private static final Map<FishingHookRenderState, Boolean> REDUCTIVE_FISHING_NET_HOOKS = new WeakHashMap<>();
    @Unique
    private static final Set<Item> REDUCTIVE_FISHING_RODS = Set.of(
            ReductiveItemRegistry.FISHING_ROD_COPPER,
            ReductiveItemRegistry.FISHING_ROD_IRON,
            ReductiveItemRegistry.FISHING_ROD_GOLD,
            ReductiveItemRegistry.FISHING_ROD_DIAMOND,
            ReductiveItemRegistry.FISHING_ROD_NETHERITE
    );

    @Inject(
            method = "extractRenderState(Lnet/minecraft/world/entity/projectile/FishingHook;Lnet/minecraft/client/renderer/entity/state/FishingHookRenderState;F)V",
            at = @At("TAIL")
    )
    private void reductive$recordFishingNetTexture(FishingHook hook, FishingHookRenderState state, float partialTick, CallbackInfo ci) {
        Player owner = hook.getPlayerOwner();
        boolean usesFishingNetTexture = owner != null && (
                REDUCTIVE_FISHING_RODS.contains(owner.getMainHandItem().getItem())
                        || REDUCTIVE_FISHING_RODS.contains(owner.getOffhandItem().getItem())
        );
        REDUCTIVE_FISHING_NET_HOOKS.put(state, usesFishingNetTexture);
    }

    @Inject(
            method = "submit(Lnet/minecraft/client/renderer/entity/state/FishingHookRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
            at = @At("HEAD")
    )
    private void reductive$setRenderState(FishingHookRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState, CallbackInfo ci) {
        REDUCTIVE_RENDER_STATE.set(state);
    }

    @Inject(
            method = "submit(Lnet/minecraft/client/renderer/entity/state/FishingHookRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
            at = @At("TAIL")
    )
    private void reductive$clearRenderState(FishingHookRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState, CallbackInfo ci) {
        REDUCTIVE_RENDER_STATE.remove();
    }

    @ModifyArg(
            method = "submit(Lnet/minecraft/client/renderer/entity/state/FishingHookRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitCustomGeometry(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/rendertype/RenderType;Lnet/minecraft/client/renderer/SubmitNodeCollector$CustomGeometryRenderer;)V",
                    ordinal = 0
            ),
            index = 1
    )
    private RenderType reductive$useFishingNetTexture(RenderType original) {
        FishingHookRenderState state = REDUCTIVE_RENDER_STATE.get();
        return state != null && REDUCTIVE_FISHING_NET_HOOKS.getOrDefault(state, false)
                ? RenderTypes.entityCutoutCull(REDUCTIVE_FISHING_NET_TEXTURE)
                : original;
    }
}
