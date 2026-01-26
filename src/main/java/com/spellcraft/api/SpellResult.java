package com.spellcraft.api;

import org.jetbrains.annotations.Nullable;

/**
 * Represents the result of attempting to cast a {@link Spell}.
 */
public enum SpellResult {

    /** Spell cast successfully. */
    SUCCESS("Spell cast successfully"),

    /** Spell cast failed for an unspecified reason. */
    FAILURE("Spell cast failed"),

    /** The caster does not have enough magic to cast the spell. */
    INSUFFICIENT_MAGIC("Not enough magic"),

    /** The spell is currently on cooldown. */
    ON_COOLDOWN("Spell is on cooldown"),

    /** The caster lacks the required permission to cast the spell. */
    NO_PERMISSION("Missing permission to cast this spell"),

    /** The spell could not be cast due to an invalid or missing target. */
    INVALID_TARGET("Invalid target for this spell"),

    /** The spell cast was cancelled by an external source. */
    CANCELLED("Spell cast was cancelled");

    private final String message;

    /**
     * Creates a new {@link SpellResult} with a default message.
     *
     * @param message the default message associated with this result.
     */
    SpellResult(String message) {
        this.message = message;
    }

    /**
     * @return the default message associated with this {@link SpellResult}.
     */
    public String getMessage() {
        return message;
    }

    /**
     * @return true if this result represents a successful spell cast.
     */
    public boolean isSuccess() {
        return this == SUCCESS;
    }

    /**
     * Attempts to resolve a {@link SpellResult} from a string value.
     *
     * @param message the string representation of the {@link SpellResult}.
     * @return the matching {@link SpellResult}, or null if no match is found.
     */
    public static @Nullable SpellResult of(String message) {
        if (message.equalsIgnoreCase("CANCELLED")) {
            return SpellResult.CANCELLED;
        }
        if (message.equalsIgnoreCase("INVALID_TARGET")) {
            return SpellResult.INVALID_TARGET;
        }
        if (message.equalsIgnoreCase("NO_PERMISSION")) {
            return SpellResult.NO_PERMISSION;
        }
        if (message.equalsIgnoreCase("ON_COOLDOWN")) {
            return SpellResult.ON_COOLDOWN;
        }
        if (message.equalsIgnoreCase("INSUFFICIENT_MAGIC")) {
            return SpellResult.INSUFFICIENT_MAGIC;
        }
        if (message.equalsIgnoreCase("FAILURE")) {
            return SpellResult.FAILURE;
        }
        if (message.equalsIgnoreCase("SUCCESS")) {
            return SpellResult.SUCCESS;
        }

        return null;
    }
}
