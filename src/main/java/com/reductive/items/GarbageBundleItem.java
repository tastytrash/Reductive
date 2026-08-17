package com.reductive.items;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.BundleTooltip;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.List;
import java.util.Optional;

public class GarbageBundleItem extends BundleItem {
    public GarbageBundleItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isBarVisible(final ItemStack stack) {
        return false;
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        BundleContents contents = stack.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);

        if (contents.items().isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(new BundleTooltip(contents));
    }


    @Override
    public boolean overrideStackedOnOther(final ItemStack self, final Slot slot, final ClickAction clickAction, final Player player) {
        if (clickAction == ClickAction.PRIMARY && !slot.getItem().isEmpty()) {
            ItemStack remaining = garbageDiscard(self, slot.getItem());
            slot.set(remaining);
            this.syncAndPlaySound(player, remaining, slot.getItem());
            return true;
        }
        return super.overrideStackedOnOther(self, slot, clickAction, player);
    }

    @Override
    public boolean overrideOtherStackedOnMe(final ItemStack self, final ItemStack other, final Slot slot, final ClickAction clickAction, final Player player, final SlotAccess carriedItem) {
        if (clickAction == ClickAction.PRIMARY && !other.isEmpty() && slot.allowModification(player)) {
            ItemStack remaining = garbageDiscard(self, other);
            carriedItem.set(remaining);
            this.syncAndPlaySound(player, remaining, other);
            return true;
        }
        return super.overrideOtherStackedOnMe(self, other, slot, clickAction, player, carriedItem);
    }


    private ItemStack garbageDiscard(ItemStack bundle, ItemStack discardedItem) {
        BundleContents initialContents = bundle.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
        BundleContents.Mutable contents = new BundleContents.Mutable(initialContents);

        if (!contents.toImmutable().items().isEmpty()) {
            contents.removeOne();
        }

        contents.tryInsert(discardedItem);
        bundle.set(DataComponents.BUNDLE_CONTENTS, contents.toImmutable());

        return discardedItem;
    }

    private void syncAndPlaySound(Player player, ItemStack remaining, ItemStack original) {
        if (remaining.getCount() < original.getCount()) {
            playInsertSound(player);
        }
        AbstractContainerMenu menu = player.containerMenu;
        menu.broadcastChanges();
    }

    private static void playInsertSound(final Entity entity) {
        entity.playSound(SoundEvents.BUNDLE_INSERT, 0.8F, 0.8F + entity.level().getRandom().nextFloat() * 0.4F);
    }
}
