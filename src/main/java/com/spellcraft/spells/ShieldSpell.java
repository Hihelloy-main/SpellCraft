package com.spellcraft.spells;

import com.spellcraft.SpellCraftPlugin;
import com.spellcraft.api.SpellCategory;
import com.spellcraft.api.SpellCaster;
import com.spellcraft.api.SpellResult;
import com.spellcraft.api.magic.MagicElement;
import com.spellcraft.core.AbstractSpell;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

public class ShieldSpell extends AbstractSpell {

    private Location currentLocation;

    public ShieldSpell() {
        super(
                "Shield",
                "Create a magical barrier that absorbs damage",
                SpellCategory.PROTECTION,
                SpellCraftPlugin.getInstance().getConfig().getInt("spells.shield.magic-cost", 25),
                SpellCraftPlugin.getInstance().getConfig().getLong("spells.shield.cooldown", 12000),
                null,
                SpellCraftPlugin.getInstance().getConfig().getBoolean("spells.shield.enabled", true),
                "Right Click a Block while Sneaking"
        );
    }

    @Override
    protected SpellResult execute(SpellCaster caster) {
        Player player = caster.getPlayer();
        currentLocation = player.getLocation().clone();

        player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE,
                SpellCraftPlugin.getInstance().getConfig().getInt("spells.shield.duration", 200), 1, false, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION,
                SpellCraftPlugin.getInstance().getConfig().getInt("spells.shield.duration", 200), 1, false, true));

        player.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, currentLocation.add(0, 1, 0), 100, 1.0, 1.0, 1.0);
        player.playSound(currentLocation, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 1.0f);

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
        return MagicElement.NATURE;
    }

    @Override
    public @NotNull Location getLocation() {
        return currentLocation != null ? currentLocation.clone() : new Location(Bukkit.getWorlds().getFirst(), 0, 0, 0);
    }
}
