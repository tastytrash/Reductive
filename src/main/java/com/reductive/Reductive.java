package com.reductive;

import com.reductive.datagen.ReductiveComponents;

import com.reductive.items.IndustrialChainsawItem;
import com.reductive.items.IndustrialDrillItem;
import com.reductive.registries.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.resources.Identifier;

import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Reductive implements ModInitializer {
    public static final String MOD_ID = "reductive";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing reductive");
        ReductiveItemRegistry.initialize();
        ReductiveEntityRegistry.initialize();
        ReductiveCreativeTab.initialize();
        ReductiveComponents.initialize();
        ReductiveBlockRegistry.initialize();
        ReductiveBlockEntityRegistry.initialize();
        ReductiveMenuRegistry.initialize();
        ReductiveLootTableRegistry.initialize();
        ReductiveFuelRegistry.initialize();


        // mining in creative
        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            if (player.isCreative() && !world.isClientSide()) {
                ItemStack mainHandItem = player.getMainHandItem();

                if (mainHandItem.getItem() instanceof IndustrialChainsawItem chainsaw) {
                    chainsaw.mineBlock(mainHandItem, world, state, pos, player);
                } else if (mainHandItem.getItem() instanceof IndustrialDrillItem drill) {
                    drill.mineBlock(mainHandItem, world, state, pos, player);
                }
            }
            return true;
        });
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}