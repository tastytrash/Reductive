package com.reductive;

import com.reductive.items.DynamiteItem;
import com.reductive.items.IronDrillItem;
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
            SlingshotItem::new,
            new Item.Settings().maxCount(1).maxDamage(256)
    );
    public static final Item DYNAMITE = register(
            "dynamite",
            DynamiteItem::new,
            new Item.Settings().maxCount(16).useCooldown(0.5f)
    );
    public static final Item IRON_DRILL = register(
            "iron_drill",
            IronDrillItem::new,
            new Item.Settings()
    );


    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, Reductive.MOD_ID, item);
    }

    public static Item register(String name, Function<Item.Settings, Item> itemFactory, Item.Settings settings) {
        RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(Reductive.MOD_ID, name));
        Item item = itemFactory.apply(settings.registryKey(itemKey));
        Registry.register(Registries.ITEM, itemKey, item);

        return item;
    }

    public static void initialize() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS)
                .register((itemGroup) -> itemGroup.add(ModItemRegistry.PEBBLE));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT)
                .register((itemGroup) -> itemGroup.add(ModItemRegistry.SLINGSHOT));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT)
                .register((itemGroup) -> itemGroup.add(ModItemRegistry.DYNAMITE));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS)
                .register((itemGroup) -> itemGroup.add(ModItemRegistry.IRON_DRILL));
    }
}
