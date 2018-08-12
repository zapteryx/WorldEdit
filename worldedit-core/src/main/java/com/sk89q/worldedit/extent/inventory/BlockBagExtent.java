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

package com.sk89q.worldedit.extent.inventory;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

/**
 * Applies a {@link BlockBag} to operations.
 */
public class BlockBagExtent extends AbstractDelegateExtent {

    private final boolean mine;
    private int[] missingBlocks = new int[BlockTypes.size()];
    private BlockBag blockBag;

    /**
     * Create a new instance.
     *
     * @param extent   the extent
     * @param blockBag the block bag
     */
    public BlockBagExtent(Extent extent, @Nonnull BlockBag blockBag) {
        this(extent, blockBag, false);
    }

    public BlockBagExtent(Extent extent, @Nonnull BlockBag blockBag, boolean mine) {
        super(extent);
        checkNotNull(blockBag);
        this.blockBag = blockBag;
        this.mine = mine;
    }

    /**
     * Get the block bag.
     *
     * @return a block bag, which may be null if none is used
     */
    public
    @Nullable
    BlockBag getBlockBag() {
        return blockBag;
    }

    /**
     * Set the block bag.
     *
     * @param blockBag a block bag, which may be null if none is used
     */
    public void setBlockBag(@Nullable BlockBag blockBag) {
        this.blockBag = blockBag;
    }

    /**
     * Gets the list of missing blocks and clears the list for the next
     * operation.
     *
     * @return a map of missing blocks
     */
    public Map<BlockType, Integer> popMissing() {
        HashMap<BlockType, Integer> map = new HashMap<>();
        for (int i = 0; i < missingBlocks.length; i++) {
            int count = missingBlocks[i];
            if (count > 0) {
                map.put(BlockTypes.get(i), count);
            }
        }
        Arrays.fill(missingBlocks, 0);
        return map;
    }

    @Override
    public boolean setBlock(Vector pos, BlockStateHolder block) throws WorldEditException {
        return setBlock(pos.getBlockX(), pos.getBlockY(), pos.getBlockZ(), block);
    }

    @Override
    public boolean setBlock(int x, int y, int z, BlockStateHolder block) throws WorldEditException {
        BlockType type = block.getBlockType();
        if (!type.getMaterial().isAir()) {
            try {
                blockBag.fetchPlacedBlock(block.toImmutableState());
            } catch (UnplaceableBlockException e) {
                throw new FaweException.FaweBlockBagException();
            } catch (BlockBagException e) {
                missingBlocks[type.getInternalId()]++;
                throw new FaweException.FaweBlockBagException();
            }
        }
        if (mine) {
            BlockStateHolder lazyBlock = getExtent().getLazyBlock(x, y, z);
            BlockType fromType = lazyBlock.getBlockType();
            if (!fromType.getMaterial().isAir()) {
                try {
                    blockBag.storeDroppedBlock(fromType.getDefaultState());
                } catch (BlockBagException ignored) {
                }
            }
        }
        return getExtent().setBlock(x, y, z, block);
    }


}