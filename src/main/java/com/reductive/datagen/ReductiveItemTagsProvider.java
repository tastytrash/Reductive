package com.reductive.datagen;

import com.reductive.ReductiveItemRegistry;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.ItemTags;

import java.util.concurrent.CompletableFuture;

public class ReductiveItemTagsProvider extends FabricTagProvider.ItemTagProvider {

    public ReductiveItemTagsProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> completableFuture) {
        super(output, completableFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {
//        valueLookupBuilder(ItemTags. )
//                .add(ReductiveItemRegistry.DIAMOND_DRILL_TIP);
    }
}
