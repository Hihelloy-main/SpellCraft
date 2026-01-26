package com.spellcraft.listeners;

import com.spellcraft.SpellCraftPlugin;
import com.spellcraft.api.Spell;
import com.spellcraft.api.SpellCaster;
import com.spellcraft.api.SpellManager;
import com.spellcraft.api.house.House;
import com.spellcraft.core.SpellCasterManager;
import com.spellcraft.util.HouseUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Optional;

public class SpellBookListener implements Listener {

    private final SpellCraftPlugin plugin;
    private final SpellCasterManager casterManager;
    private final SpellManager spellManager;
    private final NamespacedKey spellBookKey;

    public SpellBookListener(SpellCraftPlugin plugin, SpellCasterManager casterManager,
                            SpellManager spellManager, NamespacedKey spellBookKey) {
        this.plugin = plugin;
        this.casterManager = casterManager;
        this.spellManager = spellManager;
        this.spellBookKey = spellBookKey;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item.getType() != Material.WRITTEN_BOOK) {
            return;
        }

        BookMeta meta = (BookMeta) item.getItemMeta();
        if (meta == null || !meta.getPersistentDataContainer().has(spellBookKey, PersistentDataType.STRING)) {
            return;
        }

        event.setCancelled(true);

        String spellNames = meta.getPersistentDataContainer().get(spellBookKey, PersistentDataType.STRING);
        if (spellNames == null || spellNames.isEmpty()) {
            SpellCraftPlugin.getAdventure().player(player).sendMessage(Component.text("This SpellBook is empty!", NamedTextColor.RED));
            return;
        }

        SpellCaster caster = casterManager.getCaster(player);
        String[] spells = spellNames.split(",");
        int learned = 0;

        for (String spellName : spells) {
            Optional<Spell> spellOpt = spellManager.getSpell(spellName.trim());
            if (spellOpt.isPresent()) {
                Spell spell = spellOpt.get();
                if (!caster.hasLearnedSpell(spell) && HouseUtil.canUse(caster.getHouse(), spell.getElement())) {
                    caster.learnSpell(spell);
                    learned++;
                    SpellCraftPlugin.getAdventure().player(player).sendMessage(Component.text("Learned: ", NamedTextColor.GREEN).append(Component.text(spell.getName(), NamedTextColor.GOLD)));
                } else if (!HouseUtil.canUse(caster.getHouse(), spell.getElement())) {
                    SpellCraftPlugin.getAdventure().player(player).sendMessage(Component.text("Can't learn spell because your house can't use this spell's element!", NamedTextColor.RED));
                }
            }
        }

        if (learned > 0) {
            player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
            SpellCraftPlugin.getAdventure().player(player).sendMessage(Component.text("The SpellBook crumbles to dust after revealing its secrets!", NamedTextColor.AQUA));
        } else {
            SpellCraftPlugin.getAdventure().player(player).sendMessage(Component.text("You've already learned all spells in this book!", NamedTextColor.YELLOW));
        }
    }
}
