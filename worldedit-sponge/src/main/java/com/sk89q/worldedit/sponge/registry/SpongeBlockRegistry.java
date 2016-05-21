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

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.world.registry.BlockRegistry;
import com.sk89q.worldedit.world.registry.State;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.trait.BlockTrait;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpongeBlockRegistry implements BlockRegistry<SpongeBlockRegistry.Wrapper> {

    private Map<BlockType, Integer> blockTypeMapping = new HashMap<>();
    private List<List<Map<BlockTrait<?>, ?>>> typeVariants = new ArrayList<>();
    private List<Map<String, SpongeBlockState<?>>> blockStates = new ArrayList<>();
    private List<BlockType> blockTypes = new ArrayList<>();

    @Override
    public BaseBlock createFromId(int id) {
        return WorldEdit.getInstance().getBaseBlockFactory().getBaseBlock(id);
    }

    @Override
    public BaseBlock createFromId(String name) {
        BlockType targetType = Sponge.getRegistry().getType(BlockType.class, name).get();
        if (blockTypeMapping.containsKey(targetType)) {
            return createFromId(blockTypeMapping.get(targetType));
        }
        return null;
    }

    @Override
    public boolean hasEntry(int id) {
        return typeVariants.size() > id  && id >= 0;
    }

    @Override
    public BaseBlock getType(SpongeBlockRegistry.Wrapper nativeType) {
        BlockType targetType = nativeType.getState().getType();
        Map<BlockTrait<?>, ?> traitMap = nativeType.getState().getTraitMap();

        if (!blockTypeMapping.containsKey(targetType)) {
            blockTypes.add(targetType);
            blockStates.add(generateBlockStates(targetType));
            typeVariants.add(new ArrayList<>());

            blockTypeMapping.put(targetType, typeVariants.size() - 1);
        }

        int blockId = blockTypeMapping.get(targetType);
        List<Map<BlockTrait<?>, ?>> blockVariants = typeVariants.get(blockId);
        int variantId = blockVariants.indexOf(traitMap);
        if (variantId == -1) {
            blockVariants.add(traitMap);
            variantId = blockVariants.size() - 1;
        }

        return WorldEdit.getInstance().getBaseBlockFactory().getBaseBlock(blockId, variantId);
    }

    private Map<String, SpongeBlockState<?>> generateBlockStates(BlockType type) {
        Map<String, SpongeBlockState<?>> blockStates = new HashMap<>();
        for (BlockTrait<?> trait : type.getTraits()) {
            blockStates.put(trait.getName(), new SpongeBlockState<>(trait));
        }
        return blockStates;
    }

    private static final BlockState.Builder blockStateBuilder = BlockState.builder();

    @Override
    public SpongeBlockRegistry.Wrapper toNative(BaseBlock worldEditType) {
        BlockType type = blockTypes.get(worldEditType.getId());
        Map<BlockTrait<?>, ?> traitSet = typeVariants.get(worldEditType.getId()).get(worldEditType.getData());

        BlockState nativeState = blockStateBuilder.reset().blockType(type).build();
        for (Map.Entry<BlockTrait<?>, ?> trait : traitSet.entrySet()) {
            nativeState = nativeState.withTrait(trait.getKey(), trait.getValue()).get();
        }

        return new Wrapper(nativeState);
    }

    @Override
    public Map<String, ? extends State> getStates(BaseBlock worldEditType) {
        return blockStates.get(worldEditType.getId());
    }

    public static class Wrapper {
        private final BlockState blockState;

        public Wrapper(BlockState blockState) {
            this.blockState = blockState;
        }

        public BlockState getState() {
            return blockState;
        }
    }
}
