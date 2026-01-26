package com.spellcraft.ui;

import com.spellcraft.SpellCraftPlugin;
import com.spellcraft.api.SpellCaster;
import com.spellcraft.core.SpellCasterManager;
import com.spellcraft.util.ThreadUtil;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Handles the display and automatic updating of Magic Bars for players.
 * <p>
 * Each player has a boss bar representing their current magic. Bars are updated
 * on a configurable interval and automatically regenerate magic based on configuration.
 */
public class MagicBar {

    /** Reference to the main plugin instance. */
    private final SpellCraftPlugin plugin;

    /** Manager responsible for retrieving SpellCaster objects for players. */
    private final SpellCasterManager casterManager;

    /** Map of player UUIDs to their current boss bar. */
    private final Map<UUID, BossBar> magicBars;

    /** Task responsible for updating the bars each tick interval. */
    private ThreadUtil.ThreadTask updateTask;

    /** Task responsible for regenerating magic periodically. */
    private ThreadUtil.ThreadTask regenTask;

    /**
     * Constructs a MagicBar manager.
     *
     * @param plugin the plugin instance
     * @param casterManager manager to retrieve player SpellCasters
     */
    public MagicBar(SpellCraftPlugin plugin, SpellCasterManager casterManager) {
        this.plugin = plugin;
        this.casterManager = casterManager;
        this.magicBars = new HashMap<>();
    }

    /**
     * Starts the MagicBar system:
     * <ul>
     *     <li>Schedules a repeating task to update all player bars.</li>
     *     <li>Schedules a repeating task to regenerate magic for all players.</li>
     * </ul>
     */
    public void start() {
        int updateInterval = plugin.getConfig().getInt("magic.update-interval", 10);
        int regenInterval = plugin.getConfig().getInt("magic.regen-interval", 40);
        int regenAmount = plugin.getConfig().getInt("magic.regen-amount", 2);

        // Update the boss bars for all online players
        updateTask = ThreadUtil.runGlobalTimer(() -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                SpellCaster caster = casterManager.getCaster(player);
                showForPlayer(player, caster);
            }
        }, 0L, updateInterval);

        // Regenerate magic for all online players
        regenTask = ThreadUtil.runGlobalTimer(() -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                SpellCaster caster = casterManager.getCaster(player);
                int bonus = plugin.getPerkManager().getBonusRegen(caster);
                caster.regenerateMagic(regenAmount + bonus);
            }
        }, 0L, regenInterval);
    }

    /**
     * Stops all MagicBar functionality:
     * <ul>
     *     <li>Cancels the update and regeneration tasks.</li>
     *     <li>Hides all active boss bars from players.</li>
     *     <li>Clears all internal state.</li>
     * </ul>
     */
    public void stop() {
        if (updateTask != null) updateTask.cancel();
        if (regenTask != null) regenTask.cancel();

        for (Player player : Bukkit.getOnlinePlayers()) {
            BossBar bar = magicBars.get(player.getUniqueId());
            if (bar != null) SpellCraftPlugin.getAdventure().player(player).hideBossBar(bar);
        }
        magicBars.clear();
    }

    /**
     * Updates the MagicBar display for a specific player.
     *
     * @param player the player to update
     */
    public void showForPlayer(Player player) {
        SpellCaster caster = casterManager.getCaster(player);
        if (caster != null) {
            showForPlayer(player, caster);
        }
    }

    /**
     * Updates the MagicBar display for a player with a specific caster.
     * <p>
     * Handles creating a new bar if none exists, updating the progress, color,
     * and display name.
     *
     * @param player the player to update
     * @param caster the SpellCaster representing the player's magic
     */
    private void showForPlayer(Player player, SpellCaster caster) {
        BossBar bar = magicBars.get(player.getUniqueId());

        // Create a new bar if the player does not already have one
        if (bar == null) {
            bar = BossBar.bossBar(
                    Component.text("Magic", NamedTextColor.LIGHT_PURPLE),
                    1.0f,
                    BossBar.Color.BLUE,
                    BossBar.Overlay.PROGRESS
            );
            magicBars.put(player.getUniqueId(), bar);
            SpellCraftPlugin.getAdventure().player(player).showBossBar(bar);
        }

        // Update progress
        float progress = (float) caster.getMagic() / caster.getMaxMagic();
        bar.progress(Math.max(0.0f, Math.min(1.0f, progress)));

        // Update the bar's display name
        bar.name(Component.text(
                "Magic: " + caster.getMagic() + " / " + caster.getMaxMagic(),
                NamedTextColor.AQUA
        ));

        // Change the bar color based on magic percentage
        if (progress > 0.5f) {
            bar.color(BossBar.Color.BLUE);
        } else if (progress > 0.25f) {
            bar.color(BossBar.Color.YELLOW);
        } else {
            bar.color(BossBar.Color.RED);
        }
    }
}
