package com.spellcraft.util;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import java.util.List;

/**
 * Utility class for handling collision detection in the SpellCraft plugin.
 * Provides methods for detecting entities along a path or within a radius.
 */
public final class CollisionUtil {

    /** Private constructor to prevent instantiation. */
    private CollisionUtil() {}

    /**
     * Gets the first {@link LivingEntity} hit along a line between two {@link Location}s.
     * Iterates in steps along the path and checks for nearby entities within a given radius.
     *
     * @param start the starting {@link Location} of the line
     * @param end the ending {@link Location} of the line
     * @param radius the radius around the line to check for entities
     * @return the first {@link LivingEntity} found along the path, or null if none are hit
     */
    public static LivingEntity getFirstLivingEntityHit(Location start, Location end, double radius) {
        double step = 0.5;
        double distance = start.distance(end);

        for (double i = 0; i <= distance; i += step) {
            Location point = start.clone().add(
                    end.toVector().subtract(start.toVector()).normalize().multiply(i)
            );

            List<Entity> nearby = point.getWorld().getNearbyEntities(
                    point, radius, radius, radius
            ).stream().toList();

            for (Entity e : nearby) {
                if (e instanceof LivingEntity le && !le.isDead()) {
                    return le;
                }
            }
        }
        return null;
    }
}
