package com.spellcraft.spells;

import com.spellcraft.api.SpellCaster;
import com.spellcraft.api.SpellCategory;
import com.spellcraft.api.SpellResult;
import com.spellcraft.api.magic.MagicElement;
import com.spellcraft.core.AbstractSpell;
import com.spellcraft.util.TempBlock;
import com.spellcraft.util.TempFallingBlock;
import com.spellcraft.util.ThreadUtil;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.jetbrains.annotations.NotNull;

public class EarthBlock extends AbstractSpell {

    private Location currentLocation;

    public EarthBlock() {
        super("EarthBlock", "Spawn a single earth block", SpellCategory.UTILITY, 1, 0L, 50D, true, "Left Click Air");
    }

    @Override
    protected SpellResult execute(SpellCaster caster) {
        Player player = caster.getPlayer();
        currentLocation = player.getLocation().clone();

        TempBlock tb = new TempBlock(currentLocation.getBlock(), Material.GRASS_BLOCK);
        ThreadUtil.ensureLocationLater(tb.getBlock().getLocation(), tb::revert, 30 * 20);

        TempFallingBlock tfb = new TempFallingBlock(currentLocation, Material.BEDROCK.createBlockData(), player.getVelocity(), false);
        player.getLocation().getWorld().playSound(tfb.getEntity().getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.0f);

        ThreadUtil.ensureLocationLater(currentLocation, tfb::remove, 900 * 20);

        return SpellResult.SUCCESS;
    }

    @Override
    public void progress() {
    }

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
        return MagicElement.EARTH;
    }

    @Override
    public @NotNull Location getLocation() {
        return currentLocation != null ? currentLocation.clone() : new Location(Bukkit.getWorlds().getFirst(), 0, 0, 0);
    }
}
