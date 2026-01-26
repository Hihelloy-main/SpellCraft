package com.spellcraft.util;

import org.bukkit.Location;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.FallingBlock;
import org.bukkit.util.Vector;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Represents a temporary {@link FallingBlock} in the world that can expire automatically
 * and optionally execute a callback when it lands.
 */
public class TempFallingBlock {

    private static final Map<FallingBlock, TempFallingBlock> INSTANCES = new ConcurrentHashMap<>();

    private final FallingBlock entity;
    private final long creationTime;
    private final boolean canExpire;
    private Consumer<TempFallingBlock> onLand;

    /**
     * Creates and spawns a temporary falling block at the given location.
     *
     * @param loc the {@link Location} to spawn the falling block
     * @param data the {@link BlockData} of the falling block
     * @param velocity the initial {@link Vector} velocity of the block
     * @param expire true if the block should automatically expire after a short duration
     */
    public TempFallingBlock(Location loc, BlockData data, Vector velocity, boolean expire) {
        this.entity = loc.getWorld().spawnFallingBlock(loc, data.clone());
        this.entity.setVelocity(velocity);
        this.entity.setDropItem(false);
        this.entity.setHurtEntities(false);

        this.creationTime = System.currentTimeMillis();
        this.canExpire = expire;

        INSTANCES.put(entity, this);
    }

    /** @return the underlying {@link FallingBlock} entity */
    public FallingBlock getEntity() {
        return entity;
    }

    /** Removes the falling block and unregisters it from internal tracking */
    public void remove() {
        entity.remove();
        INSTANCES.remove(entity);
    }

    /**
     * Sets a callback to be executed when this block lands.
     *
     * @param onLand a {@link Consumer} receiving this {@link TempFallingBlock} instance
     */
    public void setOnLand(Consumer<TempFallingBlock> onLand) {
        this.onLand = onLand;
    }

    /** Calls the onLand callback if one has been set */
    public void callOnLand() {
        if (onLand != null) onLand.accept(this);
    }

    /**
     * Ticks all temporary falling blocks, removing them if their lifetime has expired.
     * Blocks with canExpire=true expire after 5 seconds; all blocks are forcibly removed after 2 minutes.
     */
    public static void tick() {
        long now = System.currentTimeMillis();

        for (TempFallingBlock tfb : INSTANCES.values()) {
            if ((tfb.canExpire && now - tfb.creationTime > 5000)
                    || now - tfb.creationTime > 120000) {
                tfb.remove();
            }
        }
    }

    /**
     * Checks if the given falling block is a tracked temporary falling block.
     *
     * @param fb the {@link FallingBlock} to check
     * @return true if this block is a temporary falling block
     */
    public static boolean isTempFallingBlock(FallingBlock fb) {
        return INSTANCES.containsKey(fb);
    }

    /**
     * Gets the {@link TempFallingBlock} instance corresponding to the given falling block.
     *
     * @param fb the {@link FallingBlock} to look up
     * @return the {@link TempFallingBlock} instance or null if not tracked
     */
    public static TempFallingBlock get(FallingBlock fb) {
        return INSTANCES.get(fb);
    }

    /** Removes all tracked temporary falling blocks immediately */
    public static void removeAll() {
        for (TempFallingBlock tfb : INSTANCES.values()) {
            tfb.entity.remove();
        }
        INSTANCES.clear();
    }
}
