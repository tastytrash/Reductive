package com.reductive.screens;

import com.reductive.blockentities.menus.MaterialRecyclerMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.util.Mth;

public class MaterialRecyclerScreen extends AbstractContainerScreen<MaterialRecyclerMenu> {
    private static final Identifier CONTAINER_TEXTURE = Identifier.fromNamespaceAndPath("reductive", "textures/gui/container/material_recycler.png");
    private static final Identifier LIT_PROGRESS_SPRITE = Identifier.withDefaultNamespace("container/furnace/lit_progress");
    private static final Identifier RECYCLE_PROGRESS_SPRITE = Identifier.withDefaultNamespace("container/furnace/burn_progress");

    public MaterialRecyclerScreen(MaterialRecyclerMenu abstractContainerMenu, Inventory inventory, Component component) {
        super(abstractContainerMenu, inventory, component);
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractBackground(graphics, mouseX, mouseY, delta);
        graphics.blit(RenderPipelines.GUI_TEXTURED, CONTAINER_TEXTURE, this.leftPos, this.topPos, 0.0F, 0.0F, this.imageWidth, this.imageHeight, BACKGROUND_TEXTURE_WIDTH, BACKGROUND_TEXTURE_HEIGHT);

        int litProgressHeight = Mth.ceil(this.menu.getLitProgress() * 13.0F) + 1;
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, LIT_PROGRESS_SPRITE, 14, 14, 0, 14 - litProgressHeight, this.leftPos + 11, this.topPos + 35 + 14 - litProgressHeight, 14, litProgressHeight);

        int burnProgressWidth = Mth.ceil(this.menu.getRecycleProgress() * 24.0F / this.menu.getRecycleTotalTime());
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, RECYCLE_PROGRESS_SPRITE, 24, 16, 0, 0, this.leftPos + 94, this.topPos + 34, burnProgressWidth, 16);
    }
}
