package com.spellcraft.core;

import com.spellcraft.SpellCraftPlugin;
import com.spellcraft.api.Spell;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SpellBookSpawner {

    private final SpellCraftPlugin plugin;
    private final Random random;

    public SpellBookSpawner(SpellCraftPlugin plugin) {
        this.plugin = plugin;
        this.random = new Random();
    }

    /**
     * Spawn SpellBooks randomly in loaded chests if enabled in config.
     */
    public void spawnSpellBooks() {
        if (!plugin.getConfig().getBoolean("spellbook.world-generation", true)) return;

        int chancePercent = plugin.getConfig().getInt("spellbook.chance-per-chest", 10);
        int maxBooksPerChest = plugin.getConfig().getInt("spellbook.max-books-per-chest", 1);

        List<Spell> allSpells = new ArrayList<>(plugin.getSpellManager().getAllSpells());
        if (allSpells.isEmpty()) return;

        for (World world : plugin.getServer().getWorlds()) {
            for (var chunk : world.getLoadedChunks()) {
                for (int x = 0; x < 16; x++) {
                    for (int y = 0; y < world.getMaxHeight(); y++) {
                        for (int z = 0; z < 16; z++) {
                            Block block = chunk.getBlock(x, y, z);
                            if (block.getType() != Material.CHEST) continue;
                            if (random.nextInt(100) >= chancePercent) continue;

                            BlockState state = block.getState();
                            if (!(state instanceof Chest chest)) continue;
                            Inventory inv = chest.getInventory();

                            // Randomly pick 1-3 spells
                            int numSpells = 1 + random.nextInt(Math.min(3, allSpells.size()));
                            SpellBookImpl book = new SpellBookImpl("Ancient SpellBook", plugin.getSpellBookKey());
                            for (int i = 0; i < numSpells; i++) {
                                Spell spell = allSpells.get(random.nextInt(allSpells.size()));
                                book.addSpell(spell);
                            }

                            ItemStack item = book.toItemStack();
                            for (int i = 0; i < maxBooksPerChest; i++) inv.addItem(item);
                        }
                    }
                }
            }
        }
    }
}
