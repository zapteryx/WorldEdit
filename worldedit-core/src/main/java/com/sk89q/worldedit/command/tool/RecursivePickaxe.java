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

package com.sk89q.worldedit.command.tool;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extension.platform.Platform;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;

import java.util.HashSet;
import java.util.Set;

/**
 * A pickaxe mode that recursively finds adjacent blocks within range of
 * an initial block and of the same type.
 */
public class RecursivePickaxe implements BlockTool {
    private double range;

    public RecursivePickaxe(double range) {
        this.range = range;
    }

    @Override
    public boolean canUse(Actor player) {
        return player.hasPermission("worldedit.superpickaxe.recursive");
    }

    @Override
    public boolean actPrimary(Platform server, LocalConfiguration config, Player player, LocalSession session, com.sk89q.worldedit.util.Location clicked) {
        World world = (World) clicked.getExtent();
        final Vector pos = clicked.toVector();

        EditSession editSession = session.createEditSession(player);

        BlockStateHolder block = editSession.getBlock(pos);
        if (block.getBlockType().getMaterial().isAir()) {
            return true;
        }

        if (block.getBlockType() == BlockTypes.BEDROCK && !player.canDestroyBedrock()) {
            return true;
        }

        editSession.getSurvivalExtent().setToolUse(config.superPickaxeManyDrop);

        final int radius = (int) range;
        final BlockReplace replace = new BlockReplace(editSession, (editSession.nullBlock));
        editSession.setMask((Mask) null);
        RecursiveVisitor visitor = new RecursiveVisitor(new IdMask(editSession), replace, radius, editSession);
        visitor.visit(pos);
        Operations.completeBlindly(visitor);

        editSession.flushQueue();
        session.remember(editSession);

        return true;
    }


}