package com.spellcraft.listeners;

import com.spellcraft.SpellCraftPlugin;
import com.spellcraft.api.Spell;
import com.spellcraft.api.SpellCaster;
import com.spellcraft.api.SpellResult;
import com.spellcraft.api.house.House;
import com.spellcraft.api.magic.MagicElement;
import com.spellcraft.core.SpellCasterManager;
import com.spellcraft.hooks.GriefPreventionHook;
import com.spellcraft.hooks.WGHook;
import com.spellcraft.util.HouseUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.*;

public class SpellCastListener implements Listener {

    private final SpellCraftPlugin plugin;
    private final SpellCasterManager casterManager;

    public SpellCastListener(SpellCraftPlugin plugin, SpellCasterManager casterManager) {
        this.plugin = plugin;
        this.casterManager = casterManager;
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteract(PlayerInteractEvent event) {

        Player player = event.getPlayer();

        if (player.getInventory().getItemInMainHand().getType() != Material.AIR) return;

        SpellCaster caster = casterManager.getCaster(player);

        int slot = player.getInventory().getHeldItemSlot();

        Spell spell = caster.getSpellAtSlot(slot);

        if (spell == null) return;

        Action required = spell.getAbilityActivationAction();

        if (required == null) return;

        if (event.getAction() != required) return;

        if (!validateCast(player, caster, spell, slot)) return;

        event.setCancelled(true);

        cast(player, caster, spell);
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSneakStart(PlayerToggleSneakEvent event) {

        if (!event.isSneaking()) return;

        Player player = event.getPlayer();

        if (player.getInventory().getItemInMainHand().getType() != Material.AIR) return;

        SpellCaster caster = casterManager.getCaster(player);

        int slot = player.getInventory().getHeldItemSlot();

        Spell spell = caster.getSpellAtSlot(slot);

        if (spell == null) return;

        if (spell.getAbilityActivationAction() != null) return;

        if (!validateCast(player, caster, spell, slot)) return;

        cast(player, caster, spell);
    }


    private boolean validateCast(Player player, SpellCaster caster, Spell spell, int expectedSlot) {

        if (caster.getSpellAtSlot(expectedSlot) != spell) return false;

        House house = caster.getHouse();

        MagicElement element = spell.getElement();

        if (house != null && element != null && !HouseUtil.canUse(house, element)) {

            SpellCraftPlugin.getAdventure().player(player)
                    .sendActionBar(Component.text(
                            "Your house cannot use this magic",
                            NamedTextColor.RED
                    ));

            return false;
        }


        if (Bukkit.getPluginManager().getPlugin("WorldGuard") != null) {

            if (!WGHook.canCast(player, player.getLocation())) {

                SpellCraftPlugin.getAdventure().player(player).sendMessage(
                        Component.text("You cannot cast ", NamedTextColor.RED)
                                .append(Component.text(spell.getName(), NamedTextColor.AQUA))
                                .append(Component.text(" here", NamedTextColor.RED))
                );

                return false;
            }
        }


        if (!GriefPreventionHook.canBuild(player, player.getLocation())) {

            SpellCraftPlugin.getAdventure().player(player).sendMessage(
                    Component.text("You cannot cast ", NamedTextColor.RED)
                            .append(Component.text(spell.getName(), spell.getElement().getColor()))
                            .append(Component.text(" here", NamedTextColor.RED))
            );

            return false;
        }


        return true;
    }


    private void cast(Player player, SpellCaster caster, Spell spell) {

        SpellResult result = caster.castSpell(spell);

        switch (result) {

            case SUCCESS -> {}


            case INSUFFICIENT_MAGIC -> {

                int cost = plugin.getPerkManager().modifyMagicCost(caster, spell);

                SpellCraftPlugin.getAdventure().player(player)
                        .sendMessage(Component.text(
                                "Need " + cost + " magic",
                                NamedTextColor.RED
                        ));
            }


            case ON_COOLDOWN -> {

                long cd = plugin.getPerkManager().modifyCooldown(caster, spell) / 1000;

                SpellCraftPlugin.getAdventure().player(player)
                        .sendMessage(Component.text(
                                "Cooldown " + cd + "s",
                                NamedTextColor.RED
                        ));
            }


            case NO_PERMISSION ->

                    SpellCraftPlugin.getAdventure().player(player)
                            .sendMessage(Component.text(
                                    "No permission",
                                    NamedTextColor.RED
                            ));


            case INVALID_TARGET ->

                    SpellCraftPlugin.getAdventure().player(player)
                            .sendMessage(Component.text(
                                    "Invalid target",
                                    NamedTextColor.RED
                            ));


            default ->

                    SpellCraftPlugin.getAdventure().player(player)
                            .sendMessage(Component.text(
                                    "Cast failed",
                                    NamedTextColor.RED
                            ));
        }
    }


    @EventHandler
    public void onSwap(PlayerSwapHandItemsEvent event) {

        Player player = event.getPlayer();

        if (!player.isSneaking()) return;

        if (player.getInventory().getItemInMainHand().getType() != Material.AIR) return;

        event.setCancelled(true);

        SpellCaster caster = casterManager.getCaster(player);

        int slot = player.getInventory().getHeldItemSlot();

        Spell spell = caster.getSpellAtSlot(slot);


        if (spell == null) {

            SpellCraftPlugin.getAdventure().player(player)
                    .sendMessage(Component.text(
                            "No spell bound",
                            NamedTextColor.RED
                    ));

            return;
        }


        int cost = plugin.getPerkManager().modifyMagicCost(caster, spell);

        long cooldown = plugin.getPerkManager().modifyCooldown(caster, spell) / 1000;


        SpellCraftPlugin.getAdventure().player(player)
                .sendMessage(Component.text(
                        spell.getName(),
                        NamedTextColor.AQUA
                ).decorate(TextDecoration.BOLD));


        SpellCraftPlugin.getAdventure().player(player)
                .sendMessage(Component.text(
                        spell.getDescription(),
                        NamedTextColor.GRAY
                ));


        SpellCraftPlugin.getAdventure().player(player)
                .sendMessage(Component.text(
                        "Magic " + cost + " | Cooldown " + cooldown + "s",
                        NamedTextColor.YELLOW
                ));
    }
}