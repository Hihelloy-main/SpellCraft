package com.spellcraft.spells;

import com.spellcraft.SpellCraftPlugin;
import com.spellcraft.api.SpellCategory;
import com.spellcraft.api.SpellCaster;
import com.spellcraft.api.SpellResult;
import com.spellcraft.api.magic.MagicElement;
import com.spellcraft.core.AbstractSpell;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.util.RayTraceResult;
import org.jetbrains.annotations.NotNull;

public class TeleportSpell extends AbstractSpell {

    private Location currentLocation;

    public TeleportSpell() {
        super(
                "Teleport",
                "Instantly teleport to the location you're looking at",
                SpellCategory.TRANSPORTATION,
                SpellCraftPlugin.getInstance().getConfig().getInt("spells.teleport.magic-cost", 50),
                SpellCraftPlugin.getInstance().getConfig().getLong("spells.teleport.cooldown", 15000),
                SpellCraftPlugin.getInstance().getConfig().getDouble("spells.teleport.range", 30.0),
                SpellCraftPlugin.getInstance().getConfig().getBoolean("spells.teleport.enabled", true),
                "Left Click Air"
        );
    }

    @Override
    protected SpellResult execute(SpellCaster caster) {
        Player player = caster.getPlayer();

        RayTraceResult result = player.rayTraceBlocks(range);
        if (result == null || result.getHitBlock() == null) return SpellResult.INVALID_TARGET;

        Block targetBlock = result.getHitBlock();
        Location teleportLocation = targetBlock.getLocation().clone().add(0, 1, 0);

        if (teleportLocation.getBlock().getType() != Material.AIR ||
                teleportLocation.clone().add(0, 1, 0).getBlock().getType() != Material.AIR) {
            return SpellResult.INVALID_TARGET;
        }

        teleportLocation.setYaw(player.getLocation().getYaw());
        teleportLocation.setPitch(player.getLocation().getPitch());

        player.getWorld().spawnParticle(Particle.PORTAL, player.getLocation().add(0, 1, 0), 50, 0.5, 0.5, 0.5);
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);

        player.teleport(teleportLocation);

        player.getWorld().spawnParticle(Particle.PORTAL, teleportLocation.add(0, 1, 0), 50, 0.5, 0.5, 0.5);
        player.playSound(teleportLocation, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);

        currentLocation = teleportLocation.clone();

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
        return MagicElement.VOID;
    }

    @Override
    public @NotNull Location getLocation() {
        return currentLocation != null ? currentLocation.clone() : new Location(Bukkit.getWorlds().getFirst(), 0, 0, 0);
    }
}
