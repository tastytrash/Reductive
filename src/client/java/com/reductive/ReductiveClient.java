package com.reductive;

import com.reductive.datagen.ReductiveComponents;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

public class ReductiveClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
        ItemTooltipCallback.EVENT.register((itemStack, tooltipContext, tooltipType, list) -> {
            if (!itemStack.is(ReductiveItemRegistry.DRILL_BASIC) && !itemStack.is(ReductiveItemRegistry.DRILL_INDUSTRIAL)) return;

            if (itemStack.getComponents().get(ReductiveComponents.TIP_TYPE) != null) {
                String tip = itemStack.getComponents().get(ReductiveComponents.TIP_TYPE);

                assert tip != null;
                ChatFormatting formatting = switch (tip) {
                    case "iron" -> ChatFormatting.WHITE;
                    case "gold" -> ChatFormatting.GOLD;
                    case "diamond" -> ChatFormatting.AQUA;
                    case "netherite" -> ChatFormatting.GRAY;
                    default -> ChatFormatting.RED;
                };

                String capitalizedTip = tip.substring(0, 1).toUpperCase() + tip.substring(1);
                list.add(Component.literal("Tip: " + capitalizedTip).withStyle(formatting));

            }
        });
    }
}