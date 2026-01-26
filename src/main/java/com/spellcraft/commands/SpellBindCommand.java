package com.spellcraft.commands;

import com.spellcraft.SpellCraftPlugin;
import com.spellcraft.api.Spell;
import com.spellcraft.api.SpellCaster;
import com.spellcraft.api.SpellManager;
import com.spellcraft.api.house.House;
import com.spellcraft.api.magic.MagicElement;
import com.spellcraft.core.SpellCasterManager;
import com.spellcraft.util.HouseUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SpellBindCommand implements CommandExecutor, TabCompleter {

    private final SpellCraftPlugin plugin;
    private final SpellManager spellManager;
    private final SpellCasterManager casterManager;

    public SpellBindCommand(
            SpellCraftPlugin plugin,
            SpellManager spellManager,
            SpellCasterManager casterManager
    ) {
        this.plugin = plugin;
        this.spellManager = spellManager;
        this.casterManager = casterManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            SpellCraftPlugin.getAdventure().sender(sender).sendMessage(Component.text("This command can only be used by players!")
                    .color(NamedTextColor.RED));
            return true;
        }

        if (args.length < 2) {
            SpellCraftPlugin.getAdventure().player(player).sendMessage(Component.text("Usage: /spellbind <slot> <spell|clear>")
                    .color(NamedTextColor.RED));
            SpellCraftPlugin.getAdventure().player(player).sendMessage(Component.text("Slot must be between 1–9")
                    .color(NamedTextColor.GRAY));
            return true;
        }

        int slot;
        try {
            slot = Integer.parseInt(args[0]) - 1;
            if (slot < 0 || slot > 8) {
                SpellCraftPlugin.getAdventure().player(player).sendMessage(Component.text("Slot must be between 1–9")
                        .color(NamedTextColor.RED));
                return true;
            }
        } catch (NumberFormatException e) {
            SpellCraftPlugin.getAdventure().player(player).sendMessage(Component.text("Invalid slot number!")
                    .color(NamedTextColor.RED));
            return true;
        }

        SpellCaster caster = casterManager.getCaster(player);

        if (args[1].equalsIgnoreCase("clear")) {
            caster.unbindSpell(slot);
            plugin.getPlayerDataManager().save(caster);

            SpellCraftPlugin.getAdventure().player(player).sendMessage(Component.text("Cleared spell from slot " + (slot + 1))
                    .color(NamedTextColor.GREEN));
            return true;
        }

        House house = caster.getHouse();
        if (house == null) {
            SpellCraftPlugin.getAdventure().player(player).sendMessage(Component.text("You must choose a house first!")
                    .color(NamedTextColor.RED));
            return true;
        }

        String spellName = String.join(" ", args).substring(args[0].length()).trim();
        Optional<Spell> spellOpt = spellManager.getSpell(spellName);

        if (spellOpt.isEmpty()) {
            SpellCraftPlugin.getAdventure().player(player).sendMessage(Component.text("Spell not found: " + spellName)
                    .color(NamedTextColor.RED));
            return true;
        }

        Spell spell = spellOpt.get();

        if (!caster.hasLearnedSpell(spell)) {
            SpellCraftPlugin.getAdventure().player(player).sendMessage(Component.text("You haven't learned this spell yet!")
                    .color(NamedTextColor.RED));
            SpellCraftPlugin.getAdventure().player(player).sendMessage(Component.text("Find a SpellBook to learn new spells.")
                    .color(NamedTextColor.GRAY));
            return true;
        }

        MagicElement element = spell.getElement();
        if (element != null && !HouseUtil.canUse(house, element)) {
            SpellCraftPlugin.getAdventure().player(player).sendMessage(Component.text("Your house cannot bind this type of magic!")
                    .color(NamedTextColor.RED));
            SpellCraftPlugin.getAdventure().player(player).sendMessage(Component.text(
                            "House: " + house.getName() + " | Element: " + element.getName())
                    .color(NamedTextColor.GRAY));
            return true;
        }

        caster.bindSpell(slot, spell);
        plugin.getPlayerDataManager().save(caster);

        TextColor elementColor = spell.getElement().getColor();


        SpellCraftPlugin.getAdventure().player(player).sendMessage(
                Component.text("Bound ")
                        .append(Component.text(spell.getName()).color(elementColor))
                        .append(Component.text(" to slot " + (slot + 1)))
                        .color(NamedTextColor.GREEN)
        );

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player player)) return List.of();

        SpellCaster caster = casterManager.getCaster(player);

        if (args.length == 1) {
            return List.of("1","2","3","4","5","6","7","8","9").stream()
                    .filter(s -> s.startsWith(args[0]))
                    .collect(Collectors.toList());
        }

        if (args.length == 2) {
            List<String> suggestions = new ArrayList<>();
            suggestions.add("clear");

            House house = caster.getHouse();

            suggestions.addAll(
                    caster.getLearnedSpells().stream()
                            .filter(spell -> {
                                if (house == null) return false;
                                MagicElement element = spell.getElement();
                                return element == null || HouseUtil.canUse(house, element);
                            })
                            .map(Spell::getName)
                            .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                            .collect(Collectors.toList())
            );

            return suggestions;
        }

        return List.of();
    }
}
