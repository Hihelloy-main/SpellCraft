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
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

public class SpellCastListener implements Listener {

    private final SpellCraftPlugin plugin;
    private final SpellCasterManager casterManager;

    public SpellCastListener(SpellCraftPlugin plugin, SpellCasterManager casterManager) {
        this.plugin = plugin;
        this.casterManager = casterManager;
    }


    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        SpellCaster caster = casterManager.getCaster(player);

        int slot = player.getInventory().getHeldItemSlot();
        Spell spell = caster.getSpellAtSlot(slot);

        if (spell == null) return;

        // Skip sneak-channel spells here
        if (spell.getAbilityActivationAction() == null) return;

        if (event.getAction() != spell.getAbilityActivationAction()) return;
        if (player.getInventory().getItemInMainHand().getType() != Material.AIR) return;

        if (!canCastHere(player, caster, spell)) return;

        event.setCancelled(true);

        handleCastResult(player, caster, spell);
    }


    @EventHandler
    public void onSneakStart(PlayerToggleSneakEvent event) {
        if (!event.isSneaking()) return; // Only when starting sneak

        Player player = event.getPlayer();
        SpellCaster caster = casterManager.getCaster(player);

        int slot = player.getInventory().getHeldItemSlot();
        Spell spell = caster.getSpellAtSlot(slot);

        if (spell == null) return;

        // Only handle sneak-based spells
        if (spell.getAbilityActivationAction() != null) return;

        // if (player.getInventory().getItemInMainHand().getType() != Material.AIR) return;

        if (!canCastHere(player, caster, spell)) return;

        handleCastResult(player, caster, spell);
    }


    private boolean canCastHere(Player player, SpellCaster caster, Spell spell) {
        House house = caster.getHouse();
        MagicElement element = spell.getElement();

        if (house != null && element != null && !HouseUtil.canUse(house, element)) {
            SpellCraftPlugin.getAdventure().player(player)
                    .sendActionBar(Component.text("Your house cannot use this magic", NamedTextColor.RED));
            return false;
        }

        if (Bukkit.getPluginManager().getPlugin("WorldGuard") != null) {
            if (!WGHook.canCast(player, player.getLocation())) {
                SpellCraftPlugin.getAdventure().player(player).sendMessage(
                        Component.text("You cannot cast ", NamedTextColor.RED)
                                .append(Component.text(spell.getName(), NamedTextColor.AQUA))
                                .append(Component.text(" here (WorldGuard)", NamedTextColor.RED))
                );
                return false;
            }
        }

        if (!GriefPreventionHook.canBuild(player, player.getLocation())) {
            SpellCraftPlugin.getAdventure().player(player).sendMessage(
                    Component.text("You cannot cast ", NamedTextColor.RED)
                            .append(Component.text(spell.getName(), spell.getElement().getColor()))
                            .append(Component.text(" here (Claimed land)", NamedTextColor.RED))
            );
            return false;
        }

        return true;
    }


    private void handleCastResult(Player player, SpellCaster caster, Spell spell) {
        SpellResult result = caster.castSpell(spell);

        switch (result) {
            case SUCCESS -> {}

            case INSUFFICIENT_MAGIC -> {
                int cost = plugin.getPerkManager().modifyMagicCost(caster, spell);
                SpellCraftPlugin.getAdventure().player(player)
                        .sendMessage(Component.text("Not enough magic! Need ", NamedTextColor.RED)
                                .append(Component.text(cost + " magic.", NamedTextColor.YELLOW)));
            }

            case ON_COOLDOWN -> {
                long cd = plugin.getPerkManager().modifyCooldown(caster, spell) / 1000;
                SpellCraftPlugin.getAdventure().player(player)
                        .sendMessage(Component.text("Spell on cooldown! ", NamedTextColor.RED)
                                .append(Component.text(cd + "s remaining.", NamedTextColor.YELLOW)));
            }

            case NO_PERMISSION ->
                    SpellCraftPlugin.getAdventure().player(player)
                            .sendMessage(Component.text("You don't have permission to cast this spell!", NamedTextColor.RED));

            case INVALID_TARGET ->
                    SpellCraftPlugin.getAdventure().player(player)
                            .sendMessage(Component.text("Invalid target for this spell!", NamedTextColor.RED));

            default ->
                    SpellCraftPlugin.getAdventure().player(player)
                            .sendMessage(Component.text("Failed to cast spell!", NamedTextColor.RED));
        }
    }

    @EventHandler
    public void onPlayerSwapHandItems(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();

        if (player.isSneaking() && player.getInventory().getItemInMainHand().getType() == Material.AIR) {
            event.setCancelled(true);

            SpellCaster caster = casterManager.getCaster(player);
            int slot = player.getInventory().getHeldItemSlot();
            Spell spell = caster.getSpellAtSlot(slot);

            if (spell != null) {
                int cost = plugin.getPerkManager().modifyMagicCost(caster, spell);
                long cooldown = plugin.getPerkManager().modifyCooldown(caster, spell) / 1000;

                SpellCraftPlugin.getAdventure().player(player).sendMessage(
                        Component.text(spell.getName(), NamedTextColor.AQUA)
                                .decorate(TextDecoration.BOLD));

                SpellCraftPlugin.getAdventure().player(player)
                        .sendMessage(Component.text(spell.getDescription(), NamedTextColor.GRAY));

                SpellCraftPlugin.getAdventure().player(player).sendMessage(
                        Component.text("Magic: ", NamedTextColor.YELLOW)
                                .append(Component.text(cost + "", NamedTextColor.AQUA))
                                .append(Component.text(" | Cooldown: ", NamedTextColor.YELLOW))
                                .append(Component.text(cooldown + "s", NamedTextColor.AQUA))
                );
            } else {
                SpellCraftPlugin.getAdventure().player(player)
                        .sendMessage(Component.text("No spell bound to slot " + (slot + 1), NamedTextColor.RED));
            }
        }
    }
}
