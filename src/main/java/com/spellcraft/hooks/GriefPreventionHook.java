package com.spellcraft.hooks;

import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.Claim;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public final class GriefPreventionHook {

    private static GriefPrevention gp;
    private static boolean enabled;

    private GriefPreventionHook() {}

    public static void init(Plugin plugin) {
        Plugin p = Bukkit.getPluginManager().getPlugin("GriefPrevention");
        if (p instanceof GriefPrevention) {
            gp = (GriefPrevention) p;
            enabled = true;
            plugin.getLogger().info("Hooked into GriefPrevention");
        } else {
            enabled = false;
        }
    }

    public static boolean isEnabled() {
        return enabled;
    }

    /**
     * Can a player modify blocks here using spells?
     */
    public static boolean canBuild(Player player, Location loc) {
        if (!enabled || player == null) return true;

        Claim claim = gp.dataStore.getClaimAt(loc, true, null);
        if (claim == null) return true;

        return claim.allowBuild(player, loc.getBlock().getType()) == null;
    }

    /**
     * Used for non player spells (environment, armorstands, temp blocks)
     */
    public static boolean canSpellAffect(Location loc, Player caster) {
        if (!enabled) return true;

        Claim claim = gp.dataStore.getClaimAt(loc, true, null);
        if (claim == null) return true;

        if (caster != null) {
            return claim.allowBuild(caster, loc.getBlock().getType()) == null;
        }

        return false;
    }

    public static boolean canSpellAffect(Block block, Player caster) {
        return canSpellAffect(block.getLocation(), caster);
    }
}
