package com.spellcraft.listeners;

import com.spellcraft.SpellCraftPlugin;
import com.spellcraft.api.Spell;
import com.spellcraft.api.SpellCaster;
import com.spellcraft.api.event.SpellPlayerDamageEvent;
import com.spellcraft.core.SpellCasterManager;
import com.spellcraft.util.ThreadUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    private final SpellCasterManager casterManager;
    private final SpellCraftPlugin plugin;

    public PlayerListener(SpellCraftPlugin plugin, SpellCasterManager casterManager) {
        this.plugin = plugin;
        this.casterManager = casterManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        SpellCaster caster = casterManager.getCaster(player);
        plugin.getLogger().info("SpellCaster for " + player.getName() + " loaded!");

        ThreadUtil.runGlobalTimer(() -> {
            if (plugin.getConfig().getBoolean("ui.show-magic-bar", true)) {
                plugin.getMagicBar().showForPlayer(player);
            }
        }, 20L, 100L);

        ThreadUtil.runGlobalLater(() -> {
            if (!caster.hasHouse()) {
                SpellCraftPlugin.getAdventure().player(player).sendMessage(
                        Component.text("You must choose a house!")
                                .color(NamedTextColor.RED)
                );
                SpellCraftPlugin.getAdventure().player(player).sendMessage(
                        Component.text("Use /house choose <name>")
                                .color(NamedTextColor.YELLOW)
                );
            }
        }, 40L);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        handleLeave(event.getPlayer());
    }

    @EventHandler
    public void onPlayerKick(PlayerKickEvent event) {
        handleLeave(event.getPlayer());
    }

    private void handleLeave(Player player) {
        SpellCaster caster = casterManager.getCasterIfLoaded(player.getUniqueId());
        if (caster != null) {
            plugin.getPlayerDataManager().save(caster);
            plugin.getLogger().info("SpellCaster for " + player.getName() + " saved!");
            casterManager.removeCaster(player.getUniqueId());
        }
    }

    @EventHandler
    public void onHotbarSwap(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        SpellCaster caster = casterManager.getCasterIfLoaded(player.getUniqueId());

        if (caster == null) {
            SpellCraftPlugin.getAdventure().player(player).sendActionBar(Component.empty());
            return;
        }

        Spell spell = caster.getSpellAtSlot(event.getNewSlot());
        if (spell == null) {
            SpellCraftPlugin.getAdventure().player(player).sendActionBar(Component.empty());
            return;
        }

        var element = spell.getElement();
        var color = element != null ? element.getColor() : NamedTextColor.WHITE;

        SpellCraftPlugin.getAdventure().player(player).sendActionBar(Component.text(spell.getName()).color(color));
    }

    @EventHandler
    public void onSpellDamage(SpellPlayerDamageEvent event) {
        var target = event.getTarget();
        var caster = event.getCaster();

        double finalHealth = target.getHealth() - event.getDamage();
        if (finalHealth > 0) return;

        Component message = Component.text()
                .append(Component.text(target.getName(), NamedTextColor.RED))
                .append(Component.text(" was slain by ", NamedTextColor.GRAY))
                .append(Component.text(caster.getName(), NamedTextColor.GOLD))
                .append(Component.text("'s ", NamedTextColor.GRAY))
                .append(Component.text(
                        event.getSpellName(),
                        event.getElement().getColor()
                ))
                .build();

        Bukkit.getServer().sendMessage(message);
        Bukkit.getConsoleSender().sendMessage(message);
    }
}
