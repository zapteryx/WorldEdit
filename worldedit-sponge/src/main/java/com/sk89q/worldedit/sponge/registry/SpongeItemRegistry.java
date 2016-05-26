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

import com.sk89q.worldedit.blocks.BaseItem;
import com.sk89q.worldedit.world.registry.ItemRegistry;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.item.ItemType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpongeItemRegistry implements ItemRegistry<SpongeItemRegistry.Wrapper> {

    private Map<ItemType, Integer> blockTypeMapping = new HashMap<>();
    private List<ItemType> blockTypes = new ArrayList<>();

    @Override
    public BaseItem createFromId(int id) {
        return new BaseItem(id);
    }

    @Override
    public BaseItem createFromId(String name) {
        ItemType targetType = Sponge.getRegistry().getType(ItemType.class, name).get();
        return getType(new Wrapper(targetType));
    }

    @Override
    public boolean hasEntry(int id) {
        return 0 <= id && id < blockTypes.size();
    }

    @Override
    public BaseItem getType(SpongeItemRegistry.Wrapper nativeType) {
        ItemType targetType = nativeType.getItemType();

        if (!blockTypeMapping.containsKey(targetType)) {
            blockTypes.add(targetType);

            blockTypeMapping.put(targetType, blockTypes.size() - 1);
        }

        return new BaseItem(blockTypeMapping.get(targetType));
    }

    @Override
    public SpongeItemRegistry.Wrapper toNative(BaseItem worldEditType) {
        ItemType type = blockTypes.get(worldEditType.getId());
        return new SpongeItemRegistry.Wrapper(type);
    }

    public static class Wrapper {
        private final ItemType itemType;

        public Wrapper(ItemType itemType) {
            this.itemType = itemType;
        }

        public ItemType getItemType() {
            return itemType;
        }
    }
}
