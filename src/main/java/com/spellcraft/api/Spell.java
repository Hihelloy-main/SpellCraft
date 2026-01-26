package com.spellcraft.api;

import com.spellcraft.api.magic.MagicElement;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;

public interface Spell {

    /**
     * @return this Spell's name as a {@link String}.
     */
    String getName();

    /**
     * @return this Spell's description as a {@link String}.
     */
    String getDescription();

    /**
     * @return this Spell's {@link SpellCategory}.
     */
    SpellCategory getCategory();

    /**
     * @return this Spell's magic cost as a {@link Integer}.
     */
    Integer getMagicCost();

    /**
     * @return this Spell's cooldown as a {@link Long}.
     */
    Long getCooldown();

    /**
     * @return this Spell's range as a {@link Double}.
     */
    Double getRange();

    /**
     * @param caster
     * @return a {@link SpellResult} representing whether or not this cast was carried out successfully and if not why wasn't it.
     */
    SpellResult cast(SpellCaster caster);

    /**
     * @param caster
     * @return a {@link Boolean} representing whether or not the caster can cast this Spell.
     */
    Boolean canCast(SpellCaster caster);

    /**
     * @return the {@link Permission} name attached to this Spell.
     */
    String getPermission();

    /**
     * @return a {@link Boolean} that is true if this Spell is enabled and false if it's not.
     */
    Boolean isEnabled();

    /**
     * A method to decide whether this Spell should be enabled or not.
     * @param enabled
     */
    void setEnabled(boolean enabled);

    /**
     * @return true for sneak abilities false for non sneak abilities.
     */
    boolean isSneakingAbility();

    /**
     * Make this method return the action that can activate this Spell if this is a sneak ability make isSneakingAbility return true and make this method return null.
     * @return the {@link Action} that when done can activate this Spell.
     */
    Action getAbilityActivationAction();

    /**
     * Make this method return this Spell's {@link MagicElement}.
     * @return this Spell's {@link MagicElement}.
     */
    MagicElement getElement();

    /**
     * @return this Spell's instructions on how to use it as a {@link String}.
     */
    String getInstructions();

    /**
     * Make this method return the location of this Spell.
     * @return this Spell's location as a {@link Location}.
     */
   @NotNull Location getLocation();
}
