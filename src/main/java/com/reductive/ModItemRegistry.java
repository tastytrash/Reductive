package com.reductive;

import com.reductive.items.SlingshotItem;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import java.util.function.Function;

public class ModItemRegistry {
    public static final Item PEBBLE = register("pebble", Item::new, new Item.Settings());
    public static final Item SLINGSHOT = register(
            "slingshot",
            SlingshotItem::new, // constructor reference
            new Item.Settings().maxCount(1).maxDamage(256)
    );

    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, Reductive.MOD_ID, item);
    }

    public static Item register(String name, Function<Item.Settings, Item> itemFactory, Item.Settings settings) {
        // Create the item key.
        RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(Reductive.MOD_ID, name));

        // Create the item instance.
        Item item = itemFactory.apply(settings.registryKey(itemKey));

        // Register the item.
        Registry.register(Registries.ITEM, itemKey, item);

        return item;
    }

    public static void initialize() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS)
                .register((itemGroup) -> itemGroup.add(ModItemRegistry.PEBBLE));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT)
                .register((itemGroup) -> itemGroup.add(ModItemRegistry.SLINGSHOT));
    }
}
