package com.spellcraft.api.event;

import com.spellcraft.api.Spell;
import com.spellcraft.api.SpellCaster;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Base class for all spell-related events in SpellCraft.
 * <p>
 * Provides common properties such as the {@link SpellCaster} who triggered the event
 * and the {@link Spell} associated with the event.
 */
public abstract class SpellEvent extends Event {

    /** The HandlerList required by Bukkit for event handling. */
    private static final HandlerList handlers = new HandlerList();

    /** The spell caster associated with this event. */
    protected final SpellCaster caster;

    /** The spell associated with this event. */
    protected final Spell spell;

    /**
     * Creates a new SpellEvent.
     *
     * @param caster the {@link SpellCaster} who triggered the event
     * @param spell  the {@link Spell} associated with the event
     */
    protected SpellEvent(SpellCaster caster, Spell spell) {
        this.caster = caster;
        this.spell = spell;
    }

    /**
     * @return the {@link SpellCaster} who triggered this event
     */
    public SpellCaster getCaster() {
        return caster;
    }

    /**
     * @return the {@link Spell} associated with this event
     */
    public Spell getSpell() {
        return spell;
    }

    /**
     * Required Bukkit method to retrieve this event's handler list.
     *
     * @return the {@link HandlerList} for this event
     */
    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    /**
     * Static method required by Bukkit to retrieve the handler list.
     *
     * @return the {@link HandlerList} for this event class
     */
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
