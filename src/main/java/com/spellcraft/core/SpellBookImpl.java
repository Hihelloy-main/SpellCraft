package com.spellcraft.core;

import com.spellcraft.SpellCraftPlugin;
import com.spellcraft.api.Spell;
import com.spellcraft.api.SpellBook;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentBuilder;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SpellBookImpl implements SpellBook {

    private final String title;
    private final List<Spell> spells;
    private final NamespacedKey spellBookKey;

    public SpellBookImpl(String title, NamespacedKey spellBookKey) {
        this.title = title;
        this.spells = new ArrayList<>();
        this.spellBookKey = spellBookKey;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public List<Spell> getSpells() {
        return new ArrayList<>(spells);
    }

    @Override
    public void addSpell(Spell spell) {
        if (!spells.contains(spell)) {
            spells.add(spell);
        }
    }

    @Override
    public void removeSpell(Spell spell) {
        spells.remove(spell);
    }

    @Override
    public boolean containsSpell(Spell spell) {
        return spells.contains(spell);
    }

    @Override
    public ItemStack toItemStack() {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();

        if (meta != null) {
            meta.setTitle(ChatColor.GOLD + title);
            meta.setAuthor(ChatColor.DARK_PURPLE + "Old HogWartz Professor");

            List<String> pages = new ArrayList<>();
            StringBuilder currentPage = new StringBuilder();
            currentPage.append(ChatColor.DARK_PURPLE).append(ChatColor.BOLD).append("SpellBook\n\n");
            currentPage.append(ChatColor.RESET).append(ChatColor.BLACK).append("This ancient tome\ncontains knowledge\nof powerful spells.\n\n");
            currentPage.append("Right-click to\nlearn the spells\nwithin.");
            pages.add(currentPage.toString());

            for (Spell spell : spells) {
                StringBuilder page = new StringBuilder();
                page.append(ChatColor.GOLD).append(ChatColor.BOLD).append(spell.getName()).append("\n\n");
                page.append(ChatColor.RESET).append(ChatColor.BLACK).append(spell.getDescription()).append("\n\n");
                page.append(ChatColor.DARK_GRAY).append("Category: ").append(spell.getCategory().getDisplayName()).append("\n");
                page.append("Magic Cost: ").append(spell.getMagicCost()).append("\n");
                page.append("Cooldown: ").append(spell.getCooldown() / 1000).append("s");
                pages.add(page.toString());
            }

            meta.setPages(pages);

            String spellNames = spells.stream()
                    .map(Spell::getName)
                    .collect(Collectors.joining(","));
            meta.getPersistentDataContainer().set(spellBookKey, PersistentDataType.STRING, spellNames);

            book.setItemMeta(meta);
        }

        return book;
    }

    @Override
    public boolean isValidSpellBook(ItemStack item) {
        if (item == null || item.getType() != Material.WRITTEN_BOOK) {
            return false;
        }
        BookMeta meta = (BookMeta) item.getItemMeta();
        if (meta == null) {
            return false;
        }
        return meta.getPersistentDataContainer().has(spellBookKey, PersistentDataType.STRING);
    }

    @Override
    public SpellBook fromItemStack(ItemStack item) {
        if (!isValidSpellBook(item)) {
            return null;
        }

        BookMeta meta = (BookMeta) item.getItemMeta();
        String spellNames = meta.getPersistentDataContainer().get(spellBookKey, PersistentDataType.STRING);

        SpellBookImpl spellBook = new SpellBookImpl(meta.getTitle(), spellBookKey);

        return spellBook;
    }
}
