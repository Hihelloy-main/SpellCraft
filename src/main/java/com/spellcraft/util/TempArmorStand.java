package com.spellcraft.util;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.HashSet;
import java.util.Set;

import com.spellcraft.SpellCraftPlugin;

/**
 * Represents a temporary {@link ArmorStand} in the world that can be spawned
 * invisibly and removed automatically. Tracks all instances for global management.
 */
public class TempArmorStand {

    private static final Set<TempArmorStand> INSTANCES = new HashSet<>();

    private final ArmorStand stand;

    /**
     * Creates a new invisible, marker, invulnerable, and gravity-free armor stand
     * at the specified location and registers it as a temporary armor stand.
     *
     * @param loc the {@link Location} where the armor stand should be spawned
     */
    public TempArmorStand(Location loc) {
        this.stand = loc.getWorld().spawn(loc, ArmorStand.class, as -> {
            as.setInvisible(true);
            as.setMarker(true);
            as.setGravity(false);
            as.setInvulnerable(true);
            as.setMetadata(
                    "temp_armorstand",
                    new FixedMetadataValue(SpellCraftPlugin.getInstance(), true)
            );
        });

        INSTANCES.add(this);
    }

    /** @return the underlying {@link ArmorStand} entity */
    public ArmorStand getArmorStand() {
        return stand;
    }

    /**
     * Removes this temporary armor stand from the world and unregisters it.
     */
    public void remove() {
        stand.remove();
        INSTANCES.remove(this);
    }

    /**
     * Removes all currently tracked temporary armor stands from the world
     * and clears the internal registry.
     */
    public static void removeAll() {
        for (TempArmorStand tas : new HashSet<>(INSTANCES)) {
            tas.stand.remove();
        }
        INSTANCES.clear();
    }

    /**
     * @return an unmodifiable set of all currently tracked temporary armor stands
     */
    public static Set<TempArmorStand> getAll() {
        return Set.copyOf(INSTANCES);
    }
}
