package com.spellcraft.commands;

import com.spellcraft.SpellCraftPlugin;
import com.spellcraft.api.Spell;
import com.spellcraft.api.SpellCaster;
import com.spellcraft.api.SpellCategory;
import com.spellcraft.api.SpellManager;
import com.spellcraft.api.magic.MagicElement;
import com.spellcraft.core.SpellCasterManager;
import com.spellcraft.core.SpellManagerImpl;
import com.spellcraft.ui.MagicBar;
import com.spellcraft.util.ThreadUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SpellCommand implements CommandExecutor, TabCompleter {

    private final SpellManager spellManager;
    private final SpellCasterManager casterManager;
    private final SpellCraftPlugin plugin;

    public SpellCommand(SpellCraftPlugin plugin,
                        SpellManager spellManager,
                        SpellCasterManager casterManager) {
        this.plugin = plugin;
        this.spellManager = spellManager;
        this.casterManager = casterManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            SpellCraftPlugin.getAdventure().sender(sender).sendMessage(Component.text("This command can only be used by players!", NamedTextColor.RED));
            return true;
        }

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "list" -> listSpells(player, args);
            case "help" -> {
                if (args.length < 2) {
                    SpellCraftPlugin.getAdventure().player(player).sendMessage(Component.text("Usage: /spell help <spell>", NamedTextColor.RED));
                    return true;
                }
                showSpellInfo(player, args[1]);
            }
            case "learned" -> listLearnedSpells(player);
            case "bound" -> listBoundSpells(player);
            case "reload" -> reloadSpellCraft(player);
            case "display" -> {
                if (args.length < 2) {
                    SpellCraftPlugin.getAdventure().player(player).sendMessage(Component.text("Usage: /spell display <elementName>"));
                    return true;
                }
                displayMovesinElementToSender(MagicElement.of(args[1]), sender);
            }
            default -> sendHelp(player);
        }

        return true;
    }



    private void reloadSpellCraft(Player player) {
        if (!player.hasPermission("spellcraft.admin.reload")) {
            SpellCraftPlugin.getAdventure().player(player).sendMessage(Component.text("You don't have permission to do that!", NamedTextColor.RED));
            return;
        }


        casterManager.saveAll();


        plugin.reloadConfig();
        plugin.reloadPerksConfig();


        plugin.getMagicBar().stop();
        plugin.getMagicBar().start();

        plugin.registerSpells();

        plugin.getServer().getOnlinePlayers()
                .forEach(plugin.getMagicBar()::showForPlayer);

        SpellCraftPlugin.getAdventure().player(player).sendMessage(Component.text("SpellCraft reloaded successfully.", NamedTextColor.GREEN));
        plugin.getLogger().info("SpellCraft fully reloaded.");
    }



    private void sendHelp(Player player) {
        SpellCraftPlugin.getAdventure().player(player).sendMessage(Component.text("=== SpellCraft Commands ===", NamedTextColor.GOLD));
        SpellCraftPlugin.getAdventure().player(player).sendMessage(Component.text("/spell list [category]", NamedTextColor.YELLOW).append(Component.text(" - List spells", NamedTextColor.GRAY)));
        SpellCraftPlugin.getAdventure().player(player).sendMessage(Component.text("/spell info <spell>", NamedTextColor.YELLOW).append(Component.text(" - Spell info", NamedTextColor.GRAY)));
        SpellCraftPlugin.getAdventure().player(player).sendMessage(Component.text("/spell learned", NamedTextColor.YELLOW).append(Component.text(" - Learned spells", NamedTextColor.GRAY)));
        SpellCraftPlugin.getAdventure().player(player).sendMessage(Component.text("/spell bound", NamedTextColor.YELLOW).append(Component.text(" - Bound spells", NamedTextColor.GRAY)));
        SpellCraftPlugin.getAdventure().player(player).sendMessage(Component.text("/spell reload", NamedTextColor.YELLOW).append(Component.text(" - Reload config", NamedTextColor.GRAY)));
    }



    private void listSpells(Player player, String[] args) {
        List<Spell> spells;

        if (args.length > 1) {
            try {
                SpellCategory category = SpellCategory.valueOf(args[1].toUpperCase());
                spells = spellManager.getSpellsByCategory(category).stream().toList();
                SpellCraftPlugin.getAdventure().player(player).sendMessage(Component.text("=== " + category.getDisplayName() + " Spells ===", NamedTextColor.GOLD));
            } catch (IllegalArgumentException e) {
                SpellCraftPlugin.getAdventure().player(player).sendMessage(Component.text("Invalid category!", NamedTextColor.RED));
                return;
            }
        } else {
            spells = spellManager.getAllSpells().stream().toList();
            SpellCraftPlugin.getAdventure().player(player).sendMessage(Component.text("=== All Spells ===", NamedTextColor.GOLD));
        }

        if (spells.isEmpty()) {
            SpellCraftPlugin.getAdventure().player(player).sendMessage(Component.text("No spells found.", NamedTextColor.GRAY));
            return;
        }

        spells.forEach(spell ->
        SpellCraftPlugin.getAdventure().player(player).sendMessage(Component.text(spell.getName(), NamedTextColor.AQUA).append(Component.text(" - " + spell.getCategory().getDisplayName(), NamedTextColor.GRAY))));
    }

    private void showSpellInfo(Player player, String spellName) {
        spellManager.getSpell(spellName).ifPresentOrElse(spell -> {
                    SpellCraftPlugin.getAdventure().player(player).sendMessage(Component.text("=== " + (spell.getName() != null ? spell.getName() : "null") + " ===", NamedTextColor.GOLD));
                    SpellCraftPlugin.getAdventure().player(player).sendMessage(Component.text(spell.getDescription() != null ? spell.getDescription() : "null", NamedTextColor.GRAY));
                    SpellCraftPlugin.getAdventure().player(player).sendMessage(Component.text("Instructions: ", NamedTextColor.GRAY).append(Component.text(spell.getInstructions() != null ? spell.getInstructions() : "null", NamedTextColor.YELLOW).decorate(TextDecoration.UNDERLINED)));
                    SpellCraftPlugin.getAdventure().player(player).sendMessage(Component.text("Category: " + (spell.getCategory() != null ? spell.getCategory().getDisplayName() : "null"), NamedTextColor.YELLOW));
                    SpellCraftPlugin.getAdventure().player(player).sendMessage(Component.text("Magic Cost: " + (spell.getMagicCost() != null ? spell.getMagicCost() : "null"), NamedTextColor.YELLOW));
                    SpellCraftPlugin.getAdventure().player(player).sendMessage(Component.text("Cooldown: " + (spell.getCooldown() != null ? (spell.getCooldown() / 1000) + "s" : "null"), NamedTextColor.YELLOW));
                    SpellCraftPlugin.getAdventure().player(player).sendMessage(Component.text("Range: " + (spell.getRange() != null ? spell.getRange() : "null"), NamedTextColor.YELLOW));
                }, () ->
                SpellCraftPlugin.getAdventure().player(player).sendMessage(Component.text("Spell not found: " + spellName, NamedTextColor.RED))
        );
    }

    private void displayMovesinElementToSender(MagicElement element, CommandSender sender) {
        for (Spell spell : SpellCraftPlugin.getInstance().getSpellManagerImpl().getAllSpells()) {
            if (spell.getElement().equals(element)) {
                SpellCraftPlugin.getAdventure().sender(sender).sendMessage(Component.text("=== " + element.getName() + " Spells" + " ===").color(spell.getElement().getColor()));
                SpellCraftPlugin.getAdventure().sender(sender).sendMessage(Component.text(spell.getName()).color(spell.getElement().getColor()));
            }
        }
    }

    private void listLearnedSpells(Player player) {
        SpellCaster caster = casterManager.getCaster(player);
        List<Spell> learned = caster.getLearnedSpells();

        SpellCraftPlugin.getAdventure().player(player).sendMessage(Component.text("=== Learned Spells ===", NamedTextColor.GOLD));

        if (learned.isEmpty()) {
            SpellCraftPlugin.getAdventure().player(player).sendMessage(Component.text("You haven't learned any spells.", NamedTextColor.GRAY));
            return;
        }

        learned.forEach(spell ->
                SpellCraftPlugin.getAdventure().player(player).sendMessage(Component.text(spell.getName(), NamedTextColor.AQUA).append(Component.text(" - "
                        + spell.getCategory().getDisplayName(), NamedTextColor.GRAY)))
        );
    }

    private void listBoundSpells(Player player) {
        SpellCaster caster = casterManager.getCaster(player);
        Spell[] bound = caster.getBoundSpells();

        SpellCraftPlugin.getAdventure().player(player).sendMessage(Component.text("=== Bound Spells ===", NamedTextColor.GOLD));
        for (int i = 0; i < bound.length; i++) {
            if (bound[i] != null) {
                SpellCraftPlugin.getAdventure().player(player).sendMessage(Component.text("Slot " + (i + 1)
                        + ": ", NamedTextColor.YELLOW).append(Component.text(bound[i].getName(), NamedTextColor.AQUA)));
            }
        }
    }



    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return List.of("list", "help", "learned", "bound", "reload", "display")
                    .stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("list")) {
            return List.of(SpellCategory.values()).stream()
                    .map(c -> c.name().toLowerCase())
                    .filter(s -> s.startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("help")) {
            return spellManager.getAllSpells().stream()
                    .map(Spell::getName)
                    .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("display")) {
            return MagicElement.values().values().stream()
                    .map(MagicElement::getName)
                    .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return new ArrayList<>();
    }
}
