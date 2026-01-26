package com.spellcraft.core;

import com.spellcraft.SpellCraftPlugin;
import com.spellcraft.api.SpellCaster;
import com.spellcraft.util.ThreadUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SpellCasterManager {

    private final Map<UUID, SpellCaster> casters = new HashMap<>();
    private final int defaultMaxMagic;
    private final SpellCraftPlugin plugin;

    // Auto-save interval in ticks (20 ticks = 1 second)
    private static final long AUTO_SAVE_INTERVAL_TICKS = 20 * 60; // 1 minute

    public SpellCasterManager(SpellCraftPlugin plugin, int defaultMaxMagic) {
        this.plugin = plugin;
        this.defaultMaxMagic = defaultMaxMagic;

        startAutoSaveTask();
    }

    /**
     * Returns a SpellCaster for a player.
     * Loads it exactly once and reuses it thereafter.
     */
    public SpellCaster getCaster(Player player) {
        if (player == null) throw new IllegalArgumentException("player cannot be null");

        UUID uuid = player.getUniqueId();
        SpellCaster existing = casters.get(uuid);
        if (existing != null) return existing;

        SpellCaster loaded = plugin.getPlayerDataManager().load(player);
        casters.put(uuid, loaded);
        return loaded;
    }

    /**
     * Returns a SpellCaster ONLY if already loaded.
     */
    public SpellCaster getCasterIfLoaded(UUID uuid) {
        return casters.get(uuid);
    }

    /**
     * Removes a caster from memory (does NOT save).
     */
    public void removeCaster(UUID uuid) {
        casters.remove(uuid);
    }

    /**
     * Saves all loaded casters asynchronously.
     */
    public void saveAll() {
        casters.values().forEach(plugin.getPlayerDataManager()::saveAsync);
    }

    /**
     * Clears all loaded casters from memory.
     * Should be called AFTER saveAll().
     */
    public void clearCasters() {
        casters.clear();
    }

    /**
     * Returns a live, read-only view of loaded casters.
     * Intended for iteration ONLY.
     */
    public Map<UUID, SpellCaster> getAllCasters() {
        return Collections.unmodifiableMap(casters);
    }

    public int getDefaultMaxMagic() {
        return defaultMaxMagic;
    }

    /**
     * Registers a caster only if one is not already present.
     * Safe for reloads.
     */
    public void registerCaster(UUID uuid, SpellCaster caster) {
        casters.putIfAbsent(uuid, caster);
    }

    /**
     * Starts a repeating async task that saves all loaded players periodically.
     */
    private void startAutoSaveTask() {
        ThreadUtil.runAsyncTimer(this::saveAll, AUTO_SAVE_INTERVAL_TICKS, AUTO_SAVE_INTERVAL_TICKS);
    }
}
