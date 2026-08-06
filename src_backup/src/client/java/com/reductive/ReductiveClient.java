package com.reductive;

import com.reductive.datagen.ReductiveComponents;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.text.WordUtils;

public class ReductiveClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
        ItemTooltipCallback.EVENT.register((itemStack, tooltipContext, tooltipType, list) -> {
            if (!itemStack.isOf(ReductiveItemRegistry.DRILL_BASIC) && !itemStack.isOf(ReductiveItemRegistry.DRILL_INDUSTRIAL)) return;

            if (itemStack.getComponents().get(ReductiveComponents.TIP_TYPE) != null) {
                String tip = itemStack.getComponents().get(ReductiveComponents.TIP_TYPE);

                assert tip != null;
                Formatting formatting = switch (tip) {
                    case "iron" -> Formatting.WHITE;
                    case "gold" -> Formatting.GOLD;
                    case "diamond" -> Formatting.AQUA;
                    case "netherite" -> Formatting.GRAY;
                    default -> Formatting.RED;
                };
                list.add(Text.literal("Tip: " + WordUtils.capitalize(tip)).formatted(formatting));
            }
        });
    }
}