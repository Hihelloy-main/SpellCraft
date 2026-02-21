package com.spellcraft.commands;

import com.spellcraft.SpellCraftPlugin;
import com.spellcraft.api.*;
import com.spellcraft.api.magic.MagicElement;
import com.spellcraft.core.SpellCasterManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SpellCommand implements CommandExecutor, TabCompleter {

    private final SpellManager spellManager;
    private final SpellCasterManager casterManager;
    private final SpellCraftPlugin plugin;

    public SpellCommand(
            SpellCraftPlugin plugin,
            SpellManager spellManager,
            SpellCasterManager casterManager
    ) {
        this.plugin = plugin;
        this.spellManager = spellManager;
        this.casterManager = casterManager;
    }



    @Override
    public boolean onCommand(
            CommandSender sender,
            Command command,
            String label,
            String[] args
    ) {

        if (!(sender instanceof Player player)) {

            sender.sendMessage("Player only.");

            return true;
        }



        // BASE PERMISSION

        if (!player.hasPermission("spellcraft.admin.*")
                && !player.hasPermission("spellcraft.command.spell")) {

            player.sendMessage("No permission.");

            return true;
        }



        if (args.length == 0) {

            sendHelp(player);

            return true;
        }



        switch (args[0].toLowerCase()) {

            case "list" ->
                    listSpells(player, args);


            case "help" -> {

                if (args.length < 2) {

                    player.sendMessage("/spell help <spell>");

                    return true;
                }

                showSpellInfo(player, args[1]);
            }


            case "learned" ->
                    listLearnedSpells(player);


            case "bound" ->
                    listBoundSpells(player);


            case "reload" ->
                    reloadSpellCraft(player);


            case "display" -> {

                if (args.length < 2) {

                    player.sendMessage("/spell display <element>");

                    return true;
                }

                displayMovesinElementToSender(
                        MagicElement.of(args[1]),
                        player);
            }


            default ->
                    sendHelp(player);
        }


        return true;
    }




    private void reloadSpellCraft(Player player) {


        if (!player.hasPermission("spellcraft.admin.*")
                && !player.hasPermission("spellcraft.admin.reload")) {

            player.sendMessage("No permission.");

            return;
        }



        casterManager.saveAll();


        plugin.reloadConfig();

        plugin.reloadPerksConfig();


        plugin.getMagicBar().stop();

        plugin.getMagicBar().start();


        plugin.registerSpells();


        plugin.getServer()
                .getOnlinePlayers()
                .forEach(
                        plugin.getMagicBar()
                                ::showForPlayer);



        player.sendMessage("SpellCraft reloaded.");
    }




    private void sendHelp(Player player) {

        player.sendMessage("=== SpellCraft ===");

        player.sendMessage("/spell list");

        player.sendMessage("/spell help");

        player.sendMessage("/spell learned");

        player.sendMessage("/spell bound");

        player.sendMessage("/spell reload");
    }




    private void listSpells(Player player, String[] args) {


        List<Spell> spells;


        if (args.length > 1) {

            try {

                SpellCategory category =
                        SpellCategory.valueOf(
                                args[1]
                                        .toUpperCase());


                spells =
                        spellManager
                                .getSpellsByCategory(
                                        category)
                                .stream()
                                .toList();

            }

            catch (Exception e) {

                player.sendMessage("Invalid category.");

                return;
            }

        }

        else {

            spells =
                    spellManager
                            .getAllSpells()
                            .stream()
                            .toList();
        }



        for (Spell spell : spells) {


            String perm =
                    "spellcraft.spell."
                            + spell.getName()
                            .toLowerCase();



            if (player.hasPermission("spellcraft.admin.*")
                    || player.hasPermission("spellcraft.spell.*")
                    || player.hasPermission(perm)) {

                player.sendMessage(
                        spell.getName()
                                + " - "
                                + spell.getCategory()
                                .getDisplayName());
            }
        }
    }




    private void showSpellInfo(Player player, String spellName) {


        spellManager.getSpell(spellName)
                .ifPresentOrElse(

                        spell -> {

                            String perm =
                                    "spellcraft.spell."
                                            + spell.getName()
                                            .toLowerCase();



                            if (!player.hasPermission("spellcraft.admin.*")
                                    && !player.hasPermission("spellcraft.spell.*")
                                    && !player.hasPermission(perm)) {

                                player.sendMessage(
                                        "No permission.");

                                return;
                            }



                            player.sendMessage(
                                    "=== "
                                            + spell.getName()
                                            + " ===");


                            player.sendMessage(
                                    spell.getDescription());


                            player.sendMessage(
                                    spell.getInstructions());


                        },


                        () -> player.sendMessage(
                                "Spell not found.")
                );
    }




    private void displayMovesinElementToSender(
            MagicElement element,
            CommandSender sender
    ) {

        String elementPerm =
                "spellcraft.element."
                        + element.getName()
                        .toLowerCase();


        if (!sender.hasPermission("spellcraft.admin.*")
                && !sender.hasPermission("spellcraft.element.*")
                && !sender.hasPermission(elementPerm)) {

            sender.sendMessage(
                    "No permission.");

            return;
        }



        sender.sendMessage(
                element.getName()
                        + " spells:");


        for (Spell spell :
                spellManager.getAllSpells()) {

            if (spell.getElement()
                    .equals(element)) {

                sender.sendMessage(
                        spell.getName());
            }
        }
    }




    private void listLearnedSpells(Player player) {

        SpellCaster caster =
                casterManager
                        .getCaster(player);


        for (Spell spell :
                caster.getLearnedSpells()) {

            player.sendMessage(
                    spell.getName());
        }
    }




    private void listBoundSpells(Player player) {


        SpellCaster caster =
                casterManager
                        .getCaster(player);


        Spell[] bound =
                caster.getBoundSpells();


        for (int i = 0; i < bound.length; i++) {

            if (bound[i] != null) {

                player.sendMessage(
                        (i + 1)
                                + ": "
                                + bound[i]
                                .getName());
            }
        }
    }




    @Override
    public List<String> onTabComplete(
            CommandSender sender,
            Command command,
            String alias,
            String[] args
    ) {


        if (!sender.hasPermission("spellcraft.command.spell"))
            return List.of();



        if (args.length == 1) {

            return List.of(
                            "list",
                            "help",
                            "learned",
                            "bound",
                            "reload",
                            "display")

                    .stream()

                    .filter(s ->
                            s.startsWith(
                                    args[0]))

                    .collect(Collectors.toList());
        }



        if (args.length == 2
                && args[0].equalsIgnoreCase("help")) {


            return spellManager
                    .getAllSpells()
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


                    .collect(Collectors.toList());
        }



        if (args.length == 2
                && args[0].equalsIgnoreCase("display")) {


            return MagicElement
                    .values()
                    .values()
                    .stream()

                    .map(MagicElement::getName)

                    .filter(name ->

                            sender.hasPermission("spellcraft.admin.*")

                                    ||

                                    sender.hasPermission("spellcraft.element.*")

                                    ||

                                    sender.hasPermission(
                                            "spellcraft.element."
                                                    + name.toLowerCase()))

                    .collect(Collectors.toList());
        }



        return new ArrayList<>();
    }
}