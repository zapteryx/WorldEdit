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

package com.sk89q.worldedit.blocks;

/**
 * Block data related classes.
 */
public final class BlockData {

    private BlockData() {
    }

    /**
     * Cycle a block's data value. This usually goes through some rotational pattern
     * depending on the block. If it returns -1, it means the id and data specified
     * do not have anything to cycle to.
     *
     * @param type block id to be cycled
     * @param data block data value that it starts at
     * @param increment whether to go forward (1) or backward (-1) in the cycle
     * @return the new data value for the block
     */
    public static int cycle(int type, int data, int increment) {
        if (increment != -1 && increment != 1) {
            throw new IllegalArgumentException("Increment must be 1 or -1.");
        }

        int store;
        switch (type) {

        // special case here, going to use "forward" for type and "backward" for orientation
        case BlockID.LOG:
        case BlockID.LOG2:
            if (increment == -1) {
                store = data & 0x3; // copy bottom (type) bits
                return mod((data & ~0x3) + 4, 16) | store; // switch orientation with top bits and reapply bottom bits;
            } else {
                store = data & ~0x3; // copy top (orientation) bits
                return mod((data & 0x3) + 1, 4) | store;  // switch type with bottom bits and reapply top bits
            }

        // <del>same here</del> - screw you unit tests
        /*case BlockID.QUARTZ_BLOCK:
            if (increment == -1 && data > 2) {
                switch (data) {
                case 2: return 3;
                case 3: return 4;
                case 4: return 2;
                }
            } else if (increment == 1) {
                switch (data) {
                case 0:
                    return 1;
                case 1:
                    return 2;
                case 2:
                case 3:
                case 4:
                    return 0;
                }
            } else {
                return -1;
            }*/

        case BlockID.LONG_GRASS:
        case BlockID.SANDSTONE:
        case BlockID.DIRT:
            if (data > 2) return -1;
            return mod((data + increment), 3);

        case BlockID.TORCH:
        case BlockID.REDSTONE_TORCH_ON:
        case BlockID.REDSTONE_TORCH_OFF:
            if (data < 1 || data > 4) return -1;
            return mod((data - 1 + increment), 4) + 1;

        case BlockID.OAK_WOOD_STAIRS:
        case BlockID.COBBLESTONE_STAIRS:
        case BlockID.BRICK_STAIRS:
        case BlockID.STONE_BRICK_STAIRS:
        case BlockID.NETHER_BRICK_STAIRS:
        case BlockID.SANDSTONE_STAIRS:
        case BlockID.SPRUCE_WOOD_STAIRS:
        case BlockID.BIRCH_WOOD_STAIRS:
        case BlockID.JUNGLE_WOOD_STAIRS:
        case BlockID.QUARTZ_STAIRS:
        case BlockID.ACACIA_STAIRS:
        case BlockID.DARK_OAK_STAIRS:
            if (data > 7) return -1;
            return mod((data + increment), 8);

        case BlockID.STONE_BRICK:
        case BlockID.QUARTZ_BLOCK:
        case BlockID.PUMPKIN:
        case BlockID.JACKOLANTERN:
        case BlockID.NETHER_WART:
        case BlockID.CAULDRON:
        case BlockID.WOODEN_STEP:
        case BlockID.DOUBLE_WOODEN_STEP:
        case BlockID.HAY_BLOCK:
            if (data > 3) return -1;
            return mod((data + increment), 4);

        case BlockID.STEP:
        case BlockID.DOUBLE_STEP:
        case BlockID.CAKE_BLOCK:
        case BlockID.PISTON_BASE:
        case BlockID.PISTON_STICKY_BASE:
        case BlockID.SILVERFISH_BLOCK:
            if (data > 5) return -1;
            return mod((data + increment), 6);

        case BlockID.DOUBLE_PLANT:
            store = data & 0x8; // top half flag
            data &= ~0x8;
            if (data > 5) return -1;
            return mod((data + increment), 6) | store;

        case BlockID.CROPS:
        case BlockID.PUMPKIN_STEM:
        case BlockID.MELON_STEM:
            if (data > 6) return -1;
            return mod((data + increment), 7);

        case BlockID.SOIL:
        case BlockID.RED_FLOWER:
            if (data > 8) return -1;
            return mod((data + increment), 9);

        case BlockID.RED_MUSHROOM_CAP:
        case BlockID.BROWN_MUSHROOM_CAP:
            if (data > 10) return -1;
            return mod((data + increment), 11);

        case BlockID.CACTUS:
        case BlockID.REED:
        case BlockID.SIGN_POST:
        case BlockID.VINE:
        case BlockID.SNOW:
        case BlockID.COCOA_PLANT:
            if (data > 15) return -1;
            return mod((data + increment), 16);

        case BlockID.FURNACE:
        case BlockID.BURNING_FURNACE:
        case BlockID.WALL_SIGN:
        case BlockID.LADDER:
        case BlockID.CHEST:
        case BlockID.ENDER_CHEST:
        case BlockID.TRAPPED_CHEST:
        case BlockID.HOPPER:
            int extra = data & 0x8;
            int withoutFlags = data & ~0x8;
            if (withoutFlags < 2 || withoutFlags > 5) return -1;
            return (mod((withoutFlags - 2 + increment), 4) + 2) | extra;

        case BlockID.DISPENSER:
        case BlockID.DROPPER:
            store = data & 0x8;
            data &= ~0x8;
            if (data > 5) return -1;
            return mod((data + increment), 6) | store;

        case BlockID.REDSTONE_REPEATER_OFF:
        case BlockID.REDSTONE_REPEATER_ON:
        case BlockID.COMPARATOR_OFF:
        case BlockID.COMPARATOR_ON:
        case BlockID.TRAP_DOOR:
        case BlockID.FENCE_GATE:
        case BlockID.LEAVES:
        case BlockID.LEAVES2:
            if (data > 7) return -1;
            store = data & ~0x3;
            return mod(((data & 0x3) + increment), 4) | store;

        case BlockID.MINECART_TRACKS:
            if (data < 6 || data > 9) return -1;
            return mod((data - 6 + increment), 4) + 6;

        case BlockID.SAPLING:
            if ((data & 0x3) == 3 || data > 15) return -1;
            store = data & ~0x3;
            return mod(((data & 0x3) + increment), 3) | store;

        case BlockID.FLOWER_POT:
            if (data > 13) return -1;
            return mod((data + increment), 14);

        case BlockID.CLOTH:
        case BlockID.STAINED_CLAY:
        case BlockID.CARPET:
        case BlockID.STAINED_GLASS:
        case BlockID.STAINED_GLASS_PANE:
            if (increment == 1) {
                data = nextClothColor(data);
            } else if (increment == -1) {
                data = prevClothColor(data);
            }
            return data;

        default:
            return -1;
        }
    }

