package com.spellcraft.api;

import com.spellcraft.core.AbstractSpell;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Manages the lifecycle, registration, and lookup of {@link Spell}s.
 * This interface also provides asynchronous variants for expensive operations.
 */
public interface SpellManager {

    /**
     * Registers a {@link Spell} synchronously.
     *
     * @param spell the {@link Spell} to register.
     */
    void registerSpell(Spell spell);

    /**
     * Registers a {@link Spell} asynchronously.
     *
     * @param spell the {@link Spell} to register.
     */
    void registerSpellAsync(Spell spell);

    /**
     * Unregisters a {@link Spell} synchronously.
     *
     * @param spell the {@link Spell} to unregister.
     */
    void unregisterSpell(Spell spell);

    /**
     * Unregisters a {@link Spell} asynchronously.
     *
     * @param spell the {@link Spell} to unregister.
     */
    void unregisterSpellAsync(Spell spell);

    /**
     * Retrieves a {@link Spell} by name.
     *
     * @param name the name of the {@link Spell}.
     * @return an {@link Optional} containing the {@link Spell} if found.
     */
    Optional<Spell> getSpell(String name);

    /**
     * @return a {@link Collection} of all registered {@link Spell}s.
     */
    Collection<Spell> getAllSpells();

    /**
     * @return an immutable or live {@link Map} of spell names to {@link Spell}s.
     */
    Map<String, Spell> getSpellMap();

    /**
     * Retrieves all registered {@link Spell}s asynchronously.
     *
     * @return a {@link CompletableFuture} containing all {@link Spell}s.
     */
    CompletableFuture<Collection<Spell>> getAllSpellsAsync();

    /**
     * Retrieves all {@link Spell}s belonging to a specific {@link SpellCategory}.
     *
     * @param category the {@link SpellCategory} to filter by.
     * @return a {@link Collection} of matching {@link Spell}s.
     */
    Collection<Spell> getSpellsByCategory(SpellCategory category);

    /**
     * Retrieves all {@link Spell}s belonging to a specific {@link SpellCategory} asynchronously.
     *
     * @param category the {@link SpellCategory} to filter by.
     * @return a {@link CompletableFuture} containing matching {@link Spell}s.
     */
    CompletableFuture<Collection<Spell>> getSpellsByCategoryAsync(SpellCategory category);

    /**
     * Checks whether a {@link Spell} is registered by name.
     *
     * @param name the spell name.
     * @return true if the {@link Spell} is registered.
     */
    boolean isSpellRegistered(String name);

    /**
     * Checks whether a {@link Spell} is registered by name asynchronously.
     *
     * @param name the spell name.
     * @return a {@link CompletableFuture} containing the result.
     */
    CompletableFuture<Boolean> isSpellRegisteredAsync(String name);

    /**
     * Reloads all registered {@link Spell}s.
     */
    void reloadSpells();

    /**
     * Retrieves a {@link Spell} by name asynchronously.
     *
     * @param name the spell name.
     * @return a {@link CompletableFuture} containing an {@link Optional} {@link Spell}.
     */
    CompletableFuture<Optional<Spell>> getSpellAsync(String name);

    /**
     * Shuts down this {@link SpellManager} and releases resources.
     */
    void shutdown();

    /**
     * Tracks an active {@link AbstractSpell} instance.
     *
     * @param spell the {@link AbstractSpell} to track.
     */
    void track(AbstractSpell spell);

    /**
     * @return a {@link Set} of currently active {@link AbstractSpell}s.
     */
    Set<AbstractSpell> getActiveSpells();
}
