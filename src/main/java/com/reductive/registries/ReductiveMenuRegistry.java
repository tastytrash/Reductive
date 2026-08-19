package com.reductive.registries;

import com.reductive.Reductive;
import com.reductive.blockentities.menus.MaterialRecyclerMenu;
import com.reductive.blockentities.menus.EnchantExtractorMenu;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

public class ReductiveMenuRegistry {
    public static final MenuType<MaterialRecyclerMenu> MATERIAL_RECYCLER_MENU =
            register("material_recycler", MaterialRecyclerMenu::new);

    public static final MenuType<EnchantExtractorMenu> ENCHANT_EXTRACTOR_MENU =
            register("enchant_extractor", EnchantExtractorMenu::new);

    public static <T extends AbstractContainerMenu> MenuType<T> register(String name, MenuType.MenuSupplier<T> constructor) {
        return Registry.register(BuiltInRegistries.MENU, Identifier.fromNamespaceAndPath(Reductive.MOD_ID, name), new MenuType<>(constructor, FeatureFlagSet.of()));
    }


    public static void initialize() {}
}
