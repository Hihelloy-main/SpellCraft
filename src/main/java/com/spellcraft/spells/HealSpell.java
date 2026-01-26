package com.spellcraft.spells;

import com.spellcraft.SpellCraftPlugin;
import com.spellcraft.api.SpellCategory;
import com.spellcraft.api.SpellCaster;
import com.spellcraft.api.SpellResult;
import com.spellcraft.api.magic.MagicElement;
import com.spellcraft.core.AbstractSpell;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.jetbrains.annotations.NotNull;

public class HealSpell extends AbstractSpell {

    private Location currentLocation;

    public HealSpell() {
        super(
                "Heal",
                "Restore health and remove negative effects",
                SpellCategory.HEALING,
                SpellCraftPlugin.getInstance().getConfig().getInt("spells.heal.magic-cost", 40),
                SpellCraftPlugin.getInstance().getConfig().getLong("spells.heal.cooldown", 10000),
                null,
                SpellCraftPlugin.getInstance().getConfig().getBoolean("spells.heal.enabled", true),
                "Right Click a Block while Sneaking"
        );
    }

    @Override
    protected SpellResult execute(SpellCaster caster) {
        Player player = caster.getPlayer();
        currentLocation = player.getLocation().clone();

        double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        player.setHealth(Math.min(player.getHealth() + SpellCraftPlugin.getInstance().getConfig().getDouble("spells.heal.heal-amount", 10.0), maxHealth));
        player.setFireTicks(0);
        player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
        player.getWorld().spawnParticle(Particle.HEART, currentLocation.add(0, 1, 0), 20, 0.5, 0.5, 0.5);
        player.playSound(currentLocation, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);

        return SpellResult.SUCCESS;
    }

    @Override
    public void progress() {}

    @Override
    protected void onLoad() {}

    @Override
    protected void onStop() {}

    @Override
    public boolean isSneakingAbility() {
        return true;
    }

    @Override
    public Action getAbilityActivationAction() {
        return Action.RIGHT_CLICK_BLOCK;
    }

    @Override
    public MagicElement getElement() {
        return MagicElement.WATER;
    }

    @Override
    public @NotNull Location getLocation() {
        return currentLocation != null ? currentLocation.clone() : new Location(Bukkit.getWorlds().getFirst(), 0, 0, 0);
    }
}
