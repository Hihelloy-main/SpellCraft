package com.spellcraft.util;

import com.spellcraft.api.house.House;
import com.spellcraft.api.magic.MagicElement;

/**
 * Utility class for {@link House}-related helper methods.
 * Contains methods for checking house abilities and interactions with magic elements.
 */
public final class HouseUtil {

    /** Private constructor to prevent instantiation. */
    private HouseUtil() {}

    /**
     * Checks whether a given {@link House} can use a specific {@link MagicElement}.
     *
     * @param house the {@link House} to check
     * @param element the {@link MagicElement} to test
     * @return true if the house can use the element, false otherwise
     */
    public static boolean canUse(House house, MagicElement element) {
        return house.getMagicTypes().contains(element);
    }
}
