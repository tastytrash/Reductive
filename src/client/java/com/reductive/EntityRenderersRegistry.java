package com.reductive;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;

public class EntityRenderersRegistry implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(ReductiveEntityRegistry.PEBBLE, context -> new ThrownItemRenderer<>(context, 0.75F, false));
        EntityRendererRegistry.register(ReductiveEntityRegistry.DYNAMITE, context -> new ThrownItemRenderer<>(context, 1.0F, false));
    }
}
