package com.spellcraft.core.perks;

import com.spellcraft.SpellCraftPlugin;
import com.spellcraft.api.Spell;
import com.spellcraft.api.SpellCaster;
import com.spellcraft.api.house.House;
import com.spellcraft.api.magic.MagicElement;
import org.bukkit.configuration.ConfigurationSection;

public class PerkManager {

    private final SpellCraftPlugin plugin;

    public PerkManager(SpellCraftPlugin plugin) {
        this.plugin = plugin;
    }

    public int modifyMagicCost(SpellCaster caster, Spell spell) {
        double cost = spell.getMagicCost();

        cost *= getHouseMultiplier(caster.getHouse(), "magic-cost-multiplier");
        cost *= getElementMultiplier(spell.getElement(), "magic-cost-multiplier");

        return Math.max(0, (int) Math.round(cost));
    }

    public long modifyCooldown(SpellCaster caster, Spell spell) {
        double cooldown = spell.getCooldown();

        cooldown *= getHouseMultiplier(caster.getHouse(), "cooldown-multiplier");
        cooldown *= getElementMultiplier(spell.getElement(), "cooldown-multiplier");

        return Math.max(0L, (long) cooldown);
    }

    public int getBonusRegen(SpellCaster caster) {
        House house = caster.getHouse();
        if (house == null) return 0;

        ConfigurationSection sec = plugin.getPerksConfig()
                .getConfigurationSection("houses." + house.getName());

        return sec != null ? sec.getInt("magic-regen-bonus", 0) : 0;
    }

    private double getHouseMultiplier(House house, String key) {
        if (house == null) return 1.0;

        ConfigurationSection sec = plugin.getPerksConfig()
                .getConfigurationSection("houses." + house.getName());

        return sec != null ? sec.getDouble(key, 1.0) : 1.0;
    }

    private double getElementMultiplier(MagicElement element, String key) {
        if (element == null) return 1.0;

        ConfigurationSection sec = plugin.getPerksConfig()
                .getConfigurationSection("elements." + element.getName());

        return sec != null ? sec.getDouble(key, 1.0) : 1.0;
    }
}
