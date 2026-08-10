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
                        entries.accept(ReductiveItemRegistry.COAL_CHUNK);
                        entries.accept(ReductiveItemRegistry.LAPIS_FRAGMENT);
                        entries.accept(ReductiveItemRegistry.EMERALD_JEWEL);
                        entries.accept(ReductiveItemRegistry.QUARTZ_SHARD);
                        entries.accept(ReductiveItemRegistry.DIAMOND_SHARD);
                        entries.accept(ReductiveItemRegistry.NETHERITE_NUGGET);
                        entries.accept(ReductiveItemRegistry.NETHER_STAR_FRAGMENT);

                        entries.accept(ReductiveItemRegistry.SPAWN_CORE_LESSER);
                        entries.accept(ReductiveItemRegistry.SPAWN_CORE_GREATER);

                        entries.accept(ReductiveItemRegistry.COPPER_COIL);
                        entries.accept(ReductiveItemRegistry.REDSTONE_PROCESSOR);

                        entries.accept(ReductiveItemRegistry.SLINGSHOT);
                        entries.accept(ReductiveItemRegistry.DYNAMITE);

                        entries.accept(ReductiveItemRegistry.MATCHSTICK);
                        entries.accept(ReductiveItemRegistry.LAPIS_BOTTLE);
                        entries.accept(ReductiveItemRegistry.LAPIS_EXPERIENCE_BOTTLE);
                        entries.accept(ReductiveItemRegistry.PORTABLE_CRAFTER);

                        entries.accept(ReductiveItemRegistry.DRILL_TIP_IRON);
                        entries.accept(ReductiveItemRegistry.DRILL_TIP_GOLD);
                        entries.accept(ReductiveItemRegistry.DRILL_TIP_DIAMOND);
                        entries.accept(ReductiveItemRegistry.DRILL_TIP_NETHERITE);
                        entries.accept(ReductiveItemRegistry.DRILL_BODY_BASIC);
                        entries.accept(ReductiveItemRegistry.DRILL_BASIC);
                        entries.accept(ReductiveItemRegistry.DRILL_BODY_INDUSTRIAL);
                        entries.accept(ReductiveItemRegistry.DRILL_INDUSTRIAL);

                        entries.accept(ReductiveItemRegistry.CHAINSAW_BLADE_IRON);
                        entries.accept(ReductiveItemRegistry.CHAINSAW_BLADE_GOLD);
                        entries.accept(ReductiveItemRegistry.CHAINSAW_BLADE_DIAMOND);
                        entries.accept(ReductiveItemRegistry.CHAINSAW_BLADE_NETHERITE);
                        entries.accept(ReductiveItemRegistry.CHAINSAW_BODY_BASIC);
                        entries.accept(ReductiveItemRegistry.CHAINSAW_BASIC);
                        entries.accept(ReductiveItemRegistry.CHAINSAW_BODY_INDUSTRIAL);
                        entries.accept(ReductiveItemRegistry.CHAINSAW_INDUSTRIAL);

                        entries.accept(ReductiveItemRegistry.BASIC_ENGINE);
                        entries.accept(ReductiveItemRegistry.INDUSTRIAL_ENGINE);

                        entries.accept(ReductiveBlockRegistry.EXPERIENCE_TANK.asItem());
                    })
                    .build()
    );

    public static void initialize() {}
}
