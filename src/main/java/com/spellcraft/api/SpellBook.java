package com.spellcraft.api;

import org.bukkit.inventory.ItemStack;

import java.util.List;

public interface SpellBook {

    /**
     * @return this SpellBook's title as a {@link String}.
     */
    String getTitle();

    /**
     * @return an immutable or mutable {@link List} of {@link Spell}s contained in this SpellBook.
     */
    List<Spell> getSpells();

    /**
     * Adds a {@link Spell} to this SpellBook.
     *
     * @param spell the {@link Spell} to add.
     */
    void addSpell(Spell spell);

    /**
     * Removes a {@link Spell} from this SpellBook.
     *
     * @param spell the {@link Spell} to remove.
     */
    void removeSpell(Spell spell);

    /**
     * Checks whether this SpellBook contains a specific {@link Spell}.
     *
     * @param spell the {@link Spell} to check for.
     * @return true if this SpellBook contains the Spell, false otherwise.
     */
    boolean containsSpell(Spell spell);

    /**
     * Converts this SpellBook into an {@link ItemStack} representation.
     *
     * @return this SpellBook as an {@link ItemStack}.
     */
    ItemStack toItemStack();

    /**
     * Checks whether the provided {@link ItemStack} represents a valid SpellBook.
     *
     * @param item the {@link ItemStack} to check.
     * @return true if the ItemStack is a valid SpellBook, false otherwise.
     */
    boolean isValidSpellBook(ItemStack item);

    /**
     * Creates a {@link SpellBook} instance from an {@link ItemStack}.
     * This method should only be called if {@link #isValidSpellBook(ItemStack)} returns true.
     *
     * @param item the {@link ItemStack} representing a SpellBook.
     * @return a {@link SpellBook} created from the ItemStack.
     */
    SpellBook fromItemStack(ItemStack item);
}
