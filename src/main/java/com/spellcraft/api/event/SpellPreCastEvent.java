package com.spellcraft.api.event;

import com.spellcraft.api.Spell;
import com.spellcraft.api.SpellCaster;
import org.bukkit.event.Cancellable;

/**
 * Fired before a {@link Spell} is cast by a {@link SpellCaster}.
 * <p>
 * This event allows modification of the spell's magic cost and the ability
 * to cancel the spell entirely.
 */
public class SpellPreCastEvent extends SpellEvent implements Cancellable {

    /** Whether the event has been cancelled. */
    private boolean cancelled;

    /** The current magic cost of the spell. */
    private int magicCost;

    /**
     * Creates a new {@link SpellPreCastEvent}.
     *
     * @param caster    the {@link SpellCaster} attempting to cast the spell
     * @param spell     the {@link Spell} being cast
     * @param magicCost the initial magic cost of the spell
     */
    public SpellPreCastEvent(SpellCaster caster, Spell spell, int magicCost) {
        super(caster, spell);
        this.magicCost = magicCost;
    }

    /**
     * Gets the current magic cost of this spell.
     *
     * @return the magic cost as an {@link Integer}
     */
    public int getMagicCost() {
        return magicCost;
    }

    /**
     * Sets the magic cost of this spell. The value will be clamped to a minimum of 0.
     *
     * @param magicCost the new magic cost
     */
    public void setMagicCost(int magicCost) {
        this.magicCost = Math.max(0, magicCost);
    }

    /**
     * Checks if this event has been cancelled.
     *
     * @return true if the event is cancelled, false otherwise
     */
    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * Sets the cancelled state of this event. Cancelling this event will prevent the spell from being cast.
     *
     * @param cancel true to cancel the event, false to allow it
     */
    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}
