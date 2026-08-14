package com.reductive.blockentities.menus;

import com.reductive.ReductiveMenuRegistry;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.Container;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.Map;

public class MaterialRecyclerMenu extends AbstractContainerMenu {
    private static final int FUEL_SLOT = 0;
    private static final int INPUT_START = 1;
    private static final int INPUT_END = 10;
    private static final int OUTPUT_START = 10;
    private static final int OUTPUT_END = 14;
    private static final int INVENTORY_START = 14;
    private static final int INVENTORY_END = INVENTORY_START + Inventory.INVENTORY_SIZE;

    private static final int FUEL_SLOT_X = 11;
    private static final int FUEL_SLOT_Y = 53;
    private static final int INPUT_START_X = 32;
    private static final int INPUT_START_Y = 17;
    private static final int OUTPUT_SLOT_X = 128;
    private static final int OUTPUT_SLOT_Y = 26;
    private static final int INVENTORY_START_X = 8;
    private static final int INVENTORY_START_Y = 84;

    private final Container container;
    private final ContainerData data;

    public MaterialRecyclerMenu(final int containerId, final Inventory inventory, final Container container, final ContainerData data) {
        super(ReductiveMenuRegistry.MATERIAL_RECYCLER_MENU, containerId);
        this.container = container;
        this.data = data;
        checkContainerSize(this.container, 14);
        checkContainerDataCount(this.data, 4);

        this.container.startOpen(inventory.player);

        // fuel slot
        this.addSlot(new FuelSlot(this.container, FUEL_SLOT, FUEL_SLOT_X, FUEL_SLOT_Y, inventory));

        // 3x3 input grid
        this.addInputGridSlots();

        // 2x2 output grid
        this.addSlot(new OutputSlot(this.container, OUTPUT_START, OUTPUT_SLOT_X, OUTPUT_SLOT_Y));
        this.addSlot(new OutputSlot(this.container, OUTPUT_START + 1, OUTPUT_SLOT_X + SLOT_SIZE, OUTPUT_SLOT_Y));
        this.addSlot(new OutputSlot(this.container, OUTPUT_START + 2, OUTPUT_SLOT_X, OUTPUT_SLOT_Y + SLOT_SIZE));
        this.addSlot(new OutputSlot(this.container, OUTPUT_START + 3, OUTPUT_SLOT_X + SLOT_SIZE, OUTPUT_SLOT_Y + SLOT_SIZE));

        // player inventory slots
        this.addStandardInventorySlots(inventory, INVENTORY_START_X, INVENTORY_START_Y);

        this.addDataSlots(this.data);
    }

    public MaterialRecyclerMenu(final int containerId, final Inventory inventory) {
        this(containerId, inventory, new SimpleContainer(14), new SimpleContainerData(4));
    }

    private void addInputGridSlots() {
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                final int slot = INPUT_START + x + y * 3;
                this.addSlot(new InputSlot(
                        this.container,
                        slot,
                        INPUT_START_X + x * SLOT_SIZE,
                        INPUT_START_Y + y * SLOT_SIZE
                ));
            }
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        Slot slot = this.slots.get(slotIndex);

        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        ItemStack clicked = stack.copy();

        if (slotIndex == FUEL_SLOT) {
            // fuel slot to player inventory or input grid
            if (!this.moveItemStackTo(stack, INVENTORY_START, INVENTORY_END, true)) {
                return ItemStack.EMPTY;
            }
        } else if (slotIndex >= INPUT_START && slotIndex < INPUT_END) {
            // input slot to output slots, fuel slot, or player inventory
            if (!this.moveItemStackTo(stack, OUTPUT_START, OUTPUT_END, false)) {
                if (!this.moveItemStackTo(stack, FUEL_SLOT, FUEL_SLOT + 1, false)) {
                    if (!this.moveItemStackTo(stack, INVENTORY_START, INVENTORY_END, true)) {
                        return ItemStack.EMPTY;
                    }
                }
            }
        } else if (slotIndex >= OUTPUT_START && slotIndex < OUTPUT_END) {
            // output slot to player inventory
            if (!this.moveItemStackTo(stack, INVENTORY_START, INVENTORY_END, true)) {
                return ItemStack.EMPTY;
            }
        } else if (slotIndex >= INVENTORY_START && slotIndex < INVENTORY_END) {
            // player inventory to fuel slot or input
            if (!this.moveItemStackTo(stack, FUEL_SLOT, FUEL_SLOT + 1, false)) {
                if (!this.moveItemStackTo(stack, INPUT_START, INPUT_END, false)) {
                    return ItemStack.EMPTY;
                }
            }
        }

        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        return clicked;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.container.stillValid(player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.container.stopOpen(player);
    }

    private static class FuelSlot extends Slot {
        private final Inventory inventory;

        public FuelSlot(Container container, int slot, int x, int y, Inventory inventory) {
            super(container, slot, x, y);
            this.inventory = inventory;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return inventory.player.level().fuelValues().isFuel(stack);
        }
    }

    private static class OutputSlot extends Slot {
        public OutputSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }
    }

    private static class InputSlot extends Slot {
        public InputSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return stack.isDamageableItem();
        }
    }

    public int getLitTimeRemaining() {
        return this.data.get(0);
    }

    public int getLitTotalTime() {
        return this.data.get(1);
    }

    public int getRecycleProgress() {
        return this.data.get(2);
    }

    public int getRecycleTotalTime() {
        return this.data.get(3);
    }

    public boolean isLit() {
        return this.getLitTimeRemaining() > 0;
    }

    public float getLitProgress() {
        int total = this.getLitTotalTime();

        if (total <= 0) {
            return 0.0F;
        }

        return (float) this.getLitTimeRemaining() / (float) total;
    }
}
