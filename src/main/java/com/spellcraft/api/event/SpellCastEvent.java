package com.spellcraft.api.event;

import com.spellcraft.api.Spell;
import com.spellcraft.api.SpellCaster;

/**
 * Fired when a {@link SpellCaster} successfully casts a {@link Spell}.
 * <p>
 * This event occurs after all checks (cooldown, magic cost, permissions, etc.) have passed
 * and the spell is executed.
 */
public class SpellCastEvent extends SpellEvent {

    /**
     * Creates a new SpellCastEvent.
     *
     * @param caster the {@link SpellCaster} who cast the spell
     * @param spell  the {@link Spell} that was cast
     */
    public SpellCastEvent(SpellCaster caster, Spell spell) {
        super(caster, spell);
    }
}
