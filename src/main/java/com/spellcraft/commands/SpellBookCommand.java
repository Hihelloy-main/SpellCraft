package com.spellcraft.commands;

import com.spellcraft.SpellCraftPlugin;
import com.spellcraft.api.Spell;
import com.spellcraft.api.SpellManager;
import com.spellcraft.core.SpellBookImpl;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SpellBookCommand implements CommandExecutor, TabCompleter {

    private final SpellCraftPlugin plugin;
    private final SpellManager spellManager;
    private final NamespacedKey spellBookKey;

    public SpellBookCommand(SpellCraftPlugin plugin, SpellManager spellManager, NamespacedKey spellBookKey) {
        this.plugin = plugin;
        this.spellManager = spellManager;
        this.spellBookKey = spellBookKey;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("spellcraft.command.spellbook")) {
            SpellCraftPlugin.getAdventure().sender(sender).sendMessage(Component.text("You don't have permission to use this command!", NamedTextColor.RED));
            return true;
        }

        if (args.length < 2) {
            SpellCraftPlugin.getAdventure().sender(sender).sendMessage(Component.text("Usage: /spellbook <create|give> <player> [spell1] [spell2] ...", NamedTextColor.RED));
            return true;
        }

        String action = args[0].toLowerCase();
        String playerName = args[1];

        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            SpellCraftPlugin.getAdventure().sender(sender).sendMessage(Component.text("Player not found: " + playerName, NamedTextColor.RED));
            return true;
        }

        List<String> spellNames = new ArrayList<>();
        for (int i = 2; i < args.length; i++) {
            spellNames.add(args[i]);
        }

        if (spellNames.isEmpty()) {
            SpellCraftPlugin.getAdventure().sender(sender).sendMessage(Component.text("You must specify at least one spell!", NamedTextColor.RED));
            return true;
        }

        switch (action) {
            case "create":
            case "give":
                giveSpellBook(sender, target, spellNames);
                break;
            default:
                SpellCraftPlugin.getAdventure().sender(sender).sendMessage(Component.text("Unknown action: " + action, NamedTextColor.RED));
                break;
        }

        return true;
    }

    private void giveSpellBook(CommandSender sender, Player target, List<String> spellNames) {
        SpellBookImpl spellBook = new SpellBookImpl("Ancient SpellBook", spellBookKey);

        List<String> validSpells = new ArrayList<>();
        List<String> invalidSpells = new ArrayList<>();

        for (String spellName : spellNames) {
            Optional<Spell> spellOpt = spellManager.getSpell(spellName);
            if (spellOpt.isPresent()) {
                spellBook.addSpell(spellOpt.get());
                validSpells.add(spellName);
            } else {
                invalidSpells.add(spellName);
            }
        }

        if (validSpells.isEmpty()) {
            SpellCraftPlugin.getAdventure().sender(sender).sendMessage(Component.text("No valid spells specified!", NamedTextColor.RED));
            return;
        }

        target.getInventory().addItem(spellBook.toItemStack());

        SpellCraftPlugin.getAdventure().sender(sender).sendMessage(Component.text("Gave SpellBook with " + validSpells.size() +
                " spell(s) to " + target.getName(), NamedTextColor.GREEN));

        if (!invalidSpells.isEmpty()) {
            SpellCraftPlugin.getAdventure().sender(sender).sendMessage(Component.text("Invalid spells: " + String.join(", ", invalidSpells), NamedTextColor.YELLOW));
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return List.of("create", "give").stream()
                .filter(s -> s.startsWith(args[0].toLowerCase()))
                .collect(Collectors.toList());
        }

        if (args.length == 2) {
            return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                .collect(Collectors.toList());
        }

        if (args.length >= 3) {
            return spellManager.getAllSpells().stream()
                .map(Spell::getName)
                .filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .collect(Collectors.toList());
        }

        return new ArrayList<>();
    }
}
