package com.reductive.mixin;

import com.reductive.ReductiveItemRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Predicate;

@Mixin(FishingHook.class)
public class FishingHookMixin {
    @Unique Predicate<ItemStack> SUPPORTED_LURES = (stack) -> stack.is(ReductiveItemRegistry.PEBBLE);
//    @Unique private static final ResourceKey<LootTable> TEST_FISHING_LOOT = BuiltInLootTables.FISHING_TREASURE;


    @Inject(method = "retrieve", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/storage/loot/LootParams$Builder;<init>(Lnet/minecraft/server/level/ServerLevel;)V"
    ))
    private void onFishCaught(ItemStack rod, CallbackInfoReturnable<Integer> cir) {
        FishingHook hook = (FishingHook) (Object) this;
        Player owner = hook.getPlayerOwner();

        if (!hook.level().isClientSide() && owner != null) {
            ItemStack lureStack = getLure(owner);
            owner.sendSystemMessage(net.minecraft.network.chat.Component.literal("!" + lureStack));

            if (SUPPORTED_LURES.test(lureStack)) {
                if (!owner.isCreative()) lureStack.shrink(1);
            }
        }
    }

    @ModifyVariable(
            method = "retrieve",
            at = @At("STORE"),
            name = "lootTable")
    private LootTable changeLootTable(LootTable originalTable) {
        FishingHook hook = (FishingHook) (Object) this;

        if (hook.level().getServer() != null) {
            LootTable customTable = hook.level().getServer().reloadableRegistries()
                    .getLootTable(BuiltInLootTables.FISHING_TREASURE);

            if (customTable != LootTable.EMPTY) {
                return customTable;
            }
        }

        return originalTable;
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

            return player.hasInfiniteMaterials() ? new ItemStack(ReductiveItemRegistry.PEBBLE) : ItemStack.EMPTY;
        }
    }
}
