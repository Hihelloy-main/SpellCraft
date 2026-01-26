package com.spellcraft.api.event;

import com.spellcraft.api.Spell;
import com.spellcraft.api.SpellCaster;
import com.spellcraft.api.SpellResult;
import org.jetbrains.annotations.Nullable;

/**
 * Fired when a {@link Spell} fails to cast for a {@link SpellCaster}.
 * <p>
 * This event provides the reason for failure and optionally maps it to a {@link SpellResult}.
 */
public class SpellFailEvent extends SpellEvent {

    /** The reason why the spell failed to cast. */
    private final String reason;

    /**
     * Creates a new SpellFailEvent.
     *
     * @param caster the {@link SpellCaster} who attempted to cast the spell
     * @param spell  the {@link Spell} that failed
     * @param reason the reason for the failure as a {@link String}
     */
    public SpellFailEvent(SpellCaster caster, Spell spell, String reason) {
        super(caster, spell);
        this.reason = reason;
    }

    /**
     * @return the reason why the spell failed as a {@link String}
     */
    public String getReason() {
        return reason;
    }

    /**
     * Converts the failure reason into a {@link SpellResult}, if possible.
     *
     * @return a {@link SpellResult} representing the failure, or null if it cannot be mapped
     */
    @Nullable
    public SpellResult getReasonInSpellResult() {
        return SpellResult.of(reason);
    }
}
