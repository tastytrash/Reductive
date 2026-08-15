package com.reductive.items;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.Optional;

public class PortableCrafterItem extends Item {
    private static final Component CONTAINER_TITLE = Component.translatable("container.crafting");

    public PortableCrafterItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(final Level level, final Player player, final InteractionHand hand) {
        if (!level.isClientSide()) {
            player.openMenu(new SimpleMenuProvider(
                    (containerId, playerInventory, player1) -> new CustomPortableCraftingMenu(containerId, playerInventory),
                    CONTAINER_TITLE
            ));
            player.awardStat(Stats.INTERACT_WITH_CRAFTING_TABLE);
        }

        return InteractionResult.SUCCESS;
    }

    private static class CustomPortableCraftingMenu extends CraftingMenu {
        private final Player player;

        public CustomPortableCraftingMenu(int containerId, Inventory playerInventory) {
            super(containerId, playerInventory, ContainerLevelAccess.NULL);
            this.player = playerInventory.player;
        }

        @Override
        public boolean stillValid(Player player) {
            return true;
        }

        @Override
        public void slotsChanged(net.minecraft.world.Container container) {
            Level level = this.player.level();
            if (!level.isClientSide()) {
                ServerPlayer serverPlayer = (ServerPlayer) this.player;
                ItemStack itemStack = ItemStack.EMPTY;

                CraftingInput craftingInput = this.craftSlots.asCraftInput();
                Optional<RecipeHolder<net.minecraft.world.item.crafting.CraftingRecipe>> optional = level.getServer()
                        .getRecipeManager()
                        .getRecipeFor(RecipeType.CRAFTING, craftingInput, level);

                if (optional.isPresent()) {
                    RecipeHolder<net.minecraft.world.item.crafting.CraftingRecipe> recipeHolder = optional.get();
                    ResultContainer resultContainer = this.resultSlots;

                    if (resultContainer.setRecipeUsed(serverPlayer, recipeHolder)) {
                        itemStack = recipeHolder.value().assemble(craftingInput);
                    }
                }

                this.resultSlots.setItem(0, itemStack);
                this.setRemoteSlot(0, itemStack);
                serverPlayer.connection.send(new net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket(this.containerId, this.incrementStateId(), 0, itemStack));
            }
        }

        @Override
        public void removed(Player player) {
            super.removed(player);
            this.clearContainer(player, this.craftSlots);
        }
    }
}
