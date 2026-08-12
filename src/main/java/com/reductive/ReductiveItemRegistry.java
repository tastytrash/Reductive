package com.reductive;

import com.reductive.datagen.ReductiveComponents;
import com.reductive.items.*;

import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.DamageResistant;

import java.util.function.Function;

public class ReductiveItemRegistry {
    public static ToolMaterial DRILL_TOOL_MATERIAL = new ToolMaterial(
            BlockTags.INCORRECT_FOR_WOODEN_TOOL,
            1,
            0.0F,
            0.0F,
            1,
            null
    );

    public static ToolMaterial CHAINSAW_TOOL_MATERIAL = new ToolMaterial(
            BlockTags.MINEABLE_WITH_AXE,
            1,
            0.0F,
            0.0F,
            1,
            null
    );

    // resources
    public static final Item PEBBLE = register("pebble", Item::new, new Item.Properties());
    public static final Item COAL_CHUNK = register("coal_chunk", Item::new, new Item.Properties());
    public static final Item LAPIS_FRAGMENT = register("lapis_fragment", Item::new, new Item.Properties());
    public static final Item EMERALD_JEWEL = register("emerald_jewel", Item::new, new Item.Properties());
    public static final Item QUARTZ_SHARD = register("quartz_shard", Item::new, new Item.Properties());
    public static final Item DIAMOND_SHARD = register("diamond_shard", Item::new, new Item.Properties());
    public static final Item NETHERITE_NUGGET = register("netherite_nugget", Item::new, new Item.Properties());
    public static final Item NETHER_STAR_FRAGMENT = register("nether_star_fragment", Item::new, new Item.Properties().rarity(Rarity.RARE).component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true).delayedComponent(DataComponents.DAMAGE_RESISTANT, (context) -> new DamageResistant(context.getOrThrow(DamageTypeTags.IS_EXPLOSION))));

    public static final Item SPAWN_CORE_LESSER = register("spawn_core_lesser", Item::new, new Item.Properties());
    public static final Item SPAWN_CORE_GREATER = register("spawn_core_greater", Item::new, new Item.Properties());

    public static final Item COPPER_COIL = register("copper_coil", Item::new, new Item.Properties());
    public static final Item REDSTONE_PROCESSOR = register("redstone_processor", Item::new, new Item.Properties());
    public static final Item FISHING_NET = register("fishing_net", Item::new, new Item.Properties());
    public static final Item COPPER_ROD = register("copper_rod", Item::new, new Item.Properties());
    public static final Item IRON_ROD = register("iron_rod", Item::new, new Item.Properties());
    public static final Item GOLD_ROD = register("gold_rod", Item::new, new Item.Properties());
    public static final Item DIAMOND_ROD = register("diamond_rod", Item::new, new Item.Properties());


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

    // utility
    public static final Item MATCHSTICK = register("matchstick", MatchstickItem::new, new Item.Properties());

    public static final Item LAPIS_BOTTLE = register("lapis_bottle", Item::new, new Item.Properties());
    public static final Item LAPIS_EXPERIENCE_BOTTLE = register("lapis_experience_bottle", LapisExperienceBottleItem::new, new Item.Properties().rarity((Rarity.UNCOMMON)).component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true));

    public static final Item BAIT_TREASURE = register("bait_treasure", Item::new, new Item.Properties());

    // fishing rods
    public static final Item FISHING_ROD_COPPER = register("fishing_rod_copper", FishingRodItem::new, new Item.Properties().stacksTo(1).durability(128));
    public static final Item FISHING_ROD_IRON = register("fishing_rod_iron", FishingRodItem::new, new Item.Properties().stacksTo(1).durability(192));
    public static final Item FISHING_ROD_GOLD = register("fishing_rod_gold", FishingRodItem::new, new Item.Properties().stacksTo(1).durability(32));
    public static final Item FISHING_ROD_DIAMOND = register("fishing_rod_diamond", FishingRodItem::new, new Item.Properties().stacksTo(1).durability(384));
    public static final Item FISHING_ROD_NETHERITE = register("fishing_rod_netherite", FishingRodItem::new, new Item.Properties().stacksTo(1).durability(512));

    public static final Item PORTABLE_CRAFTER = register("portable_crafter", PortableCrafterItem::new, new Item.Properties().stacksTo(1));

    // drill tips
    public static final Item DRILL_TIP_IRON = register("drill_tip_iron", Item::new, new Item.Properties().stacksTo(1));
    public static final Item DRILL_TIP_GOLD = register("drill_tip_gold", Item::new, new Item.Properties().stacksTo(1));
    public static final Item DRILL_TIP_DIAMOND = register("drill_tip_diamond", Item::new, new Item.Properties().stacksTo(1));
    public static final Item DRILL_TIP_NETHERITE = register("drill_tip_netherite", Item::new, new Item.Properties().stacksTo(1));
    // drill bodies
    public static final Item DRILL_BODY_BASIC = register("drill_body_basic", Item::new, new Item.Properties().stacksTo(1));
    public static final Item DRILL_BODY_INDUSTRIAL = register("drill_body_industrial", Item::new, new Item.Properties().stacksTo(1));
    // drills
    public static final Item DRILL_BASIC = register("drill_basic",
            settings -> new DrillItem(settings, DRILL_BODY_BASIC),
            new Item.Properties().stacksTo(1).pickaxe(DRILL_TOOL_MATERIAL, 1.0F, -2.0F)
                    .component(ReductiveComponents.TIP_TYPE, "netherite")
    );
    public static final Item DRILL_INDUSTRIAL = register("drill_industrial",
            settings -> new IndustrialDrillItem(settings, DRILL_BODY_INDUSTRIAL),
            new Item.Properties().stacksTo(1).pickaxe(DRILL_TOOL_MATERIAL, 1.0F, -2.0F)
                    .component(ReductiveComponents.TIP_TYPE, "netherite")
    );

    // chainsaw tips
    public static final Item CHAINSAW_BLADE_IRON = register("chainsaw_blade_iron", Item::new, new Item.Properties().stacksTo(1));
    public static final Item CHAINSAW_BLADE_GOLD = register("chainsaw_blade_gold", Item::new, new Item.Properties().stacksTo(1));
    public static final Item CHAINSAW_BLADE_DIAMOND = register("chainsaw_blade_diamond", Item::new, new Item.Properties().stacksTo(1));
    public static final Item CHAINSAW_BLADE_NETHERITE = register("chainsaw_blade_netherite", Item::new, new Item.Properties().stacksTo(1));
    // chainsaw bodies
    public static final Item CHAINSAW_BODY_BASIC = register("chainsaw_body_basic", Item::new, new Item.Properties().stacksTo(1));
    public static final Item CHAINSAW_BODY_INDUSTRIAL = register("chainsaw_body_industrial", Item::new, new Item.Properties().stacksTo(1));
    // chainsaws
    public static final Item CHAINSAW_BASIC = register("chainsaw_basic",
            settings -> new ChainsawItem(settings, CHAINSAW_BODY_BASIC),
            new Item.Properties().stacksTo(1).axe(CHAINSAW_TOOL_MATERIAL, 1.0F, -2.8F)
                    .component(ReductiveComponents.BLADE_TYPE, "netherite")
    );
    public static final Item CHAINSAW_INDUSTRIAL = register("chainsaw_industrial",
            settings -> new IndustrialChainsawItem(settings, CHAINSAW_BODY_INDUSTRIAL),
            new Item.Properties().stacksTo(1).axe(CHAINSAW_TOOL_MATERIAL, 1.0F, -2.8F)
                    .component(ReductiveComponents.BLADE_TYPE, "netherite")
    );

    // engines
    public static final Item BASIC_ENGINE = register("basic_engine", Item::new, new Item.Properties().stacksTo(1));
    public static final Item INDUSTRIAL_ENGINE = register("industrial_engine", Item::new, new Item.Properties().stacksTo(1));

    public static Item register(String name, Function<Item.Properties, Item> itemFactory, Item.Properties settings) {
        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Reductive.MOD_ID, name));
        Item item = itemFactory.apply(settings.setId(itemKey));
        Registry.register(BuiltInRegistries.ITEM, itemKey, item);

        return item;
    }

    public static void initialize() {
    }
}
