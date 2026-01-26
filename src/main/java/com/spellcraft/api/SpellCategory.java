package com.spellcraft.api;

/**
 * Represents the category a {@link Spell} belongs to.
 * Categories are used for organization, filtering, and presentation.
 */
public enum SpellCategory {

    /**
     * Spells that harness the power of elements.
     */
    ELEMENTAL("Elemental", "Spells that harness the power of elements"),

    /**
     * Offensive spells intended for combat.
     */
    COMBAT("Combat", "Offensive spells for battle"),

    /**
     * Utility spells that provide helpful effects.
     */
    UTILITY("Utility", "Helpful spells for various purposes"),

    /**
     * Spells that restore health or cure ailments.
     */
    HEALING("Healing", "Spells that restore health and cure ailments"),

    /**
     * Defensive spells such as shields or protective effects.
     */
    PROTECTION("Protection", "Defensive spells and shields"),

    /**
     * Spells focused on movement and travel.
     */
    TRANSPORTATION("Transportation", "Spells for movement and travel");

    private final String displayName;
    private final String description;

    SpellCategory(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    /**
     * @return the human-readable display name of this {@link SpellCategory}.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * @return a short description explaining the purpose of this {@link SpellCategory}.
     */
    public String getDescription() {
        return description;
    }
}
