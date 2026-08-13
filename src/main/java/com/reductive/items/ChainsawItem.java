package com.reductive.items;

import com.reductive.Reductive;
import com.reductive.datagen.ReductiveComponents;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.Enchantable;
import net.minecraft.world.item.enchantment.Repairable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

public class ChainsawItem extends Item {
    private final Item bodyType;

    public ChainsawItem(Properties settings, Item bodyType) {
        super(settings);
        this.bodyType = bodyType;
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level world, BlockState state, BlockPos pos, LivingEntity miner) {
        if (stack.nextDamageWillBreak()) {
            ItemStack bodyOnly = new ItemStack(bodyType);
            stack.shrink(1);
            miner.handleExtraItemsCreatedOnUse(bodyOnly);
            world.playSound(null, miner.getX(), miner.getY(), miner.getZ(), SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 1.0f, 1.0f);}


        return super.mineBlock(stack, world, state, pos, miner);
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        String blade = stack.get(ReductiveComponents.BLADE_TYPE);
        if (blade == null) return 1.0f;

        boolean isAxeBlock = state.is(BlockTags.MINEABLE_WITH_AXE);
        boolean isHoeBlock = state.is(BlockTags.MINEABLE_WITH_HOE);

        if (isAxeBlock || isHoeBlock) {
            return switch (blade) {
                case "iron" -> 14.0f;
                case "gold" -> 40.0f;
                case "diamond" -> 22.0f;
                case "netherite" -> 34.0f;
                default -> 1.0f;
            };
        }

        return 1.0f;
    }

    public static void applyBladeProperties(ItemStack stack) {
        String blade = stack.get(ReductiveComponents.BLADE_TYPE);
        if (blade == null) return;

        BladeProperties properties = switch (blade) {
            case "iron" -> new BladeProperties(512, 14, Items.IRON_INGOT, 5.0F);
            case "gold" -> new BladeProperties(192, 22, Items.GOLD_INGOT, 4.0F);
            case "diamond" -> new BladeProperties(2304, 10, Items.DIAMOND, 6.0F);
            case "netherite" -> new BladeProperties(3456, 15, Items.NETHERITE_INGOT, 7.0F);
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
                                Identifier.fromNamespaceAndPath(Reductive.MOD_ID, "chainsaw_blade_damage"),
                                properties.attackDamage(),
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        EquipmentSlotGroup.MAINHAND
                )
                .add(
                        Attributes.ATTACK_SPEED,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath(Reductive.MOD_ID, "chainsaw_attack_speed"),
                                -2.8F,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        EquipmentSlotGroup.MAINHAND
                )
                .build();

        if (!attributes.equals(stack.get(DataComponents.ATTRIBUTE_MODIFIERS))) {
            stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attributes);
        }
    }

    private record BladeProperties(int durability, int enchantability, Item repairItem, float attackDamage) {}

    @Override
    public void onCraftedPostProcess(ItemStack stack, Level world) {
        applyBladeProperties(stack);
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel world, Entity entity, @Nullable EquipmentSlot slot) {
        applyBladeProperties(stack);
    }
}
