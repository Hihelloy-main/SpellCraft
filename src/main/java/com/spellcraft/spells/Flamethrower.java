package com.spellcraft.spells;

import com.spellcraft.SpellCraftPlugin;
import com.spellcraft.api.SpellCaster;
import com.spellcraft.api.SpellCategory;
import com.spellcraft.api.SpellResult;
import com.spellcraft.api.magic.MagicElement;
import com.spellcraft.core.AbstractSpell;
import com.spellcraft.util.DamageHandler;
import com.spellcraft.util.ParticleEffect;
import com.spellcraft.util.ThreadUtil;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.jetbrains.annotations.NotNull;

public class Flamethrower extends AbstractSpell {

    private Location pointloc;
    private Location playerloc;

    public Flamethrower() {
        super(
                "Flamethrower",
                "Shoot flames at your enemies!",
                SpellCategory.COMBAT,
                SpellCraftPlugin.getInstance().getConfig().getInt("spells.flamethrower.magic-cost", 30),
                SpellCraftPlugin.getInstance().getConfig().getLong("spells.flamethrower.cooldown", 5000L),
                SpellCraftPlugin.getInstance().getConfig().getDouble("spells.flamethrower.range", 50.0),
                SpellCraftPlugin.getInstance().getConfig().getBoolean("spells.flamethrower.enabled", true),
                "Crouch in front of your enemy!"
        );
    }

    @Override
    protected SpellResult execute(SpellCaster caster) {
        Player player = caster.getPlayer();

        double maxRange = getRange();
        double step = 0.6;
        double hitRadius = SpellCraftPlugin.getInstance().getConfig()
                .getDouble("spells.flamethrower.hit-radius", 1.2);
        double damage = SpellCraftPlugin.getInstance().getConfig()
                .getDouble("spells.flamethrower.damage", 2.0);
        int fireTicks = SpellCraftPlugin.getInstance().getConfig()
                .getInt("spells.flamethrower.fire-ticks", 40);

        int maxDurationTicks = SpellCraftPlugin.getInstance().getConfig()
                .getInt("spells.flamethrower.duration-ticks", 60); // 3 seconds default

        final int[] livedTicks = {0};

        ThreadUtil.ensureLocationTimer(player.getEyeLocation(), () -> {

            // STOP CONDITIONS
            if (!player.isOnline() || player.isDead() || !player.isSneaking()) {
                remove();
                return;
            }

            if (livedTicks[0]++ > maxDurationTicks) {
                remove();
                return;
            }

            Location eye = player.getEyeLocation();
            var dir = eye.getDirection().normalize();
            playerloc = player.getLocation();

            for (double d = 0; d < maxRange; d += step) {
                Location point = eye.clone().add(dir.clone().multiply(d));
                pointloc = point;

                if (point.getBlock().getType().isSolid()) break;

                ParticleEffect.FLAME.display(point, 2);
                ParticleEffect.SMOKE_NORMAL.display(point, 1);

                point.getWorld().getNearbyEntities(point, hitRadius, hitRadius, hitRadius)
                        .forEach(entity -> {
                            if (!(entity instanceof LivingEntity living)) return;
                            if (living.equals(player)) return;
                            if (!DamageHandler.isValidTarget(player, living)) return;

                            DamageHandler.damage(player, living, damage, getName(), getElement());
                            living.setFireTicks(Math.max(living.getFireTicks(), fireTicks));
                        });
            }

        }, 0, 1);

        return SpellResult.SUCCESS;
    }


    @Override
    public void progress() { }

    @Override
    protected void onLoad() { }

    @Override
    protected void onStop() { }

    @Override
    public boolean isSneakingAbility() {
        return true;
    }

    @Override
    public Action getAbilityActivationAction() {
        return null;
    }

    @Override
    public MagicElement getElement() {
        return MagicElement.FIRE;
    }

    @Override
    public @NotNull Location getLocation() {
        return (pointloc == null) ? playerloc : pointloc;
    }
}