    /**
     * Returns the data value for the next color of cloth in the rainbow. This
     * should not be used if you want to just increment the data value.
     *
     * @param data the data value
     * @return the next data value
     */
    public static int nextClothColor(int data) {
        switch (data) {
            case ClothColor.ID.WHITE: return ClothColor.ID.LIGHT_GRAY;
            case ClothColor.ID.LIGHT_GRAY: return ClothColor.ID.GRAY;
            case ClothColor.ID.GRAY: return ClothColor.ID.BLACK;
            case ClothColor.ID.BLACK: return ClothColor.ID.BROWN;
            case ClothColor.ID.BROWN: return ClothColor.ID.RED;
            case ClothColor.ID.RED: return ClothColor.ID.ORANGE;
            case ClothColor.ID.ORANGE: return ClothColor.ID.YELLOW;
            case ClothColor.ID.YELLOW: return ClothColor.ID.LIGHT_GREEN;
            case ClothColor.ID.LIGHT_GREEN: return ClothColor.ID.DARK_GREEN;
            case ClothColor.ID.DARK_GREEN: return ClothColor.ID.CYAN;
            case ClothColor.ID.CYAN: return ClothColor.ID.LIGHT_BLUE;
            case ClothColor.ID.LIGHT_BLUE: return ClothColor.ID.BLUE;
            case ClothColor.ID.BLUE: return ClothColor.ID.PURPLE;
            case ClothColor.ID.PURPLE: return ClothColor.ID.MAGENTA;
            case ClothColor.ID.MAGENTA: return ClothColor.ID.PINK;
            case ClothColor.ID.PINK: return ClothColor.ID.WHITE;
        }

        return ClothColor.ID.WHITE;
    }

    /**
     * Returns the data value for the previous ext color of cloth in the rainbow.
     * This should not be used if you want to just increment the data value.
     *
     * @param data the data value
     * @return the new data value
     */
    public static int prevClothColor(int data) {
        switch (data) {
            case ClothColor.ID.LIGHT_GRAY: return ClothColor.ID.WHITE;
            case ClothColor.ID.GRAY: return ClothColor.ID.LIGHT_GRAY;
            case ClothColor.ID.BLACK: return ClothColor.ID.GRAY;
            case ClothColor.ID.BROWN: return ClothColor.ID.BLACK;
            case ClothColor.ID.RED: return ClothColor.ID.BROWN;
            case ClothColor.ID.ORANGE: return ClothColor.ID.RED;
            case ClothColor.ID.YELLOW: return ClothColor.ID.ORANGE;
            case ClothColor.ID.LIGHT_GREEN: return ClothColor.ID.YELLOW;
            case ClothColor.ID.DARK_GREEN: return ClothColor.ID.LIGHT_GREEN;
            case ClothColor.ID.CYAN: return ClothColor.ID.DARK_GREEN;
            case ClothColor.ID.LIGHT_BLUE: return ClothColor.ID.CYAN;
            case ClothColor.ID.BLUE: return ClothColor.ID.LIGHT_BLUE;
            case ClothColor.ID.PURPLE: return ClothColor.ID.BLUE;
            case ClothColor.ID.MAGENTA: return ClothColor.ID.PURPLE;
            case ClothColor.ID.PINK: return ClothColor.ID.MAGENTA;
            case ClothColor.ID.WHITE: return ClothColor.ID.PINK;
        }

        return ClothColor.ID.WHITE;
    }

    /**
     * Better modulo, not just remainder.
     */
    private static int mod(int x, int y) {
        int res = x % y;
        return res < 0 ? res + y : res;
    }

}
