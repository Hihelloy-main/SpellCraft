package com.spellcraft.util;

import com.spellcraft.api.Spell;
import com.spellcraft.api.event.SpellPlayerDamageEvent;
import com.spellcraft.api.magic.MagicElement;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

/**
 * Utility class for handling damage caused by spells.
 * Provides methods for applying spell damage and validating spell targets.
 */
public final class DamageHandler {

    /** Private constructor to prevent instantiation. */
    private DamageHandler() {}

    /**
     * Applies spell damage from a caster to a target.
     * If the target is a {@link Player}, fires a {@link SpellPlayerDamageEvent} which can be cancelled.
     *
     * @param caster the {@link Player} casting the spell
     * @param target the {@link LivingEntity} receiving damage
     * @param damage the amount of damage to apply
     * @param spellName the name of the spell causing the damage
     * @param element the {@link MagicElement} associated with the spell
     */
    public static void damage(
            Player caster,
            LivingEntity target,
            double damage,
            String spellName,
            MagicElement element
    ) {
        if (target.isDead()) return;
        if (target.equals(caster)) return;

        if (target instanceof Player player) {
            if (player.isInvulnerable()) return;

            SpellPlayerDamageEvent event =
                    new SpellPlayerDamageEvent(caster, player, spellName, element, damage);

            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) return;

            player.damage(event.getDamage(), caster);
            return;
        }

        target.damage(damage, caster);
    }

    public static void damage(
            Player caster,
            LivingEntity target,
            double damage,
            Spell spell,
            MagicElement element
    ) {
        damage(caster, target, damage, spell.getName(), element);
    }

    /**
     * Checks whether a target is a valid recipient for spell damage from a caster.
     *
     * @param caster the {@link Player} casting the spell
     * @param target the {@link LivingEntity} to check
     * @return true if the target can be damaged by the caster, false otherwise
     */
    public static boolean isValidTarget(Player caster, LivingEntity target) {
        if (target.isDead()) return false;
        if (target.equals(caster)) return false;

        if (target instanceof Player player) {
            return !player.isInvulnerable();
        }

        return true;
    }
}
