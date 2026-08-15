package com.reductive.helpers;

import com.reductive.Reductive;
import com.reductive.datagen.ReductiveComponents;

import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.Enchantable;
import net.minecraft.world.item.enchantment.Repairable;

public class DrillHelper {
    public static void applyTipProperties(ItemStack stack) {
        String tipType = stack.get(ReductiveComponents.TIP_TYPE);
        if (tipType == null) return;

        TipProperties properties = switch (tipType) {
            case "iron" -> new TipProperties(512, 14, Items.IRON_INGOT, 2.0F);
            case "gold" -> new TipProperties(192, 22, Items.GOLD_INGOT, 2.0F);
            case "diamond" -> new TipProperties(2304, 10, Items.DIAMOND, 3.0F);
            case "netherite" -> new TipProperties(3456, 15, Items.NETHERITE_INGOT, 4.0F);
            default -> null;
        };
        if (properties == null) return;

        Integer currentMax = stack.get(DataComponents.MAX_DAMAGE);
        if (currentMax == null || currentMax != properties.durability()) {
            stack.set(DataComponents.MAX_DAMAGE, properties.durability());
        }

        Enchantable enchantable = new Enchantable(properties.enchantability());
        if (!enchantable.equals(stack.get(DataComponents.ENCHANTABLE))) {
            stack.set(DataComponents.ENCHANTABLE, enchantable);
        }

        Repairable repairable = new Repairable(HolderSet.direct(BuiltInRegistries.ITEM.wrapAsHolder(properties.repairItem())));
        if (!repairable.equals(stack.get(DataComponents.REPAIRABLE))) {
            stack.set(DataComponents.REPAIRABLE, repairable);
        }

        ItemAttributeModifiers attributes = ItemAttributeModifiers.builder()
                .add(
                        Attributes.ATTACK_DAMAGE,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath(Reductive.MOD_ID, "drill_tip_damage"),
                                properties.attackDamage(),
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        EquipmentSlotGroup.MAINHAND
                )
                .add(
                        Attributes.ATTACK_SPEED,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath(Reductive.MOD_ID, "drill_attack_speed"),
                                -2.0F,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        EquipmentSlotGroup.MAINHAND
                )
                .build();

        if (!attributes.equals(stack.get(DataComponents.ATTRIBUTE_MODIFIERS))) {
            stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attributes);
        }
    }

    public record TipProperties(int durability, int enchantability, Item repairItem, float attackDamage) {}
}
