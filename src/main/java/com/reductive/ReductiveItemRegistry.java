package com.reductive;

import com.reductive.datagen.ReductiveComponents;
import com.reductive.items.DrillItem;
import com.reductive.items.DynamiteItem;
import com.reductive.items.IndustrialDrillItem;
import com.reductive.items.SlingshotItem;
import java.util.function.Function;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;

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
    public static final Item PEBBLE = register("pebble", Item::new, new Item.Properties());
    public static final Item DIAMOND_SHARD = register("diamond_shard", Item::new, new Item.Properties());
    public static final Item NETHERITE_NUGGET = register("netherite_nugget", Item::new, new Item.Properties());

    // weapons & combat
    public static final Item SLINGSHOT = register(
            "slingshot",
            SlingshotItem::new,
            new Item.Properties().stacksTo(1).durability(256)
    );
    public static final Item DYNAMITE = register(
            "dynamite",
            DynamiteItem::new,
            new Item.Properties().stacksTo(16).useCooldown(0.5f)
    );

    // drill tips
    public static final Item IRON_DRILL_TIP = register("iron_drill_tip", Item::new, new Item.Properties().stacksTo(1));
    public static final Item GOLD_DRILL_TIP = register("gold_drill_tip", Item::new, new Item.Properties().stacksTo(1));
    public static final Item DIAMOND_DRILL_TIP = register("diamond_drill_tip", Item::new, new Item.Properties().stacksTo(1));
    public static final Item NETHERITE_DRILL_TIP = register("netherite_drill_tip", Item::new, new Item.Properties().stacksTo(1));
    // drill bodies
    public static final Item DRILL_BODY_BASIC = register("drill_body_basic", Item::new, new Item.Properties().stacksTo(1));
    public static final Item DRILL_BODY_INDUSTRIAL = register("drill_body_industrial", Item::new, new Item.Properties().stacksTo(1));
    // drills
    public static final Item DRILL_BASIC = register("drill_basic",
            settings -> new DrillItem(settings, DRILL_BODY_BASIC),
            new Item.Properties().stacksTo(1).pickaxe(DRILL_TOOL_MATERIAL, 1.0F, -1.0F)
                    .component(ReductiveComponents.TIP_TYPE, "netherite")
    );
    public static final Item DRILL_INDUSTRIAL = register("drill_industrial",
            settings -> new IndustrialDrillItem(settings, DRILL_BODY_INDUSTRIAL),
            new Item.Properties().stacksTo(1).pickaxe(DRILL_TOOL_MATERIAL, 1.0F, -1.0F)
                    .component(ReductiveComponents.TIP_TYPE, "netherite")
    );
    // power cores
    public static final Item BASIC_DRILL_ENGINE = register("basic_drill_engine", Item::new, new Item.Properties().stacksTo(1));
    public static final Item INDUSTRIAL_DRILL_ENGINE = register("industrial_drill_engine", Item::new, new Item.Properties().stacksTo(1));


    private static Item registerItem(String name, Item item) {
        return Registry.register(BuiltInRegistries.ITEM, Reductive.MOD_ID, item);
    }

    public static Item register(String name, Function<Item.Properties, Item> itemFactory, Item.Properties settings) {
        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Reductive.MOD_ID, name));
        Item item = itemFactory.apply(settings.setId(itemKey));
        Registry.register(BuiltInRegistries.ITEM, itemKey, item);

        return item;
    }

    public static void initialize() {
    }
}
