package com.spellcraft.spells;

import com.spellcraft.SpellCraftPlugin;
import com.spellcraft.api.SpellCategory;
import com.spellcraft.api.SpellCaster;
import com.spellcraft.api.SpellResult;
import com.spellcraft.api.magic.MagicElement;
import com.spellcraft.core.AbstractSpell;
import com.spellcraft.util.ThreadUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class FireballSpell extends AbstractSpell {

    private Location currentLocation;

    public FireballSpell() {
        super(
                "Fireball",
                "Launch a blazing fireball that explodes on impact",
                SpellCategory.COMBAT,
                SpellCraftPlugin.getInstance().getConfig().getInt("spells.fireball.magic-cost", 30),
                SpellCraftPlugin.getInstance().getConfig().getLong("spells.fireball.cooldown", 5000),
                SpellCraftPlugin.getInstance().getConfig().getDouble("spells.fireball.range", 50.0),
                SpellCraftPlugin.getInstance().getConfig().getBoolean("spells.fireball.enabled", true),
                "Left Click Air"
        );
    }

    @Override
    protected SpellResult execute(SpellCaster caster) {
        Player player = caster.getPlayer();
        Vector direction = player.getEyeLocation().getDirection();

        Fireball fireball = player.getWorld().spawn(player.getEyeLocation().add(direction.clone().multiply(2)), Fireball.class);
        fireball.setShooter(player);
        fireball.setVelocity(direction.multiply(SpellCraftPlugin.getInstance().getConfig().getDouble("spells.fireball.speed", 1.5)));
        fireball.setYield((float) SpellCraftPlugin.getInstance().getConfig().getDouble("spells.fireball.explosion-power", 2.0));
        fireball.setIsIncendiary(true);

        currentLocation = fireball.getLocation().clone();
        Location start = currentLocation.clone();

        ThreadUtil.ensureLocationTimer(fireball.getLocation(), () -> {
            if (!fireball.isValid() || fireball.isDead()) return;

            currentLocation = fireball.getLocation().clone();

            if (fireball.getLocation().distanceSquared(start) >= Math.pow(SpellCraftPlugin.getInstance().getConfig().getDouble("spells.fireball.range", 20.0), 2)) {
                fireball.remove();
            }
        }, 1L, 1L);

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
        return false;
    }

    @Override
    public Action getAbilityActivationAction() {
        return Action.LEFT_CLICK_AIR;
    }

    @Override
    public MagicElement getElement() {
        return MagicElement.FIRE;
    }

    @Override
    public @NotNull Location getLocation() {
        return currentLocation != null ? currentLocation.clone() : new Location(Bukkit.getWorlds().getFirst(), 0, 0, 0);
    }
}
