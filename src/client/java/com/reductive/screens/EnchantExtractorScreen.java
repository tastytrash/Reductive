package com.reductive.screens;

import com.reductive.Reductive;
import com.reductive.blockentities.menus.EnchantExtractorMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.ItemCombinerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class EnchantExtractorScreen extends ItemCombinerScreen<EnchantExtractorMenu> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(Reductive.MOD_ID,"textures/gui/container/enchant_extractor.png");
    private static final Component TOO_EXPENSIVE_TEXT = Component.translatable("container.repair.expensive");

    public EnchantExtractorScreen(EnchantExtractorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, TEXTURE);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int xm, int ym) {
        super.extractLabels(graphics, xm, ym);
        int cost = this.menu.getCost();
        if (cost > 0) {
            int color = -8323296;
            Component line;
            if (cost >= 40 && !this.minecraft.player.isCreative()) {
                line = TOO_EXPENSIVE_TEXT;
                color = -40864;
            } else if (!this.menu.getSlot(2).hasItem()) {
                line = null;
            } else {
                line = Component.translatable("container.repair.cost", cost);
                if (!this.menu.getSlot(2).mayPickup(this.minecraft.player)) {
                    color = -40864;
                }
            }

            if (line != null) {
                int tx = this.imageWidth - 8 - this.font.width(line) - 2;
                graphics.fill(tx - 2, 67, this.imageWidth - 8, 79, 1325400064);
                graphics.text(this.font, line, tx, 69, color);
            }
        }
    }

    @Override
    protected void extractErrorIcon(GuiGraphicsExtractor graphics, int xo, int yo) {

    }
}
