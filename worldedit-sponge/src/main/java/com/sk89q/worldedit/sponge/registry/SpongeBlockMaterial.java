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

import com.sk89q.worldedit.blocks.BlockMaterial;
import org.spongepowered.api.block.BlockType;

public class SpongeBlockMaterial implements BlockMaterial {

    private BlockType blockType;

    public SpongeBlockMaterial(BlockType blockType) {
        this.blockType = blockType;
    }

    @Override
    public boolean isRenderedAsNormalBlock() {
        return false;
    }

    @Override
    public boolean isFullCube() {
        return false;
    }

    @Override
    public boolean isOpaque() {
        return false;
    }

    @Override
    public boolean isPowerSource() {
        return false;
    }

    @Override
    public boolean isLiquid() {
        return false;
    }

    @Override
    public boolean isSolid() {
        return false;
    }

    @Override
    public float getHardness() {
        return 0;
    }

    @Override
    public float getResistance() {
        return 0;
    }

    @Override
    public float getSlipperiness() {
        return 0;
    }

    @Override
    public boolean isGrassBlocking() {
        return false;
    }

    @Override
    public float getAmbientOcclusionLightValue() {
        return 0;
    }

    @Override
    public int getLightOpacity() {
        return 0;
    }

    @Override
    public int getLightValue() {
        return 0;
    }

    @Override
    public boolean isFragileWhenPushed() {
        return false;
    }

    @Override
    public boolean isUnpushable() {
        return false;
    }

    @Override
    public boolean isAdventureModeExempt() {
        return false;
    }

    @Override
    public boolean isTicksRandomly() {
        return false;
    }

    @Override
    public boolean isUsingNeighborLight() {
        return false;
    }

    @Override
    public boolean isMovementBlocker() {
        return false;
    }

    @Override
    public boolean isBurnable() {
        return false;
    }

    @Override
    public boolean isToolRequired() {
        return false;
    }

    @Override
    public boolean isReplacedDuringPlacement() {
        return false;
    }
}
