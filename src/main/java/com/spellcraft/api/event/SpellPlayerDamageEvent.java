package com.spellcraft.api.event;

import com.spellcraft.api.magic.MagicElement;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Fired when a {@link Player} takes damage from a spell cast by another {@link Player}.
 * <p>
 * This event allows modification of the damage dealt and can also be cancelled to prevent damage.
 */
public class SpellPlayerDamageEvent extends Event {

    /** The HandlerList required by Bukkit for event handling. */
    private static final HandlerList handlers = new HandlerList();

    /** The player who cast the spell. */
    private final Player caster;

    /** The player who is being damaged by the spell. */
    private final Player target;

    /** The name of the spell causing the damage. */
    private final String spellName;

    /** The element type of the spell causing the damage. */
    private final MagicElement element;

    /** The amount of damage to be dealt. */
    private double damage;

    /** Whether the event is cancelled. */
    private boolean cancelled;

    /**
     * Creates a new SpellPlayerDamageEvent.
     *
     * @param caster    the player casting the spell
     * @param target    the player being damaged
     * @param spellName the name of the spell
     * @param element   the element of the spell
     * @param damage    the amount of damage to be applied
     */
    public SpellPlayerDamageEvent(
            Player caster,
            Player target,
            String spellName,
            MagicElement element,
            double damage
    ) {
        this.caster = caster;
        this.target = target;
        this.spellName = spellName;
        this.element = element;
        this.damage = damage;
    }

    /**
     * @return the player who cast the spell
     */
    public Player getCaster() {
        return caster;
    }

    /**
     * @return the player who is being damaged
     */
    public Player getTarget() {
        return target;
    }

    /**
     * @return the name of the spell causing damage
     */
    public String getSpellName() {
        return spellName;
    }

    /**
     * @return the element type of the spell
     */
    public MagicElement getElement() {
        return element;
    }

    /**
     * @return the current amount of damage to be dealt
     */
    public double getDamage() {
        return damage;
    }

    /**
     * Sets the amount of damage to be dealt.
     *
     * @param damage the new damage value
     */
    public void setDamage(double damage) {
        this.damage = damage;
    }

    /**
     * @return true if the event has been cancelled, false otherwise
     */
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * Sets whether this event is cancelled.
     *
     * @param cancelled true to cancel the event, false to allow damage
     */
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    /**
     * Required Bukkit method to retrieve the event's handler list.
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
