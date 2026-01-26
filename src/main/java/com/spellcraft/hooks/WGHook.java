package com.spellcraft.hooks;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.Association;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.association.RegionAssociable;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;

public class WGHook {

    private static WorldGuardPlugin wgPlugin;
    public static final StateFlag SPELL_CAST_FLAG =
            new StateFlag("spell-cast", true);

    public static void init() {
        wgPlugin = (WorldGuardPlugin) Bukkit.getPluginManager().getPlugin("WorldGuard");
        if (wgPlugin == null) {
            Bukkit.getLogger().warning("[SpellCraft] WorldGuard not found! Regions will not be checked.");
            return;
        }

        try {
            WorldGuard.getInstance()
                    .getFlagRegistry()
                    .register(SPELL_CAST_FLAG);
        } catch (IllegalStateException ignored) {
            // Flag already registered
        }
    }

    public static boolean canCast(Player player, Location location) {
        if (wgPlugin == null) return true;
        if (player == null || location == null) return true;

        RegionContainer container =
                WorldGuard.getInstance().getPlatform().getRegionContainer();

        RegionQuery query = container.createQuery();

        ApplicableRegionSet regions =
                query.getApplicableRegions(BukkitAdapter.adapt(location));


        State spellState = regions.queryState(null, SPELL_CAST_FLAG);
        if (spellState == State.DENY) {
            return false;
        }

        LocalPlayer localPlayer = wgPlugin.wrapPlayer(player);
        State buildState = regions.queryState(localPlayer, Flags.BUILD);
        if (buildState == State.DENY) {
            return false;
        }

        return true;
    }
}
