package com.reductive.mixin;

import com.reductive.ReductiveItemRegistry;
import com.reductive.ReductiveLootTableRegistry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.function.Predicate;

@Mixin(FishingHook.class)
public class FishingHookMixin {
    @Unique Predicate<ItemStack> SUPPORTED_LURES = (stack) -> stack.is(ReductiveItemRegistry.BAIT_TREASURE);
    @Unique boolean lureUsed;
    @Unique LootTable currentLootTable;
    @Unique
    private LootParams currentParams;
    @Unique
    private static final ItemStack VANILLA_FISHING_ROD = new ItemStack(Items.FISHING_ROD);

    @Redirect(
            method = "shouldStopFishing",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;getMainHandItem()Lnet/minecraft/world/item/ItemStack;"
            )
    )
    private ItemStack reductive$validateMainHandFishingRod(Player player) {
        ItemStack stack = player.getMainHandItem();
        return reductive$isReductiveFishingRod(stack) ? VANILLA_FISHING_ROD : stack;
    }

    @Redirect(
            method = "shouldStopFishing",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;getOffhandItem()Lnet/minecraft/world/item/ItemStack;"
            )
    )
    private ItemStack reductive$validateOffhandFishingRod(Player player) {
        ItemStack stack = player.getOffhandItem();
        return reductive$isReductiveFishingRod(stack) ? VANILLA_FISHING_ROD : stack;
    }

    @Unique
    private static boolean reductive$isReductiveFishingRod(ItemStack stack) {
        return stack.is(ReductiveItemRegistry.FISHING_ROD_COPPER)
                || stack.is(ReductiveItemRegistry.FISHING_ROD_IRON)
                || stack.is(ReductiveItemRegistry.FISHING_ROD_GOLD)
                || stack.is(ReductiveItemRegistry.FISHING_ROD_DIAMOND)
                || stack.is(ReductiveItemRegistry.FISHING_ROD_NETHERITE);
    }

    @Inject(method = "retrieve", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/storage/loot/LootParams$Builder;<init>(Lnet/minecraft/server/level/ServerLevel;)V"
    ))
    private void reductive$onFishCaught(ItemStack rod, CallbackInfoReturnable<Integer> cir) {
        FishingHook hook = (FishingHook) (Object) this;
        Player owner = hook.getPlayerOwner();

        lureUsed = false;
        currentLootTable = getLootTable(BuiltInLootTables.FISHING, hook);
        currentParams = null;

        if (!hook.level().isClientSide() && owner != null) {
            ItemStack lureStack = getLure(owner);

            if (SUPPORTED_LURES.test(lureStack) && !lureStack.isEmpty()) {
                if (lureStack.is(ReductiveItemRegistry.BAIT_TREASURE)) {
                    currentLootTable = getLootTable(ReductiveLootTableRegistry.BAIT_TREASURE, hook);
                }

                if (!owner.isCreative()) lureStack.shrink(1);
                lureUsed = true;
            }
        }
    }

    @ModifyVariable(
            method = "retrieve",
            at = @At("STORE"),
            name = "lootTable")
    private LootTable reductive$changeLootTable(LootTable originalTable) {
        if (!lureUsed) return originalTable;

        FishingHook hook = (FishingHook) (Object) this;

        if (hook.level().getServer() != null) {
            LootTable customTable = currentLootTable;

            if (customTable != LootTable.EMPTY) {
                return customTable;
            }
        }

        return originalTable;
    }

    @ModifyVariable(
            method = "retrieve",
            at = @At("STORE"),
            name = "params"
    )
    private LootParams reductive$getParams(LootParams params) {
        this.currentParams = params;
        return params;
    }

    @ModifyVariable(
            method = "retrieve",
            at = @At("STORE"),
            name = "items")
    private List<ItemStack> reductive$rollExtraLoot(List<ItemStack> items) {
        for (int i = 0; i < 10; i++) {
            items.addAll(currentLootTable.getRandomItems(currentParams));
        }

        return items;
    }

    @Unique
    private ItemStack getLure(Player player) {
        ItemStack heldProjectile = ProjectileWeaponItem.getHeldProjectile(player, SUPPORTED_LURES);
        if (!heldProjectile.isEmpty()) {
            return heldProjectile;
        } else {
            for(int i = 0; i < player.getInventory().getContainerSize(); ++i) {
                ItemStack itemStack = player.getInventory().getItem(i);
                if (SUPPORTED_LURES.test(itemStack)) {
                    return itemStack;
                }
            }

            return ItemStack.EMPTY;
        }
    }

    @Unique
    private static LootTable getLootTable(ResourceKey<LootTable> lootTable, FishingHook hook) {
        return hook.level().getServer().reloadableRegistries().getLootTable(lootTable);
    }
}
