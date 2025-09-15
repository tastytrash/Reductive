package com.reductive;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;

public class ReductiveItemGroups {
    public static final ItemGroup REDUCTIVE = Registry.register(
            Registries.ITEM_GROUP,
            com.reductive.Reductive.id("reductive"),
            FabricItemGroup.builder()
                    .icon(() -> new ItemStack(ReductiveItemRegistry.DYNAMITE))
                    .displayName(Text.translatable("itemGroup.reductive"))
                    .entries((displayContext, entries) -> {
                        entries.add(ReductiveItemRegistry.PEBBLE);
                        entries.add(ReductiveItemRegistry.COPPER_NUGGET);
                        entries.add(ReductiveItemRegistry.DIAMOND_SHARD);

                        entries.add(ReductiveItemRegistry.SLINGSHOT);
                        entries.add(ReductiveItemRegistry.DYNAMITE);

                        entries.add(ReductiveItemRegistry.IRON_DRILL_TIP);
                        entries.add(ReductiveItemRegistry.GOLD_DRILL_TIP);
                        entries.add(ReductiveItemRegistry.DIAMOND_DRILL_TIP);
                        entries.add(ReductiveItemRegistry.NETHERITE_DRILL_TIP);
                        entries.add(ReductiveItemRegistry.DRILL_BODY_BASIC);
                        entries.add(ReductiveItemRegistry.DRILL_BASIC);
                        entries.add(ReductiveItemRegistry.DRILL_BODY_INDUSTRIAL);
                        entries.add(ReductiveItemRegistry.DRILL_INDUSTRIAL);
                        entries.add(ReductiveItemRegistry.COAL_POWER_CORE);

                        entries.add(ReductiveBlockRegistry.REDSTONE_POWER_CORE.asItem());
                    })
                    .build()
    );

    public static void initialize() {
    }
}
