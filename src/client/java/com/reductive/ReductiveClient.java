package com.reductive;

import com.reductive.datagen.ReductiveComponents;

import com.reductive.items.GarbageBundleItem;
import com.reductive.registries.ReductiveItemRegistry;
import com.reductive.registries.ReductiveMenuRegistry;
import com.reductive.screens.MaterialRecyclerScreen;
import com.reductive.screens.EnchantExtractorScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;

public class ReductiveClient implements ClientModInitializer {
	private EngineSound engineSound;

	@Override
	public void onInitializeClient() {
        MenuScreens.register(ReductiveMenuRegistry.MATERIAL_RECYCLER_MENU, MaterialRecyclerScreen::new);
        MenuScreens.register(ReductiveMenuRegistry.ENCHANT_EXTRACTOR_MENU, EnchantExtractorScreen::new);

        ItemTooltipCallback.EVENT.register((itemStack, tooltipContext, tooltipType, list) -> {
            if (itemStack.getItem() instanceof GarbageBundleItem) {
                BundleContents contents = itemStack.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);

                // Ensure the list has at least a title before trying to insert directly below it
                if (!list.isEmpty()) {
                    if (contents.items().isEmpty()) {
                        // Inserting at index 1 places the text right under the "Garbage Bundle" item name
                        list.add(1, Component.translatable("item.reductive.garbage_bundle.empty.description")
                                .withStyle(ChatFormatting.GRAY));
                    } else {
                        // Optional: Places a status line right under the title when full
                        list.add(1, Component.translatable("item.reductive.garbage_bundle.full.description").withStyle(ChatFormatting.RED));
                        list.add(2, Component.translatable("item.reductive.garbage_bundle.full.description2").withStyle(ChatFormatting.RED));
                    }
                }
                return; // Exit early
            }

            boolean isDrill = itemStack.is(ReductiveItemRegistry.DRILL_BASIC)
                    || itemStack.is(ReductiveItemRegistry.DRILL_INDUSTRIAL);

            boolean isChainsaw = itemStack.is(ReductiveItemRegistry.CHAINSAW_BASIC)
                    || itemStack.is(ReductiveItemRegistry.CHAINSAW_INDUSTRIAL);

            if (!isDrill && !isChainsaw) return;

            for (int i = 0; i < list.size(); i++) {
                Component line = list.get(i);
                String text = line.getString();

                 if (text.contains("Attack Damage")) {
                    String numStr = text.replaceAll("[^0-9.]", "");
                     try {
                        double baseVal = Double.parseDouble(numStr);
                        double finalDmg = baseVal + 1;
                        String formattedDmg = finalDmg == (int) finalDmg ? String.valueOf((int) finalDmg) : String.valueOf(finalDmg);

                        list.set(i, Component.literal(" " + formattedDmg + " Attack Damage").withStyle(ChatFormatting.DARK_GREEN));
                    } catch (Exception _) {}
                }

                else if (text.contains("Attack Speed")) {
                    String numStr = text.replaceAll("[^0-9.]", "");
                     try {
                        double baseVal = Double.parseDouble(numStr);
                        double finalSpeed = 4.0 - baseVal;
                        String formattedSpeed = finalSpeed == (int) finalSpeed ? String.valueOf((int) finalSpeed) : String.format("%.1f", finalSpeed);

                        list.set(i, Component.literal(" " + formattedSpeed + " Attack Speed").withStyle(ChatFormatting.DARK_GREEN));
                    } catch (Exception _) {}
                }
            }

            String material = isDrill ? itemStack.get(ReductiveComponents.TIP_TYPE) : itemStack.get(ReductiveComponents.BLADE_TYPE);

            if (material == null) return;

            ChatFormatting formatting = switch (material) {
                case "iron" -> ChatFormatting.WHITE;
                case "gold" -> ChatFormatting.GOLD;
                case "diamond" -> ChatFormatting.AQUA;
                case "netherite" -> ChatFormatting.GRAY;
                default -> ChatFormatting.RED;
            };

            String capitalized = material.substring(0, 1).toUpperCase() + material.substring(1);
            String label = isDrill ? "Tip" : "Blade";

            list.add(1, Component.literal(label + ": " + capitalized).withStyle(formatting));

            // chainsaw max blocks
            if (itemStack.is(ReductiveItemRegistry.CHAINSAW_INDUSTRIAL)) {
                int maxBlocks = switch (material) {
                    case "iron" -> 7;
                    case "gold" -> 3;
                    case "diamond" -> 11;
                    case "netherite" -> 15;
                    default -> 0;
                };

                list.add(2, Component.literal("Breaks " + maxBlocks + " blocks")
                        .withStyle(ChatFormatting.DARK_GRAY).withStyle(ChatFormatting.ITALIC));
            }
        });


        // tool engine (bee) sound
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            boolean shouldPlay =
                    client.player != null
                    && client.options.keyAttack.isDown()
                    && isEngineTool(client.player.getMainHandItem());

            if (shouldPlay) {
                if (engineSound == null || engineSound.isStopped()) {
                    engineSound = new EngineSound(client.player);
                    client.getSoundManager().play(engineSound);
                }
            } else if (engineSound != null) {
                engineSound.stopLoop();
                engineSound = null;
            }
        });
    }

    private static boolean isEngineTool(ItemStack stack) {
        return stack.is(ReductiveItemRegistry.CHAINSAW_BASIC)
                || stack.is(ReductiveItemRegistry.CHAINSAW_INDUSTRIAL)
                || stack.is(ReductiveItemRegistry.DRILL_BASIC)
                || stack.is(ReductiveItemRegistry.DRILL_INDUSTRIAL);
    }

    private static class EngineSound extends AbstractTickableSoundInstance {
        private final LocalPlayer player;

        private EngineSound(LocalPlayer player) {
            super(SoundEvents.BEE_LOOP, SoundSource.PLAYERS, RandomSource.create(0));
            this.player = player;
            this.looping = true;
            this.relative = true;
            this.volume = 0.65F;
            this.pitch = 0.65F;
        }

        @Override
        public void tick() {
            Minecraft client = Minecraft.getInstance();
            if (!client.options.keyAttack.isDown() || !isEngineTool(player.getMainHandItem())) {
                stop();
            }
        }

        private void stopLoop() {
            stop();
        }
    }
}
