package com.spellcraft.commands;

import com.spellcraft.SpellCraftPlugin;
import com.spellcraft.api.Spell;
import com.spellcraft.api.SpellManager;
import com.spellcraft.core.SpellBookImpl;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class SpellBookCommand implements CommandExecutor, TabCompleter {

    private final SpellCraftPlugin plugin;
    private final SpellManager spellManager;
    private final NamespacedKey spellBookKey;

    public SpellBookCommand(
            SpellCraftPlugin plugin,
            SpellManager spellManager,
            NamespacedKey spellBookKey
    ) {
        this.plugin = plugin;
        this.spellManager = spellManager;
        this.spellBookKey = spellBookKey;
    }



    @Override
    public boolean onCommand(
            CommandSender sender,
            Command command,
            String label,
            String[] args
    ) {

        if (!sender.hasPermission("spellcraft.admin.*")
                && !sender.hasPermission("spellcraft.command.spellbook")) {

            SpellCraftPlugin.getAdventure().sender(sender)
                    .sendMessage(Component.text(
                            "You don't have permission to use this command!",
                            NamedTextColor.RED));

            return true;
        }


        if (args.length < 2) {

            SpellCraftPlugin.getAdventure().sender(sender)
                    .sendMessage(Component.text(
                            "Usage: /spellbook <create|give> <player> [spells]",
                            NamedTextColor.RED));

            return true;
        }


        String action =
                args[0].toLowerCase();


        // PERMISSION BY ACTION

        if (action.equals("create")) {

            if (!sender.hasPermission("spellcraft.admin.*")
                    && !sender.hasPermission("spellcraft.spellbook.craft")) {

                sender.sendMessage("No permission.");

                return true;
            }
        }


        if (action.equals("give")) {

            if (!sender.hasPermission("spellcraft.admin.*")
                    && !sender.hasPermission("spellcraft.spellbook.give")) {

                sender.sendMessage("No permission.");

                return true;
            }
        }



        String playerName =
                args[1];


        Player target =
                Bukkit.getPlayer(playerName);


        if (target == null) {

            SpellCraftPlugin.getAdventure().sender(sender)
                    .sendMessage(Component.text(
                            "Player not found: " + playerName,
                            NamedTextColor.RED));

            return true;
        }



        List<String> spellNames =
                new ArrayList<>();


        for (int i = 2; i < args.length; i++) {

            spellNames.add(args[i]);
        }



        if (spellNames.isEmpty()) {

            sender.sendMessage("You must specify at least one spell.");

            return true;
        }



        giveSpellBook(sender, target, spellNames);

        return true;
    }




    private void giveSpellBook(
            CommandSender sender,
            Player target,
            List<String> spellNames
    ) {


        SpellBookImpl spellBook =
                new SpellBookImpl(
                        "Ancient SpellBook",
                        spellBookKey);


        List<String> valid =
                new ArrayList<>();


        List<String> invalid =
                new ArrayList<>();


        for (String spellName : spellNames) {

            Optional<Spell> spellOpt =
                    spellManager.getSpell(spellName);


            if (spellOpt.isPresent()) {

                Spell spell =
                        spellOpt.get();


                String perm =
                        "spellcraft.spell."
                                + spell.getName()
                                .toLowerCase();


                if (sender.hasPermission("spellcraft.admin.*")
                        || sender.hasPermission("spellcraft.spell.*")
                        || sender.hasPermission(perm)) {

                    spellBook.addSpell(spell);

                    valid.add(spellName);

                } else {

                    invalid.add(spellName + " (no permission)");

                }

            } else {

                invalid.add(spellName);
            }
        }



        if (valid.isEmpty()) {

            sender.sendMessage("No valid spells.");

            return;
        }



        target.getInventory()
                .addItem(
                        spellBook.toItemStack());



        SpellCraftPlugin.getAdventure().sender(sender)
                .sendMessage(Component.text(
                        "Gave SpellBook with "
                                + valid.size()
                                + " spell(s) to "
                                + target.getName(),
                        NamedTextColor.GREEN));



        if (!invalid.isEmpty()) {

            sender.sendMessage(
                    "Invalid: "
                            + String.join(
                            ", ",
                            invalid));
        }
    }




    @Override
    public List<String> onTabComplete(
            CommandSender sender,
            Command command,
            String alias,
            String[] args
    ) {


        if (!sender.hasPermission("spellcraft.command.spellbook"))
            return List.of();



        if (args.length == 1) {

            return List.of("create", "give")
                    .stream()

                    .filter(s ->
                            s.startsWith(
                                    args[0]
                                            .toLowerCase()))

                    .collect(Collectors.toList());
        }



        if (args.length == 2) {

            return Bukkit.getOnlinePlayers()
                    .stream()

                    .map(Player::getName)

                    .filter(name ->
                            name.toLowerCase()
                                    .startsWith(
                                            args[1]
                                                    .toLowerCase()))

                    .collect(Collectors.toList());
        }



        if (args.length >= 3) {


            return spellManager.getAllSpells()
                    .stream()

                    .map(Spell::getName)

                    .filter(name ->

                            sender.hasPermission("spellcraft.admin.*")

                                    ||

                                    sender.hasPermission("spellcraft.spell.*")

                                    ||

                                    sender.hasPermission(
                                            "spellcraft.spell."
                                                    + name.toLowerCase()))


                    .filter(name ->

                            name.toLowerCase()
                                    .startsWith(
                                            args[
                                                    args.length - 1]
                                                    .toLowerCase()))

                    .collect(Collectors.toList());
        }


        return List.of();
    }
}