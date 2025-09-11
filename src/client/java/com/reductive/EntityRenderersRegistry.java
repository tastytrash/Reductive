package com.reductive;

import com.reductive.entities.PebbleEntity;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;

public class EntityRenderersRegistry implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        System.out.println("registering pebble renderer");
        EntityRendererRegistry.register(ModEntityRegistry.PEBBLE, context -> new FlyingItemEntityRenderer<>(context, 0.75F, true));
    }
}
