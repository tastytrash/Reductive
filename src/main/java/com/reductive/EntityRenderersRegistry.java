package com.reductive;

import com.reductive.entities.renderer.PebbleEntityRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.api.ClientModInitializer;

public class EntityRenderersRegistry implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // register your entity renderer here
        EntityRendererRegistry.register(ModEntityRegistry.PACKED_SNOWBALL, PebbleEntityRenderer::new);
    }
}
