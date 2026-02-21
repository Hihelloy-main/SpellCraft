package com.spellcraft.spells;

import com.spellcraft.SpellCraftPlugin;
import com.spellcraft.api.SpellCaster;
import com.spellcraft.api.SpellCategory;
import com.spellcraft.api.SpellResult;
import com.spellcraft.api.magic.MagicElement;
import com.spellcraft.core.AbstractSpell;
import com.spellcraft.util.HouseUtil;
import com.spellcraft.util.ParticleEffect;
import com.spellcraft.util.ThreadUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class AirJets extends AbstractSpell {

    private Location currentLoc;
    private Location playerLoc;

    public AirJets() {
        super(
                "AirJets",
                "Use powerful air streams to propel yourself through the sky",
                SpellCategory.TRANSPORTATION,
                SpellCraftPlugin.getInstance().getConfig().getInt("spells.airjets.magic-cost", 30),
                SpellCraftPlugin.getInstance().getConfig().getLong("spells.airjets.cooldown", 7000L),
                SpellCraftPlugin.getInstance().getConfig().getDouble("spells.airjets.range", 100.0),
                SpellCraftPlugin.getInstance().getConfig().getBoolean("spells.airjets.enabled", true),
                "Sneak to ride a jet of air!"
        );
    }

    @Override
    protected SpellResult execute(SpellCaster caster) {
        Player player = caster.getPlayer();

        double speed = SpellCraftPlugin.getInstance().getConfig().getDouble("spells.airjets.speed", 1.5);
        double maxDistance = getRange();

        Location start = player.getLocation().clone();
        playerLoc = start;

        ThreadUtil.ensureLocationTimer(start, () -> {

            if (!player.isOnline() || player.isDead() || !HouseUtil.canUse(caster.getHouse(), getElement())) {
                remove();
                return;
            }

            // Stop if player stops sneaking
            if (!player.isSneaking()) {
                remove();
                return;
            }

            // Stop if exceeded range
            if (player.getLocation().distanceSquared(start) > maxDistance * maxDistance) {
                remove();
                return;
            }

            // Propel player in look direction
            Vector direction = player.getEyeLocation().getDirection().normalize();
            player.setVelocity(direction.multiply(speed));

            // Particles under player
            Location below = player.getLocation().clone().subtract(0, 1, 0);
            currentLoc = below;
            playerLoc = player.getLocation();

            ParticleEffect.CLOUD.display(below, 25);
            ParticleEffect.SMOKE_NORMAL.display(below, 10);

        }, 0, 1);

        return SpellResult.SUCCESS;
    }

    @Override public void progress() {}
    @Override protected void onLoad() {}
    @Override protected void onStop() {}

    @Override public boolean isSneakingAbility() { return true; }
    @Override public Action getAbilityActivationAction() { return null; }
    @Override public MagicElement getElement() { return MagicElement.AIR; }

    @Override
    public @NotNull Location getLocation() {
        return currentLoc == null ? playerLoc : currentLoc;
    }
}
