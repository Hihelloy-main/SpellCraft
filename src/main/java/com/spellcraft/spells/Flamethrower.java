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
        playerloc = player.getLocation();

        Location origin = player.getEyeLocation();
        var direction = origin.getDirection().normalize();

        double damage = SpellCraftPlugin.getInstance().getConfig()
                .getDouble("spells.flamethrower.damage", 2.0);
        double particleRange = SpellCraftPlugin.getInstance().getConfig()
                .getDouble("spells.flamethrower.particle-range", 0.6);

        ThreadUtil.ensureLocationTimer(origin, () -> {

            // Always spawn flame forward
            Location point = origin.clone().add(direction.clone().multiply(1.2));
            pointloc = point;

            // Visuals ALWAYS happen
            ParticleEffect.FLAME.display(point, 20);

            // Damage ONLY if something is inside
            point.getWorld().getNearbyEntities(point, particleRange, particleRange, particleRange)
                    .forEach(entity -> {
                        if (!(entity instanceof LivingEntity living)) return;
                        if (!DamageHandler.isValidTarget(player, living)) return;

                        DamageHandler.damage(
                                player,
                                living,
                                damage,
                                getName(),
                                getElement()
                        );
                    });

        }, 1, 1);

        // Never fail due to missing target
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
