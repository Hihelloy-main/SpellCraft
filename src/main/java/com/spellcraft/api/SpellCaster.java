package com.spellcraft.api;

import com.spellcraft.api.house.House;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public interface SpellCaster {

    /**
     * @return this SpellCaster's {@link UUID}.
     */
    UUID getUUID();

    /**
     * @return the {@link Player} associated with this SpellCaster.
     */
    Player getPlayer();

    /**
     * @return this SpellCaster's current magic amount.
     */
    int getMagic();

    /**
     * Sets this SpellCaster's current magic amount.
     *
     * @param magic the magic amount to set.
     */
    void setMagic(int magic);

    /**
     * @return this SpellCaster's maximum magic capacity.
     */
    int getMaxMagic();

    /**
     * Sets this SpellCaster's maximum magic capacity.
     *
     * @param maxMagic the maximum magic to set.
     */
    void setMaxMagic(int maxMagic);

    /**
     * Checks whether this SpellCaster has at least a given amount of magic.
     *
     * @param amount the magic amount to check for.
     * @return true if this SpellCaster has enough magic, false otherwise.
     */
    boolean hasMagic(int amount);

    /**
     * Consumes a given amount of magic from this SpellCaster.
     *
     * @param amount the amount of magic to consume.
     */
    void consumeMagic(int amount);

    /**
     * Regenerates a given amount of magic for this SpellCaster.
     *
     * @param amount the amount of magic to regenerate.
     */
    void regenerateMagic(int amount);

    /**
     * @return a {@link List} of {@link Spell}s learned by this SpellCaster.
     */
    List<Spell> getLearnedSpells();

    /**
     * Checks whether this SpellCaster has learned a specific {@link Spell}.
     *
     * @param spell the {@link Spell} to check for.
     * @return true if the Spell has been learned, false otherwise.
     */
    boolean hasLearnedSpell(Spell spell);

    /**
     * Marks a {@link Spell} as learned by this SpellCaster.
     *
     * @param spell the {@link Spell} to learn.
     */
    void learnSpell(Spell spell);

    /**
     * Binds a {@link Spell} to a specific hotbar slot.
     *
     * @param slot the hotbar slot index.
     * @param spell the {@link Spell} to bind.
     */
    void bindSpell(int slot, Spell spell);

    /**
     * Saves this SpellCaster's bound spells to a {@link ConfigurationSection}.
     *
     * @param section the configuration section to save to.
     */
    void saveBinds(ConfigurationSection section);

    /**
     * Loads this SpellCaster's bound spells from a {@link ConfigurationSection}.
     *
     * @param section the configuration section to load from.
     * @param registry the {@link SpellManager} used to resolve spells.
     */
    void loadBinds(ConfigurationSection section, SpellManager registry);

    /**
     * Removes a learned {@link Spell} from this SpellCaster.
     *
     * @param spell the {@link Spell} to unlearn.
     */
    void unlearnSpell(Spell spell);

    /**
     * Saves this SpellCaster's learned spells to a {@link ConfigurationSection}.
     *
     * @param section the configuration section to save to.
     */
    void saveLearnedSpells(ConfigurationSection section);

    /**
     * Loads this SpellCaster's learned spells from a {@link ConfigurationSection}.
     *
     * @param section the configuration section to load from.
     * @param registry the {@link SpellManager} used to resolve spells.
     */
    void loadLearnedSpells(ConfigurationSection section, SpellManager registry);

    /**
     * @return an array of {@link Spell}s bound to this SpellCaster.
     */
    Spell[] getBoundSpells();

    /**
     * Unbinds any {@link Spell} bound to the given slot.
     *
     * @param slot the hotbar slot index.
     */
    void unbindSpell(int slot);

    /**
     * Gets the {@link Spell} bound to a specific slot.
     *
     * @param slot the hotbar slot index.
     * @return the {@link Spell} bound to the slot, or null if none is bound.
     */
    Spell getSpellAtSlot(int slot);

    /**
     * Checks whether a {@link Spell} is currently on cooldown.
     *
     * @param spell the {@link Spell} to check.
     * @return true if the Spell is on cooldown, false otherwise.
     */
    boolean isOnCooldown(Spell spell);

    /**
     * Sets a cooldown for a {@link Spell}.
     *
     * @param spell the {@link Spell} to apply the cooldown to.
     * @param duration the cooldown duration in milliseconds.
     */
    void setCooldown(Spell spell, long duration);

    /**
     * Gets the remaining cooldown time for a {@link Spell}.
     *
     * @param spell the {@link Spell} to check.
     * @return the remaining cooldown time in milliseconds.
     */
    long getRemainingCooldown(Spell spell);

    /**
     * Attempts to cast a {@link Spell}.
     *
     * @param spell the {@link Spell} to cast.
     * @return a {@link SpellResult} representing the result of the cast.
     */
    SpellResult castSpell(Spell spell);

    /**
     * @return this SpellCaster's {@link House}, or null if none is assigned.
     */
    House getHouse();

    /**
     * Sets this SpellCaster's {@link House}.
     *
     * @param house the {@link House} to assign.
     */
    void setHouse(House house);

    /**
     * @return true if this SpellCaster belongs to a {@link House}, false otherwise.
     */
    boolean hasHouse();
}
