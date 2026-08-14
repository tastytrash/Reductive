package com.reductive;

import net.minecraft.references.BlockItemId;
import net.minecraft.resources.Identifier;

public class ReductiveBlockItemIds {
    public static final BlockItemId EXPERIENCE_TANK = ReductiveBlockItemIds.create("experience_tank");
    public static final BlockItemId MATERIAL_RECYCLER = ReductiveBlockItemIds.create("material_recycler");

    private static BlockItemId create(String name) {
        Identifier id = Identifier.fromNamespaceAndPath(Reductive.MOD_ID, name);
        return BlockItemId.create(id, id);
    }
}
