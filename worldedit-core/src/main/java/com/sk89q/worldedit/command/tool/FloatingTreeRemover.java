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

import com.google.common.collect.Sets;
import com.sk89q.worldedit.*;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extension.platform.Platform;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.registry.Blocks;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

/**
 * A pickaxe mode that removes floating treetops (logs and leaves not connected
 * to anything else)
 */
public class FloatingTreeRemover implements BlockTool {
    private static final BaseBlock AIR = WorldEdit.getInstance().getBaseBlockFactory().getBaseBlock(Blocks.AIR.getId());
    private int rangeSq;

    public FloatingTreeRemover() {
        rangeSq = 100*100;
    }

    @Override
    public boolean canUse(Actor player) {
        return player.hasPermission("worldedit.tool.deltree");
    }

    private Set<Integer> getTreeArtifacts() {
        return Sets.newHashSet(
                Blocks.LEAVES.getId(),
                Blocks.LEAVES2.getId(),
                Blocks.VINE.getId()
        );
    }
    private Set<Integer> getTreeBlocks() {
        Set<Integer> treeBlocks = getTreeArtifacts();

        treeBlocks.add(Blocks.LOG.getId());
        treeBlocks.add(Blocks.LOG2.getId());
        treeBlocks.add(Blocks.BROWN_MUSHROOM_BLOCK.getId());
        treeBlocks.add(Blocks.RED_MUSHROOM_BLOCK.getId());

        return treeBlocks;
    }

    @Override
    public boolean actPrimary(Platform server, LocalConfiguration config,
            Player player, LocalSession session, Location clicked) {

        final World world = (World) clicked.getExtent();

        Set<Integer> treeBlocks = getTreeBlocks();
        int id = world.getLazyBlock(clicked.toVector()).getId();
        if (!treeBlocks.contains(id)) {
            player.printError("That's not a tree.");
            return true;
        }

        final EditSession editSession = session.createEditSession(player);

        try {
            final Set<Vector> blockSet = bfs(world, clicked.toVector());
            if (blockSet == null) {
                player.printError("That's not a floating tree.");
                return true;
            }

            for (Vector blockVector : blockSet) {
                final int typeId = editSession.getLazyBlock(blockVector).getId();
                if (treeBlocks.contains(typeId)) {
                    editSession.setBlock(blockVector, AIR);
                }
            }
        } catch (MaxChangedBlocksException e) {
            player.printError("Max blocks change limit reached.");
        } finally {
            session.remember(editSession);
        }

        return true;
    }

    Vector[] recurseDirections = {
        PlayerDirection.NORTH.vector(),
        PlayerDirection.EAST.vector(),
        PlayerDirection.SOUTH.vector(),
        PlayerDirection.WEST.vector(),
        PlayerDirection.UP.vector(),
        PlayerDirection.DOWN.vector(),
    };

    private Set<Integer> getTerminatingBlocks() {
        return Sets.newHashSet(
                Blocks.AIR.getId(),
                Blocks.SNOW_LAYER.getId()
        );
    }

    /**
     * Helper method.
     *
     * @param world the world that contains the tree
     * @param origin any point contained in the floating tree
     * @return a set containing all blocks in the tree/shroom or null if this is not a floating tree/shroom.
     */
    private Set<Vector> bfs(World world, Vector origin) throws MaxChangedBlocksException {
        final Set<Integer> treeBlocks = getTreeBlocks();
        final Set<Integer> terminatingBlocks = getTerminatingBlocks();

        final Set<Vector> visited = new HashSet<Vector>();
        final LinkedList<Vector> queue = new LinkedList<Vector>();

        queue.addLast(origin);
        visited.add(origin);

        while (!queue.isEmpty()) {
            final Vector current = queue.removeFirst();
            for (Vector recurseDirection : recurseDirections) {
                final Vector next = current.add(recurseDirection);
                if (origin.distanceSq(next) > rangeSq) {
                    // Maximum range exceeded => stop walking
                    continue;
                }

                if (visited.add(next)) {
                    final int nextTypeId = world.getLazyBlock(next).getId();
                    if (terminatingBlocks.contains(nextTypeId)) {
                        continue;
                    }

                    if (treeBlocks.contains(nextTypeId)) {
                        queue.addLast(next);
                        break;
                    }

                    // we hit something solid - evaluate where we came from
                    final int curTypeId =  world.getLazyBlock(current).getId();
                    if (getTreeArtifacts().contains(curTypeId)) {
                        // leaves touching a wall/the ground => stop walking this route
                        continue;
                    } else {
                        // log/shroom touching a wall/the ground => this is not a floating tree, bail out
                        return null;
                    }
                }
            }
        }

        return visited;
    }
}
