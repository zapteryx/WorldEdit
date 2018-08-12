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

package com.sk89q.worldedit.function.pattern;

import static com.google.common.base.Preconditions.checkNotNull;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.world.block.BlockStateHolder;

/**
 * A pattern that reads from {@link Clipboard}.
 */
public class ClipboardPattern extends AbstractPattern {

    private final Clipboard clipboard;
    private final int sx, sy, sz;
    private final Vector min;
    private MutableBlockVector mutable = new MutableBlockVector();

    /**
     * Create a new clipboard pattern.
     *
     * @param clipboard the clipboard
     */
    public ClipboardPattern(Clipboard clipboard) {
        checkNotNull(clipboard);
        this.clipboard = clipboard;
        Vector size = clipboard.getMaximumPoint().subtract(clipboard.getMinimumPoint()).add(1, 1, 1);
        this.sx = size.getBlockX();
        this.sy = size.getBlockY();
        this.sz = size.getBlockZ();
        this.min = clipboard.getMinimumPoint();
    }

    @Override
    public BlockStateHolder apply(Vector position) {
        int xp = position.getBlockX() % sx;
        int yp = position.getBlockY() % sy;
        int zp = position.getBlockZ() % sz;
        if (xp < 0) xp += sx;
        if (yp < 0) yp += sy;
        if (zp < 0) zp += sz;
        mutable.mutX((min.getX() + xp));
        mutable.mutY((min.getY() + yp));
        mutable.mutZ((min.getZ() + zp));
        return clipboard.getBlock(mutable);
    }


}