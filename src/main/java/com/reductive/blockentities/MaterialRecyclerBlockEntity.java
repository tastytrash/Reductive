package com.reductive.blockentities;

import com.reductive.ReductiveBlockEntityRegistry;
import com.reductive.ReductiveItemRegistry;
import com.reductive.blockentities.menus.MaterialRecyclerMenu;
import com.reductive.blocks.MaterialRecyclerBlock;
import com.reductive.helpers.ImplementedContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MaterialRecyclerBlockEntity extends BlockEntity implements ImplementedContainer, MenuProvider {
    private static final int FUEL_SLOT = 0;
    private static final int INPUT_START = 1;
    private static final int INPUT_END = 10;
    private static final int OUTPUT_START = 10;
    private static final int OUTPUT_END = 14;

    private final NonNullList<ItemStack> items = NonNullList.withSize(14, ItemStack.EMPTY);
    private int litTimeRemaining;
    private int litTotalTime;
    private int cookingTimer;
    private int cookingTotalTime = 100;

    protected final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int id) {
            return switch (id) {
                case 0 -> litTimeRemaining;
                case 1 -> litTotalTime;
                case 2 -> cookingTimer;
                case 3 -> cookingTotalTime;
                default -> 0;
            };
        }

        @Override
        public void set(int id, int value) {
            switch (id) {
                case 0 -> litTimeRemaining = value;
                case 1 -> litTotalTime = value;
                case 2 -> cookingTimer = value;
                case 3 -> cookingTotalTime = value;
            }
        }

        @Override
        public int getCount() {
            return 4;
        }
    };

    public MaterialRecyclerBlockEntity(BlockPos pos, BlockState state) {
        super(ReductiveBlockEntityRegistry.MATERIAL_RECYCLER_BLOCK_ENTITY, pos, state);
    }

    @Override public NonNullList<ItemStack> getItems() { return this.items; }
    @Override public boolean stillValid(Player player) { return Container.stillValidBlockEntity(this, player); }
    @Override public @NonNull Component getDisplayName() { return Component.translatable("block.reductive.material_recycler"); }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new MaterialRecyclerMenu(containerId, inventory, this, this.dataAccess);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        ContainerHelper.loadAllItems(input, this.items);
        this.litTimeRemaining = input.getIntOr("LitTimeRemaining", 0);
        this.litTotalTime = input.getIntOr("LitTotalTime", 0);
        this.cookingTimer = input.getIntOr("CookingTimer", 0);
        this.cookingTotalTime = input.getIntOr("CookingTotalTime", 100);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, this.items);
        output.putInt("LitTimeRemaining", this.litTimeRemaining);
        output.putInt("LitTotalTime", this.litTotalTime);
        output.putInt("CookingTimer", this.cookingTimer);
        output.putInt("CookingTotalTime", this.cookingTotalTime);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, MaterialRecyclerBlockEntity entity) {
        boolean wasLit = entity.litTimeRemaining > 0;
        if (wasLit) entity.litTimeRemaining--;

        ItemStack fuel = entity.items.get(FUEL_SLOT);
        boolean canRecycle = entity.canRecycle();
        boolean isLit = entity.litTimeRemaining > 0;
        boolean changed = false;

        if (isLit || (!fuel.isEmpty() && canRecycle)) {
            if (canRecycle && !isLit) {
                int burnDuration = level.fuelValues().burnDuration(fuel);
                entity.litTimeRemaining = burnDuration;
                entity.litTotalTime = burnDuration;
                if (burnDuration > 0) {
                    fuel.shrink(1);
                    isLit = true;
                    changed = true;
                }
            }

            if (isLit && canRecycle) {
                entity.cookingTimer++;
                if (entity.cookingTimer >= entity.cookingTotalTime) {
                    entity.cookingTimer = 0;
                    entity.cookingTotalTime = 200;
                    entity.recycleNextIngredient();
                    changed = true;
                }
            } else {
                entity.cookingTimer = 0;
            }
        } else if (entity.cookingTimer > 0) {
            entity.cookingTimer = Mth.clamp(entity.cookingTimer - 2, 0, entity.cookingTotalTime);
        }

        if (wasLit != isLit) {
            state = state.setValue(MaterialRecyclerBlock.LIT, isLit);
            level.setBlock(pos, state, Block.UPDATE_ALL);
            changed = true;
        }

        if (changed) {
            setChanged(level, pos, state);
        }
    }

    private boolean canRecycle() {
        return findRecycleItem() != null;
    }

    private RecycleCandidate findRecycleItem() {
        for (int i = INPUT_START; i < INPUT_END; i++) {
            ItemStack ingredient = this.items.get(i);
            if (!ingredient.isEmpty() && RECYCLE_MAP.containsKey(ingredient.getItem())) {
                List<ItemStack> outputs = getItemsFromDurability(ingredient);
                if (tryOutput(outputs, false)) {
                    return new RecycleCandidate(ingredient, outputs);
                }
            }
        }
        return null;
    }

    private void recycleNextIngredient() {
        RecycleCandidate candidate = findRecycleItem();
        if (candidate != null && tryOutput(candidate.outputs, true)) {
            candidate.ingredient.shrink(1);
        }
    }

    private boolean tryOutput(List<ItemStack> outputs, boolean commit) {
        SimpleContainer outputSlots = new SimpleContainer(OUTPUT_END - OUTPUT_START);
        for (int i = 0; i < outputSlots.getContainerSize(); i++) {
            outputSlots.setItem(i, this.items.get(OUTPUT_START + i).copy());
        }

        for (ItemStack output : outputs) {
            if (!outputSlots.addItem(output).isEmpty()) return false;
        }

        if (commit) {
            for (int i = 0; i < outputSlots.getContainerSize(); i++) {
                this.items.set(OUTPUT_START + i, outputSlots.getItem(i));
            }
        }
        return true;
    }

    private record RecycleCandidate(ItemStack ingredient, List<ItemStack> outputs) {}
    private record ItemConversion(Item targetItem, int ratio) {}

    private static List<ItemStack> getItemsFromDurability(ItemStack stack) {
        float durabilityPercent = 1.0f - ((float) stack.getDamageValue() / stack.getMaxDamage());
        List<ItemStack> result = new ArrayList<>();
        List<ItemStack> recipeItems = RECYCLE_MAP.get(stack.getItem());

        if (recipeItems == null) return result;

        for (ItemStack item : recipeItems) {
            ItemConversion conversion = CONVERSION_MAP.get(item.getItem());
            if (conversion != null) {
                int pieces = (int) (item.getCount() * durabilityPercent * conversion.ratio);
                int fullCount = pieces / conversion.ratio;
                int remainder = pieces % conversion.ratio;
                if (fullCount > 0) result.add(new ItemStack(item.getItem(), fullCount));
                if (remainder > 0) result.add(new ItemStack(conversion.targetItem, remainder));
            } else {
                int count = (int) (item.getCount() * durabilityPercent);
                if (count > 0) result.add(item.copyWithCount(count));
            }
        }
        return result;
    }

    private static final Map<Item, ItemConversion> CONVERSION_MAP = new HashMap<>();
    static {
        CONVERSION_MAP.put(Items.COBBLESTONE, new ItemConversion(ReductiveItemRegistry.PEBBLE, 4));
        CONVERSION_MAP.put(Items.COAL, new ItemConversion(ReductiveItemRegistry.COAL_CHUNK, 9));
        CONVERSION_MAP.put(Items.LAPIS_LAZULI, new ItemConversion(ReductiveItemRegistry.LAPIS_FRAGMENT, 9));
        CONVERSION_MAP.put(Items.EMERALD, new ItemConversion(ReductiveItemRegistry.EMERALD_JEWEL, 9));
        CONVERSION_MAP.put(Items.QUARTZ, new ItemConversion(ReductiveItemRegistry.QUARTZ_SHARD, 4));
        CONVERSION_MAP.put(Items.DIAMOND, new ItemConversion(ReductiveItemRegistry.DIAMOND_SHARD, 9));
        CONVERSION_MAP.put(Items.NETHERITE_INGOT, new ItemConversion(ReductiveItemRegistry.NETHERITE_NUGGET, 9));
        CONVERSION_MAP.put(Items.NETHER_STAR, new ItemConversion(ReductiveItemRegistry.NETHER_STAR_FRAGMENT, 8));
        CONVERSION_MAP.put(Items.IRON_INGOT, new ItemConversion(Items.IRON_NUGGET, 9));
        CONVERSION_MAP.put(Items.GOLD_INGOT, new ItemConversion(Items.GOLD_NUGGET, 9));
        CONVERSION_MAP.put(Items.COPPER_INGOT, new ItemConversion(Items.COPPER_NUGGET, 9));
    }

    private static final Map<Item, List<ItemStack>> RECYCLE_MAP = new HashMap<>();
    static {
        RECYCLE_MAP.put(Items.WOODEN_PICKAXE, List.of(new ItemStack(Items.OAK_PLANKS, 3), new ItemStack(Items.STICK, 2)));
        RECYCLE_MAP.put(Items.WOODEN_AXE, List.of(new ItemStack(Items.OAK_PLANKS, 3), new ItemStack(Items.STICK, 2)));
        RECYCLE_MAP.put(Items.WOODEN_SHOVEL, List.of(new ItemStack(Items.OAK_PLANKS, 1), new ItemStack(Items.STICK, 2)));
        RECYCLE_MAP.put(Items.WOODEN_HOE, List.of(new ItemStack(Items.OAK_PLANKS, 2), new ItemStack(Items.STICK, 2)));
        RECYCLE_MAP.put(Items.WOODEN_SWORD, List.of(new ItemStack(Items.OAK_PLANKS, 2), new ItemStack(Items.STICK, 1)));
        RECYCLE_MAP.put(Items.WOODEN_SPEAR, List.of(new ItemStack(Items.OAK_PLANKS, 1), new ItemStack(Items.STICK, 2)));

        RECYCLE_MAP.put(Items.STONE_PICKAXE, List.of(new ItemStack(Items.COBBLESTONE, 3), new ItemStack(Items.STICK, 2)));
        RECYCLE_MAP.put(Items.STONE_AXE, List.of(new ItemStack(Items.COBBLESTONE, 3), new ItemStack(Items.STICK, 2)));
        RECYCLE_MAP.put(Items.STONE_SHOVEL, List.of(new ItemStack(Items.COBBLESTONE, 1), new ItemStack(Items.STICK, 2)));
        RECYCLE_MAP.put(Items.STONE_HOE, List.of(new ItemStack(Items.COBBLESTONE, 2), new ItemStack(Items.STICK, 2)));
        RECYCLE_MAP.put(Items.STONE_SWORD, List.of(new ItemStack(Items.COBBLESTONE, 2), new ItemStack(Items.STICK, 1)));
        RECYCLE_MAP.put(Items.STONE_SPEAR, List.of(new ItemStack(Items.COBBLESTONE, 1), new ItemStack(Items.STICK, 2)));

        RECYCLE_MAP.put(Items.IRON_PICKAXE, List.of(new ItemStack(Items.IRON_INGOT, 3), new ItemStack(Items.STICK, 2)));
        RECYCLE_MAP.put(Items.IRON_AXE, List.of(new ItemStack(Items.IRON_INGOT, 3), new ItemStack(Items.STICK, 2)));
        RECYCLE_MAP.put(Items.IRON_SHOVEL, List.of(new ItemStack(Items.IRON_INGOT, 1), new ItemStack(Items.STICK, 2)));
        RECYCLE_MAP.put(Items.IRON_HOE, List.of(new ItemStack(Items.IRON_INGOT, 2), new ItemStack(Items.STICK, 2)));
        RECYCLE_MAP.put(Items.IRON_SWORD, List.of(new ItemStack(Items.IRON_INGOT, 2), new ItemStack(Items.STICK, 1)));
        RECYCLE_MAP.put(Items.IRON_SPEAR, List.of(new ItemStack(Items.IRON_INGOT, 1), new ItemStack(Items.STICK, 2)));

        RECYCLE_MAP.put(Items.GOLDEN_PICKAXE, List.of(new ItemStack(Items.GOLD_INGOT, 3), new ItemStack(Items.STICK, 2)));
        RECYCLE_MAP.put(Items.GOLDEN_AXE, List.of(new ItemStack(Items.GOLD_INGOT, 3), new ItemStack(Items.STICK, 2)));
        RECYCLE_MAP.put(Items.GOLDEN_SHOVEL, List.of(new ItemStack(Items.GOLD_INGOT, 1), new ItemStack(Items.STICK, 2)));
        RECYCLE_MAP.put(Items.GOLDEN_HOE, List.of(new ItemStack(Items.GOLD_INGOT, 2), new ItemStack(Items.STICK, 2)));
        RECYCLE_MAP.put(Items.GOLDEN_SWORD, List.of(new ItemStack(Items.GOLD_INGOT, 2), new ItemStack(Items.STICK, 1)));
        RECYCLE_MAP.put(Items.GOLDEN_SPEAR, List.of(new ItemStack(Items.GOLD_INGOT, 1), new ItemStack(Items.STICK, 2)));

        RECYCLE_MAP.put(Items.DIAMOND_PICKAXE, List.of(new ItemStack(Items.DIAMOND, 3), new ItemStack(Items.STICK, 2)));
        RECYCLE_MAP.put(Items.DIAMOND_AXE, List.of(new ItemStack(Items.DIAMOND, 3), new ItemStack(Items.STICK, 2)));
        RECYCLE_MAP.put(Items.DIAMOND_SHOVEL, List.of(new ItemStack(Items.DIAMOND, 1), new ItemStack(Items.STICK, 2)));
        RECYCLE_MAP.put(Items.DIAMOND_HOE, List.of(new ItemStack(Items.DIAMOND, 2), new ItemStack(Items.STICK, 2)));
        RECYCLE_MAP.put(Items.DIAMOND_SWORD, List.of(new ItemStack(Items.DIAMOND, 2), new ItemStack(Items.STICK, 1)));
        RECYCLE_MAP.put(Items.DIAMOND_SPEAR, List.of(new ItemStack(Items.DIAMOND, 1), new ItemStack(Items.STICK, 2)));

        RECYCLE_MAP.put(Items.NETHERITE_PICKAXE, List.of(new ItemStack(Items.NETHERITE_INGOT, 1), new ItemStack(Items.DIAMOND, 3), new ItemStack(Items.STICK, 2)));
        RECYCLE_MAP.put(Items.NETHERITE_AXE, List.of(new ItemStack(Items.NETHERITE_INGOT, 1), new ItemStack(Items.DIAMOND, 3), new ItemStack(Items.STICK, 2)));
        RECYCLE_MAP.put(Items.NETHERITE_SHOVEL, List.of(new ItemStack(Items.NETHERITE_INGOT, 1), new ItemStack(Items.DIAMOND, 1), new ItemStack(Items.STICK, 2)));
        RECYCLE_MAP.put(Items.NETHERITE_HOE, List.of(new ItemStack(Items.NETHERITE_INGOT, 1), new ItemStack(Items.DIAMOND, 2), new ItemStack(Items.STICK, 2)));
        RECYCLE_MAP.put(Items.NETHERITE_SWORD, List.of(new ItemStack(Items.NETHERITE_INGOT, 1), new ItemStack(Items.DIAMOND, 2), new ItemStack(Items.STICK, 1)));
        RECYCLE_MAP.put(Items.NETHERITE_SPEAR, List.of(new ItemStack(Items.NETHERITE_INGOT, 1), new ItemStack(Items.DIAMOND, 1), new ItemStack(Items.STICK, 2)));

        RECYCLE_MAP.put(Items.LEATHER_HELMET, List.of(new ItemStack(Items.LEATHER, 5)));
        RECYCLE_MAP.put(Items.LEATHER_CHESTPLATE, List.of(new ItemStack(Items.LEATHER, 8)));
        RECYCLE_MAP.put(Items.LEATHER_LEGGINGS, List.of(new ItemStack(Items.LEATHER, 7)));
        RECYCLE_MAP.put(Items.LEATHER_BOOTS, List.of(new ItemStack(Items.LEATHER, 4)));

        RECYCLE_MAP.put(Items.IRON_HELMET, List.of(new ItemStack(Items.IRON_INGOT, 5)));
        RECYCLE_MAP.put(Items.IRON_CHESTPLATE, List.of(new ItemStack(Items.IRON_INGOT, 8)));
        RECYCLE_MAP.put(Items.IRON_LEGGINGS, List.of(new ItemStack(Items.IRON_INGOT, 7)));
        RECYCLE_MAP.put(Items.IRON_BOOTS, List.of(new ItemStack(Items.IRON_INGOT, 4)));

        RECYCLE_MAP.put(Items.GOLDEN_HELMET, List.of(new ItemStack(Items.GOLD_INGOT, 5)));
        RECYCLE_MAP.put(Items.GOLDEN_CHESTPLATE, List.of(new ItemStack(Items.GOLD_INGOT, 8)));
        RECYCLE_MAP.put(Items.GOLDEN_LEGGINGS, List.of(new ItemStack(Items.GOLD_INGOT, 7)));
        RECYCLE_MAP.put(Items.GOLDEN_BOOTS, List.of(new ItemStack(Items.GOLD_INGOT, 4)));

        RECYCLE_MAP.put(Items.DIAMOND_HELMET, List.of(new ItemStack(Items.DIAMOND, 5)));
        RECYCLE_MAP.put(Items.DIAMOND_CHESTPLATE, List.of(new ItemStack(Items.DIAMOND, 8)));
        RECYCLE_MAP.put(Items.DIAMOND_LEGGINGS, List.of(new ItemStack(Items.DIAMOND, 7)));
        RECYCLE_MAP.put(Items.DIAMOND_BOOTS, List.of(new ItemStack(Items.DIAMOND, 4)));

        RECYCLE_MAP.put(Items.NETHERITE_HELMET, List.of(new ItemStack(Items.NETHERITE_INGOT, 1), new ItemStack(Items.DIAMOND, 5)));
        RECYCLE_MAP.put(Items.NETHERITE_CHESTPLATE, List.of(new ItemStack(Items.NETHERITE_INGOT, 1), new ItemStack(Items.DIAMOND, 8)));
        RECYCLE_MAP.put(Items.NETHERITE_LEGGINGS, List.of(new ItemStack(Items.NETHERITE_INGOT, 1), new ItemStack(Items.DIAMOND, 7)));
        RECYCLE_MAP.put(Items.NETHERITE_BOOTS, List.of(new ItemStack(Items.NETHERITE_INGOT, 1), new ItemStack(Items.DIAMOND, 4)));

        RECYCLE_MAP.put(Items.TURTLE_HELMET, List.of(new ItemStack(Items.TURTLE_SCUTE, 5)));

        RECYCLE_MAP.put(Items.CHAINMAIL_HELMET, List.of(new ItemStack(Items.IRON_NUGGET, 3)));
        RECYCLE_MAP.put(Items.CHAINMAIL_CHESTPLATE, List.of(new ItemStack(Items.IRON_NUGGET, 5)));
        RECYCLE_MAP.put(Items.CHAINMAIL_LEGGINGS, List.of(new ItemStack(Items.IRON_NUGGET, 4)));
        RECYCLE_MAP.put(Items.CHAINMAIL_BOOTS, List.of(new ItemStack(Items.IRON_NUGGET, 2)));

        RECYCLE_MAP.put(Items.SHIELD, List.of(new ItemStack(Items.IRON_INGOT, 1), new ItemStack(Items.OAK_PLANKS, 6)));

        RECYCLE_MAP.put(Items.BOW, List.of(new ItemStack(Items.STICK, 3), new ItemStack(Items.STRING, 3)));

        RECYCLE_MAP.put(Items.CROSSBOW, List.of(new ItemStack(Items.STICK, 3), new ItemStack(Items.STRING, 2), new ItemStack(Items.IRON_INGOT, 1), new ItemStack(Items.TRIPWIRE_HOOK, 1)));

        RECYCLE_MAP.put(Items.FISHING_ROD, List.of(new ItemStack(Items.STICK, 3), new ItemStack(Items.STRING, 2)));

        RECYCLE_MAP.put(Items.FLINT_AND_STEEL, List.of(new ItemStack(Items.IRON_INGOT, 1), new ItemStack(Items.FLINT, 1)));

        RECYCLE_MAP.put(Items.SHEARS, List.of(new ItemStack(Items.IRON_INGOT, 2)));

        RECYCLE_MAP.put(Items.CARROT_ON_A_STICK, List.of(new ItemStack(Items.FISHING_ROD, 1), new ItemStack(Items.CARROT, 1)));

        RECYCLE_MAP.put(Items.WARPED_FUNGUS_ON_A_STICK, List.of(new ItemStack(Items.FISHING_ROD, 1), new ItemStack(Items.WARPED_FUNGUS, 1)));

        RECYCLE_MAP.put(Items.ELYTRA, List.of(new ItemStack(Items.PHANTOM_MEMBRANE, 5), new ItemStack(Items.LEATHER, 2)));

        RECYCLE_MAP.put(Items.TRIDENT, List.of(new ItemStack(Items.PRISMARINE_SHARD, 5), new ItemStack(Items.PRISMARINE_CRYSTALS, 2)));

        RECYCLE_MAP.put(Items.MACE, List.of(new ItemStack(Items.HEAVY_CORE, 1), new ItemStack(Items.BREEZE_ROD, 1)));

        RECYCLE_MAP.put(Items.BRUSH, List.of(new ItemStack(Items.COPPER_INGOT, 1), new ItemStack(Items.STICK, 1), new ItemStack(Items.FEATHER, 1)));
    }
}
