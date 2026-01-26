package com.spellcraft.util;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 * Utility class for finding a block that can serve as a "source" for spells.
 * Typically used to find water, lava, fire, or stone blocks in front of a player.
 */
public final class BlockSource {

    /** Private constructor to prevent instantiation. */
    private BlockSource() {}

    /**
     * Gets the first valid source {@link Block} in front of the {@link Player} within a given range.
     * Checks blocks along the player's line of sight in 0.5 block increments.
     *
     * @param player the {@link Player} to check from
     * @param range the maximum range to search for a source block
     * @return the first valid source {@link Block}, or null if none is found
     */
    public static Block getSourceBlock(Player player, double range) {
        Location eye = player.getEyeLocation();
        for (double i = 0; i <= range; i += 0.5) {
            Location loc = eye.clone().add(eye.getDirection().multiply(i));
            Block block = loc.getBlock();

            if (isValidSource(block)) {
                return block;
            }
        }
        return null;
    }

    /**
     * Determines if a block is a valid source block for spells.
     * Valid types include WATER, LAVA, FIRE, and STONE.
     *
     * @param block the {@link Block} to check
     * @return true if the block is a valid source, false otherwise
     */
    private static boolean isValidSource(Block block) {
        Material type = block.getType();
        return type == Material.WATER
                || type == Material.LAVA
                || type == Material.FIRE
                || type == Material.STONE;
    }
}
