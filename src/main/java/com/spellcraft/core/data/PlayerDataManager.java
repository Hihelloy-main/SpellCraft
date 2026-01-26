package com.spellcraft.core.data;

import com.spellcraft.SpellCraftPlugin;
import com.spellcraft.api.Spell;
import com.spellcraft.api.SpellCaster;
import com.spellcraft.api.house.House;
import com.spellcraft.api.magic.MagicElement;
import com.spellcraft.core.SpellCasterImpl;
import com.spellcraft.util.HouseUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PlayerDataManager {

    private final SpellCraftPlugin plugin;
    private final File playerFolder;

    private static final ExecutorService IO_EXECUTOR =
            Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r, "SpellCraft-PlayerIO");
                t.setDaemon(true);
                return t;
            });

    public PlayerDataManager(SpellCraftPlugin plugin) {
        this.plugin = plugin;
        this.playerFolder = new File(plugin.getDataFolder(), "players");

        if (!playerFolder.exists() && !playerFolder.mkdirs()) {
            plugin.getLogger().severe("Failed to create player data folder!");
        }
    }

    public File getPlayerFile(UUID uuid) {
        return new File(playerFolder, uuid.toString() + ".yml");
    }

    /** Load player data and apply defaults if necessary */
    public SpellCaster load(Player player) {
        File file = getPlayerFile(player.getUniqueId());
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        int maxMagic = plugin.getConfig().getInt("magic.max", 100);
        SpellCasterImpl caster = new SpellCasterImpl(player, maxMagic);

        caster.setMagic(config.getInt("magic", maxMagic));

        // Load house
        String houseName = config.getString("house");
        if (houseName != null) {
            try {
                caster.setHouse(House.of(houseName));
            } catch (IllegalArgumentException ex) {
                plugin.getLogger().warning(
                        "Invalid house '" + houseName + "' for " + player.getName()
                );
                caster.setHouse(null);
            }
        }

        ConfigurationSection learned = config.getConfigurationSection("learned-spells");
        if (learned != null) {
            caster.loadLearnedSpells(learned, plugin.getSpellManager());
        }

        ConfigurationSection binds = config.getConfigurationSection("binds");
        if (binds != null) {
            caster.loadBinds(binds, plugin.getSpellManager());
        }

        autoUnbindInvalidSpells(caster);

        if (!file.exists()) {
            saveAsync(caster);
        }

        return caster;
    }

    /** Save player data synchronously */
    public void save(SpellCaster caster) {
        PlayerSnapshot snapshot = PlayerSnapshot.capture(caster);
        writeSnapshot(snapshot);
    }

    /** Save player data asynchronously (non blocking) */
    public void saveAsync(SpellCaster caster) {
        PlayerSnapshot snapshot = PlayerSnapshot.capture(caster);
        IO_EXECUTOR.execute(() -> writeSnapshot(snapshot));
    }

    private void writeSnapshot(PlayerSnapshot snapshot) {
        File file = getPlayerFile(snapshot.uuid);
        YamlConfiguration config = new YamlConfiguration();

        config.set("magic", snapshot.magic);
        config.set("house", snapshot.house != null ? snapshot.house.getName() : null);

        ConfigurationSection learned = config.createSection("learned-spells");
        snapshot.saveLearnedSpells(learned);

        ConfigurationSection binds = config.createSection("binds");
        snapshot.saveBinds(binds);

        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe(
                    "Failed to save player data for " + snapshot.name
            );
            e.printStackTrace();
        }
    }

    /** Unbinds any spell that the player's house cannot use */
    private void autoUnbindInvalidSpells(SpellCaster caster) {
        House house = caster.getHouse();
        if (house == null) return;

        for (int i = 0; i < 9; i++) {
            Spell spell = caster.getSpellAtSlot(i);
            if (spell == null) continue;

            MagicElement element = spell.getElement();
            if (element != null && !HouseUtil.canUse(house, element)) {
                caster.unbindSpell(i);
            }
        }
    }

    private static final class PlayerSnapshot {

        final UUID uuid;
        final String name;
        final int magic;
        final House house;
        final SpellCaster caster;

        private PlayerSnapshot(SpellCaster caster) {
            this.caster = caster;
            this.uuid = caster.getUUID();
            this.name = caster.getPlayer().getName();
            this.magic = caster.getMagic();
            this.house = caster.getHouse();
        }

        static PlayerSnapshot capture(SpellCaster caster) {
            return new PlayerSnapshot(caster);
        }

        void saveLearnedSpells(ConfigurationSection section) {
            caster.saveLearnedSpells(section);
        }

        void saveBinds(ConfigurationSection section) {
            caster.saveBinds(section);
        }
    }
}
