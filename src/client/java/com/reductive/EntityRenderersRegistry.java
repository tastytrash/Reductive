package com.reductive;

import com.reductive.registries.ReductiveEntityRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;

public class EntityRenderersRegistry implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRenderers.register(ReductiveEntityRegistry.PEBBLE, context -> new ThrownItemRenderer<>(context, 0.75F, false));
        EntityRenderers.register(ReductiveEntityRegistry.DYNAMITE, context -> new ThrownItemRenderer<>(context, 1.0F, false));
        EntityRenderers.register(ReductiveEntityRegistry.TORCH, context -> new ThrownItemRenderer<>(context, 0.75F, false));
    }
}
