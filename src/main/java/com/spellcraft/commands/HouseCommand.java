package com.spellcraft.commands;

import com.spellcraft.SpellCraftPlugin;
import com.spellcraft.api.SpellCaster;
import com.spellcraft.api.house.House;
import com.spellcraft.core.SpellCasterManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.checkerframework.checker.units.qual.C;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class HouseCommand implements CommandExecutor, TabCompleter {

    private final SpellCasterManager casterManager;

    public HouseCommand(SpellCasterManager casterManager) {
        this.casterManager = casterManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) return true;

        SpellCaster caster = casterManager.getCaster(player);

        if (args.length == 0) {
            SpellCraftPlugin.getAdventure().player(player).sendMessage(Component.text("/house choose <house>", NamedTextColor.YELLOW));
            SpellCraftPlugin.getAdventure().player(player).sendMessage(Component.text("/house info", NamedTextColor.YELLOW));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "choose" -> {

                if (args.length < 2) {
                    SpellCraftPlugin.getAdventure().player(player).sendMessage(Component.text("Choose a house!", NamedTextColor.RED));
                    return true;
                }

                try {
                    House house = House.of(args[1].toUpperCase());
                    caster.setHouse(house);

                    SpellCraftPlugin.getAdventure().player(player).sendMessage(Component.text("You have joined ").append(Component.text(house.getName()).color(house.getPrimaryColor())));
                } catch (IllegalArgumentException e) {
                    SpellCraftPlugin.getAdventure().player(player).sendMessage(Component.text("Invalid house.", NamedTextColor.RED));
                }
            }

            case "info" -> {
                House house = caster.getHouse();
                if (house == null) {
                    SpellCraftPlugin.getAdventure().player(player).sendMessage(Component.text("You do not belong to a house yet.", NamedTextColor.RED));
                    return true;
                }

                SpellCraftPlugin.getAdventure().player(player).sendMessage(Component.text("House: " + house.getName(), NamedTextColor.GOLD));
                SpellCraftPlugin.getAdventure().player(player).sendMessage(Component.text("Motto: " + house.getMotto(), NamedTextColor.GRAY));
                SpellCraftPlugin.getAdventure().player(player).sendMessage(Component.text("Symbol: " + house.getSymbol(), NamedTextColor.GRAY));
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (args.length == 1) {
            return List.of("choose", "info");
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("choose")) {
            return House.values().values().stream()
                    .map(House::getName)
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}
