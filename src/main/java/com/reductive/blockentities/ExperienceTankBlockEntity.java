package com.reductive.blockentities;

import com.reductive.registries.ReductiveBlockEntityRegistry;
import com.reductive.blocks.ExperienceTankBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class ExperienceTankBlockEntity extends BlockEntity {
    private int storedXp = 0;

    public ExperienceTankBlockEntity(BlockPos pos, BlockState state) {
        super(ReductiveBlockEntityRegistry.EXPERIENCE_TANK_BLOCK_ENTITY, pos, state);
    }

    public int getStoredXp() {
        return this.storedXp;
    }

    public void setStoredXp(int xp) {
        this.storedXp = xp;
        // let the game know it was modified
        this.setChanged();
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        storedXp = input.getIntOr("StoredXp", 0);
        storedXp = Math.clamp(storedXp, 0, ExperienceTankBlock.MAX_XP);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        output.putInt("StoredXp", storedXp);
        super.saveAdditional(output);
    }
}