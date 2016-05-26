/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.sponge.registry;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.sponge.SpongeWorldData;
import com.sk89q.worldedit.world.registry.StateValue;
import net.minecraft.util.EnumFacing;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.trait.BlockTrait;

import javax.annotation.Nullable;
import java.util.Optional;

class SpongeBlockStateValue<T extends Comparable<T>> implements StateValue {

    private BlockTrait<T> blockTrait;
    private T value;

    SpongeBlockStateValue(BlockTrait<T> blockTrait, T value) {
        this.blockTrait = blockTrait;
        this.value = value;
    }

    @Override
    public boolean isSet(BaseBlock block) {
        BlockState blockState = SpongeWorldData.getInstance().getBlockRegistry().toNative(block).getState();
        Optional<T> traitValue = blockState.getTraitValue(blockTrait);
        return traitValue.isPresent() && value.equals(traitValue.get());
    }

    @Override
    public BaseBlock set(BaseBlock block) {
        SpongeBlockRegistry registry = SpongeWorldData.getInstance().getBlockRegistry();

        BlockState blockState = registry.toNative(block).getState();
        blockState = blockState.withTrait(blockTrait, value).get();

        return registry.getType(new SpongeBlockRegistry.Wrapper(blockState));
    }

    @Nullable
    @Override
    public Vector getDirection() {
        // TODO Convert to Sponge
        EnumFacing direction = EnumFacing.valueOf(value.toString());

        Vector dirVec;
        switch (direction.getAxis()) {
            case X:
                dirVec = new Vector(1, 0, 0);
                break;
            case Y:
                dirVec = new Vector(0, 1, 0);
                break;
            case Z:
                dirVec = new Vector(0, 1, 1);
                break;
            default:
                return null;
        }

        return direction.getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE ? dirVec.multiply(-1) : dirVec;
    }
}
