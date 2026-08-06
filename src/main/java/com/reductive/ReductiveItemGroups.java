package com.reductive;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class ReductiveItemGroups {
    public static final CreativeModeTab REDUCTIVE = Registry.register(
            BuiltInRegistries.CREATIVE_MODE_TAB,
            com.reductive.Reductive.id("reductive"),
            CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
                    .icon(() -> new ItemStack(ReductiveItemRegistry.DYNAMITE))
                    .title(Component.translatable("itemGroup.reductive"))
                    .displayItems((displayContext, entries) -> {
                        entries.accept(ReductiveItemRegistry.PEBBLE);
                        entries.accept(ReductiveItemRegistry.DIAMOND_SHARD);
                        entries.accept(ReductiveItemRegistry.NETHERITE_NUGGET);

                        entries.accept(ReductiveItemRegistry.SLINGSHOT);
                        entries.accept(ReductiveItemRegistry.DYNAMITE);

                        entries.accept(ReductiveItemRegistry.IRON_DRILL_TIP);
                        entries.accept(ReductiveItemRegistry.GOLD_DRILL_TIP);
                        entries.accept(ReductiveItemRegistry.DIAMOND_DRILL_TIP);
                        entries.accept(ReductiveItemRegistry.NETHERITE_DRILL_TIP);
                        entries.accept(ReductiveItemRegistry.DRILL_BODY_BASIC);
                        entries.accept(ReductiveItemRegistry.DRILL_BASIC);
                        entries.accept(ReductiveItemRegistry.DRILL_BODY_INDUSTRIAL);
                        entries.accept(ReductiveItemRegistry.DRILL_INDUSTRIAL);
                        entries.accept(ReductiveItemRegistry.BASIC_DRILL_ENGINE);
                        entries.accept(ReductiveItemRegistry.INDUSTRIAL_DRILL_ENGINE);
                    })
                    .build()
    );

    public static void initialize() {
    }
}
