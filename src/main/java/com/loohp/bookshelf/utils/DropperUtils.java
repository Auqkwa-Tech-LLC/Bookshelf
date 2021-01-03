package com.loohp.bookshelf.utils;

import com.loohp.bookshelf.Bookshelf;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Dropper;

public class DropperUtils {

    private DropperUtils() {
    }

    public static Block getDropperRelative(Block block) {
        if (!block.getType().equals(Material.DROPPER)) {
            return null;
        }
        Block relativeBlock;
        if (Bookshelf.version.isLegacy()) {
            BlockFace face = ((org.bukkit.block.data.type.Dispenser) block.getBlockData()).getFacing();
            relativeBlock = block.getRelative(face);
        } else {
            Dropper dropper = (Dropper) block.getState();
            @SuppressWarnings("deprecation")
            int data = dropper.getRawData();
            BlockFace relative;
            switch (data) {
                case 1:
                case 9:
                    relative = BlockFace.UP;
                    break;
                case 2:
                case 10:
                    relative = BlockFace.NORTH;
                    break;
                case 3:
                case 11:
                    relative = BlockFace.SOUTH;
                    break;
                case 4:
                case 12:
                    relative = BlockFace.WEST;
                    break;
                case 5:
                case 13:
                    relative = BlockFace.EAST;
                    break;
                default:
                    relative = BlockFace.DOWN;
            }
            relativeBlock = block.getRelative(relative);
        }
        return relativeBlock;
    }
}
