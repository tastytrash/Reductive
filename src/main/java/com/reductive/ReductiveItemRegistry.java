package com.reductive;

import com.reductive.datagen.ReductiveComponents;
import com.reductive.items.DrillItem;
import com.reductive.items.DynamiteItem;
import com.reductive.items.IndustrialDrillItem;
import com.reductive.items.SlingshotItem;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ToolMaterial;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

import java.util.function.Function;

public class ReductiveItemRegistry {
    public static ToolMaterial DRILL_TOOL_MATERIAL = new ToolMaterial(
            BlockTags.INCORRECT_FOR_WOODEN_TOOL,
            1,
            0.0F,
            1.5F,
            22,
            null
    );

    // resources
    public static final Item PEBBLE = register("pebble", Item::new, new Item.Settings());
    public static final Item COPPER_NUGGET = register("copper_nugget", Item::new, new Item.Settings());
    public static final Item DIAMOND_SHARD = register("diamond_shard", Item::new, new Item.Settings());

    // weapons & combat
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

    // drill tips
    public static final Item IRON_DRILL_TIP = register("iron_drill_tip", Item::new, new Item.Settings().maxCount(1));
    public static final Item GOLD_DRILL_TIP = register("gold_drill_tip", Item::new, new Item.Settings().maxCount(1));
    public static final Item DIAMOND_DRILL_TIP = register("diamond_drill_tip", Item::new, new Item.Settings().maxCount(1));
    public static final Item NETHERITE_DRILL_TIP = register("netherite_drill_tip", Item::new, new Item.Settings().maxCount(1));
    // drill bodies
    public static final Item DRILL_BODY_BASIC = register("drill_body_basic", Item::new, new Item.Settings().maxCount(1));
    public static final Item DRILL_BODY_INDUSTRIAL = register("drill_body_industrial", Item::new, new Item.Settings().maxCount(1));
    // drills
    public static final Item DRILL_BASIC = register("drill_basic",
            settings -> new DrillItem(settings, DRILL_BODY_BASIC),
            new Item.Settings().maxCount(1).pickaxe(DRILL_TOOL_MATERIAL, 1.0F, -1.0F)
                    .component(ReductiveComponents.TIP_TYPE, "netherite")
    );
    public static final Item DRILL_INDUSTRIAL = register("drill_industrial",
            settings -> new IndustrialDrillItem(settings, DRILL_BODY_INDUSTRIAL),
            new Item.Settings().maxCount(1).pickaxe(DRILL_TOOL_MATERIAL, 1.0F, -1.0F)
                    .component(ReductiveComponents.TIP_TYPE, "netherite")
    );
    // power cores
    public static final Item BASIC_DRILL_ENGINE = register("basic_drill_engine", Item::new, new Item.Settings().maxCount(1));

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
    }
}
