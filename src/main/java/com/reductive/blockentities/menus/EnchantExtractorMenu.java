package com.reductive.blockentities.menus;

import com.reductive.registries.ReductiveMenuRegistry;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.state.BlockState;

public class EnchantExtractorMenu extends ItemCombinerMenu {
    private final DataSlot cost;

    public EnchantExtractorMenu(final int containerId, final Inventory inventory) {
        this(containerId, inventory, ContainerLevelAccess.NULL);
    }

    public EnchantExtractorMenu(final int containerId, final Inventory inventory, final ContainerLevelAccess access) {
        super(ReductiveMenuRegistry.ENCHANT_EXTRACTOR_MENU, containerId, inventory, access, createInputSlotDefinitions());
        this.cost = DataSlot.standalone();
        this.addDataSlot(this.cost);
    }

    private static ItemCombinerMenuSlotDefinition createInputSlotDefinitions() {
        return ItemCombinerMenuSlotDefinition.create()
                .withSlot(0, 49, 19, (itemStack) -> !itemStack.is(Items.BOOK) && !itemStack.is(Items.ENCHANTED_BOOK))
                .withSlot(1, 49, 40, (itemStack) -> itemStack.is(Items.BOOK))
                .withResultSlot(2, 129, 34)
                .build();
    }

    @Override
    protected boolean mayPickup(Player player, boolean hasItem) {
        return (player.isCreative() || player.experienceLevel >= this.cost.get()) && this.cost.get() > 0;
    }

    @Override
    protected void onTake(Player player, ItemStack carried) {
        if (!player.isCreative()) {
            player.giveExperienceLevels(-this.cost.get());
        }

        this.access.execute((level, pos) -> {
            level.playSound(null, pos, SoundEvents.GRINDSTONE_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
        });

        EnchantmentHelper.setEnchantments(this.inputSlots.getItem(0), ItemEnchantments.EMPTY);

        this.inputSlots.getItem(1).shrink(1);

        this.cost.set(0);
    }

    @Override
    protected boolean isValidBlock(BlockState state) {
        return true;
    }

    @Override
    public void createResult() {
        ItemStack input = this.inputSlots.getItem(0);
        ItemStack book = this.inputSlots.getItem(1);

        if (!input.isEmpty() && !book.isEmpty() && book.is(Items.BOOK)) {
            ItemEnchantments enchantments = EnchantmentHelper.getEnchantmentsForCrafting(input);

            if (!enchantments.isEmpty()) {
                ItemStack result = new ItemStack(Items.ENCHANTED_BOOK);
                EnchantmentHelper.setEnchantments(result, enchantments);

                int cost = 1;
                for (var entry : enchantments.entrySet()) {
                    cost += entry.getIntValue() * 2;
                }
                cost = Math.min(cost, 39);

                this.cost.set(cost);
                this.resultSlots.setItem(0, result);
                this.broadcastChanges();
                return;
            }
        }

        this.resultSlots.setItem(0, ItemStack.EMPTY);
        this.cost.set(0);
    }

    public int getCost() {
        return this.cost.get();
    }
}
