package com.spellcraft.core;

import com.spellcraft.api.*;
import com.spellcraft.api.event.SpellCastEvent;
import com.spellcraft.api.event.SpellFailEvent;
import com.spellcraft.api.event.SpellPreCastEvent;
import com.spellcraft.api.house.House;
import com.spellcraft.util.HouseUtil;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Implementation of SpellCaster interface.
 * Handles magic, spells, binds, cooldowns, house restrictions, and persistence.
 */
public class SpellCasterImpl implements SpellCaster {

    private final UUID uuid;
    private final Player player;

    private int magic;
    private int maxMagic;

    private final Set<Spell> learnedSpells = new HashSet<>();
    private final Spell[] boundSpells = new Spell[9];
    private final Map<Spell, Long> cooldowns = new HashMap<>();

    private House house;

    public SpellCasterImpl(Player player, int maxMagic) {
        this.uuid = player.getUniqueId();
        this.player = player;
        this.maxMagic = maxMagic;
        this.magic = maxMagic;
    }


    @Override
    public UUID getUUID() {
        return uuid;
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    @Override
    public int getMagic() {
        return magic;
    }

    @Override
    public void setMagic(int magic) {
        this.magic = Math.max(0, Math.min(magic, maxMagic));
    }

    @Override
    public int getMaxMagic() {
        return maxMagic;
    }

    @Override
    public void setMaxMagic(int maxMagic) {
        this.maxMagic = maxMagic;
        if (magic > maxMagic) magic = maxMagic;
    }

    @Override
    public boolean hasMagic(int amount) {
        return magic >= amount;
    }

    @Override
    public void consumeMagic(int amount) {
        magic = Math.max(0, magic - amount);
    }

    @Override
    public void regenerateMagic(int amount) {
        magic = Math.min(maxMagic, magic + amount);
    }

    @Override
    public List<Spell> getLearnedSpells() {
        return new ArrayList<>(learnedSpells);
    }

    @Override
    public boolean hasLearnedSpell(Spell spell) {
        return spell != null && learnedSpells.contains(spell);
    }

    @Override
    public void learnSpell(Spell spell) {
        if (spell != null) learnedSpells.add(spell);
    }

    @Override
    public void unlearnSpell(Spell spell) {
        learnedSpells.remove(spell);
        // Automatically unbind if bound
        for (int i = 0; i < boundSpells.length; i++) {
            if (boundSpells[i] == spell) boundSpells[i] = null;
        }
    }

    @Override
    public void saveLearnedSpells(ConfigurationSection section) {
        section.set("list", getLearnedSpells().stream().map(Spell::getName).toList());
    }

    @Override
    public void loadLearnedSpells(ConfigurationSection section, SpellManager registry) {
        learnedSpells.clear();
        cooldowns.clear();

        if (section == null) return;
        for (String name : section.getStringList("list")) {
            registry.getSpell(name).ifPresent(learnedSpells::add);
        }
    }

    @Override
    public Spell[] getBoundSpells() {
        return Arrays.copyOf(boundSpells, boundSpells.length);
    }

    @Override
    public void bindSpell(int slot, Spell spell) {
        if (slot < 0 || slot >= boundSpells.length) throw new IllegalArgumentException("Invalid slot");
        if (spell == null) throw new IllegalArgumentException("Spell cannot be null");
        if (!hasLearnedSpell(spell)) throw new IllegalStateException("Spell not learned");

        boundSpells[slot] = spell;
    }

    @Override
    public void unbindSpell(int slot) {
        if (slot < 0 || slot >= boundSpells.length) return;
        boundSpells[slot] = null;
    }

    @Override
    public Spell getSpellAtSlot(int slot) {
        return (slot < 0 || slot >= boundSpells.length) ? null : boundSpells[slot];
    }

    @Override
    public void saveBinds(ConfigurationSection section) {
        for (int i = 0; i < boundSpells.length; i++) {
            section.set(String.valueOf(i), boundSpells[i] != null ? boundSpells[i].getName() : null);
        }
    }

    @Override
    public void loadBinds(ConfigurationSection section, SpellManager registry) {
        Arrays.fill(boundSpells, null);
        if (section == null) return;

        for (String key : section.getKeys(false)) {
            if (!key.matches("\\d+")) continue;

            int slot = Integer.parseInt(key);
            if (slot >= boundSpells.length) continue;

            String spellName = section.getString(key);
            if (spellName == null) continue;

            registry.getSpell(spellName)
                    .filter(this::hasLearnedSpell)
                    .ifPresent(spell -> boundSpells[slot] = spell);
        }
    }

    @Override
    public boolean isOnCooldown(Spell spell) {
        Long end = cooldowns.get(spell);
        if (end == null || System.currentTimeMillis() >= end) {
            cooldowns.remove(spell);
            return false;
        }
        return true;
    }

    @Override
    public void setCooldown(Spell spell, long duration) {
        cooldowns.put(spell, System.currentTimeMillis() + duration);
    }

    @Override
    public long getRemainingCooldown(Spell spell) {
        Long end = cooldowns.get(spell);
        return end == null ? 0 : Math.max(0, end - System.currentTimeMillis());
    }

    @Override
    public SpellResult castSpell(Spell spell) {

        if (!hasLearnedSpell(spell)) {
            Bukkit.getPluginManager().callEvent(new SpellFailEvent(this, spell, "NOT_LEARNED"));
            return SpellResult.FAILURE;
        }

        SpellPreCastEvent pre = new SpellPreCastEvent(this, spell, spell.getMagicCost());
        Bukkit.getPluginManager().callEvent(pre);

        if (pre.isCancelled()) {
            Bukkit.getPluginManager().callEvent(new SpellFailEvent(this, spell, "CANCELLED"));
            return SpellResult.FAILURE;
        }

        if (isOnCooldown(spell)) {
            Bukkit.getPluginManager().callEvent(new SpellFailEvent(this, spell, "COOLDOWN"));
            return SpellResult.FAILURE;
        }

        if (!hasMagic(pre.getMagicCost())) {
            Bukkit.getPluginManager().callEvent(new SpellFailEvent(this, spell, "INSUFFICIENT_MAGIC"));
            return SpellResult.FAILURE;
        }

        // attempt cast FIRST
        SpellResult result = spell.cast(this);

        if (result != SpellResult.SUCCESS) {
            Bukkit.getPluginManager().callEvent(new SpellFailEvent(this, spell, "CAST_FAILED"));
            return result;
        }

        // only consume magic AFTER success
        consumeMagic(pre.getMagicCost());

        // apply cooldown AFTER success

        Bukkit.getPluginManager().callEvent(new SpellCastEvent(this, spell));

        return SpellResult.SUCCESS;
    }

    @Override
    public House getHouse() {
        return house;
    }

    @Override
    public void setHouse(House house) {
        this.house = house;
        // Automatically unbind illegal spells
        if (house != null) {
            for (int i = 0; i < boundSpells.length; i++) {
                Spell spell = boundSpells[i];
                if (spell != null && spell.getElement() != null && !HouseUtil.canUse(house, spell.getElement())) {
                    boundSpells[i] = null;
                }
            }
        }
    }

    @Override
    public boolean hasHouse() {
        return house != null;
    }
}
