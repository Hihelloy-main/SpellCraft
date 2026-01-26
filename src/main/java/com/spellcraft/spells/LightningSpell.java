package com.spellcraft.spells;

import com.spellcraft.SpellCraftPlugin;
import com.spellcraft.api.SpellCategory;
import com.spellcraft.api.SpellCaster;
import com.spellcraft.api.SpellResult;
import com.spellcraft.api.magic.MagicElement;
import com.spellcraft.core.AbstractSpell;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.util.RayTraceResult;
import org.jetbrains.annotations.NotNull;

public class LightningSpell extends AbstractSpell {

    private Location currentLocation;

    public LightningSpell() {
        super(
                "Lightning",
                "Call down a lightning bolt at your target location",
                SpellCategory.ELEMENTAL,
                SpellCraftPlugin.getInstance().getConfig().getInt("spells.lightning.magic-cost", 35),
                SpellCraftPlugin.getInstance().getConfig().getLong("spells.lightning.cooldown", 8000),
                SpellCraftPlugin.getInstance().getConfig().getDouble("spells.lightning.range", 40.0),
                SpellCraftPlugin.getInstance().getConfig().getBoolean("spells.lightning.enabled", true),
                "Left Click while Sneaking"
        );
    }

    @Override
    protected SpellResult execute(SpellCaster caster) {
        Player player = caster.getPlayer();

        RayTraceResult result = player.rayTraceBlocks(range);
        if (result == null || result.getHitBlock() == null) return SpellResult.INVALID_TARGET;

        Block targetBlock = result.getHitBlock();
        currentLocation = targetBlock.getLocation().clone().add(0, 1, 0);

        player.getWorld().strikeLightning(currentLocation);

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
        return Action.LEFT_CLICK_AIR;
    }

    @Override
    public MagicElement getElement() {
        return MagicElement.LIGHTNING;
    }

    @Override
    public @NotNull Location getLocation() {
        return currentLocation != null ? currentLocation.clone() : new Location(Bukkit.getWorlds().getFirst(), 0, 0, 0);
    }
}
