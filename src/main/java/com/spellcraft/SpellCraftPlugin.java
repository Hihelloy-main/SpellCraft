package com.spellcraft;

import com.cjcrafter.foliascheduler.FoliaCompatibility;
import com.cjcrafter.foliascheduler.ServerImplementation;
import com.spellcraft.api.Spell;
import com.spellcraft.api.SpellCaster;
import com.spellcraft.api.SpellManager;
import com.spellcraft.commands.HouseCommand;
import com.spellcraft.commands.SpellBindCommand;
import com.spellcraft.commands.SpellBookCommand;
import com.spellcraft.commands.SpellCommand;
import com.spellcraft.core.SpellBookImpl;
import com.spellcraft.core.SpellBookSpawner;
import com.spellcraft.core.SpellCasterManager;
import com.spellcraft.core.SpellManagerImpl;
import com.spellcraft.core.data.PlayerDataManager;
import com.spellcraft.core.perks.PerkManager;
import com.spellcraft.hooks.GriefPreventionHook;
import com.spellcraft.hooks.WGHook;
import com.spellcraft.listeners.PlayerListener;
import com.spellcraft.listeners.SpellBookListener;
import com.spellcraft.listeners.SpellCastListener;
import com.spellcraft.spells.*;
import com.spellcraft.ui.MagicBar;
import com.spellcraft.util.ThreadUtil;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * Main plugin class for SpellCraft.
 * <p>
 * Responsible for initializing the plugin, managing spell registration,
 * player data, magic bars, hooks, and commands.
 */
public class SpellCraftPlugin extends JavaPlugin {

    /** Singleton plugin instance */
    public static SpellCraftPlugin plugin;

    /** Manager for handling spells */
    private SpellManager spellManager;

    /** Implementation of SpellManager */
    private SpellManagerImpl spellManagerImpl;

    /** Manager for player SpellCaster instances */
    private SpellCasterManager casterManager;

    /** Manager for loading/saving player data */
    private PlayerDataManager playerDataManager;

    /** Manager for handling perks */
    private PerkManager perkManager;

    /** UI manager for player magic bars */
    private MagicBar magicBar;

    /** Key used for registering spellbook items */
    private NamespacedKey spellBookKey;

    /** Configuration for perks */
    private FileConfiguration perksConfig;

    /** Adventure API for boss bars and chat */
    private BukkitAudiences adventure;

    /** Scheduler abstraction (supports Folia and Bukkit) */
    public static ServerImplementation scheduler;

    /** Plugin logger */
    public static Logger log;

    @Override
    public void onEnable() {
        plugin = this;
        scheduler = new FoliaCompatibility(plugin).getServerImplementation();
        log = getLogger();

        // Save default configs and load perks
        saveDefaultConfig();
        saveResource("perks.yml", false);
        loadPerksConfig();

        perkManager = new PerkManager(this);

        // Initialize spellbook key
        spellBookKey = new NamespacedKey(this, "spellbook");

        // Initialize caster manager
        int maxMagic = getConfig().getInt("magic.max", 100);
        casterManager = new SpellCasterManager(this, maxMagic);

        // Initialize spell manager
        spellManagerImpl = new SpellManagerImpl();
        spellManager = spellManagerImpl;
        registerSpells();

        // Load player data
        playerDataManager = new PlayerDataManager(this);

        // Start magic bar UI
        magicBar = new MagicBar(this, casterManager);
        magicBar.start();

        // Initialize optional hooks
        if (Bukkit.getPluginManager().getPlugin("WorldEdit") != null) {
            WGHook.init();
        }
        if (Bukkit.getPluginManager().getPlugin("GriefPrevention") != null) {
            GriefPreventionHook.init(this);
        }

        // Register listeners and commands
        registerListeners();
        registerCommands();

        // Safely load online players (for /reload support)
        Bukkit.getOnlinePlayers().forEach(player -> {
            SpellCaster caster = playerDataManager.load(player);
            casterManager.registerCaster(player.getUniqueId(), caster);
            magicBar.showForPlayer(player);
        });

        // Optionally spawn spellbooks in the world
        if (getConfig().getBoolean("spellbook.world-generation", true)) {
            new SpellBookSpawner(this).spawnSpellBooks();
        }

        // Register spellbook crafting recipe
        registerSpellBookRecipe();

        getLogger().info("SpellCraft has been enabled!");
    }

    @Override
    public void onDisable() {
        // Stop magic bar UI
        if (magicBar != null) {
            magicBar.stop();
        }

        // Unregister spells asynchronously
        if (spellManager != null) {
            spellManager.getSpellMap().forEach((s, spell) -> spellManager.unregisterSpellAsync(spell));
            spellManager.shutdown();
        }

        // Save and clear player casters
        if (casterManager != null) {
            casterManager.saveAll();
            casterManager.clearCasters();
        }

        // Shutdown any background tasks
        ThreadUtil.shutdown();

        getLogger().info("SpellCraft has been disabled!");
    }

    /**
     * Reloads the perks configuration from file.
     */
    public void reloadPerksConfig() {
        loadPerksConfig();
        getLogger().info("Perks config reloaded.");
    }

    /**
     * Loads the perks.yml file into memory.
     */
    private void loadPerksConfig() {
        File file = new File(getDataFolder(), "perks.yml");
        perksConfig = YamlConfiguration.loadConfiguration(file);
    }

    /**
     * Gets the Adventure API instance for this plugin.
     *
     * @return BukkitAudiences instance
     */
    public static BukkitAudiences getAdventure() {
        if (getInstance().adventure == null) {
            getInstance().adventure = BukkitAudiences.create(getInstance());
        }
        return getInstance().adventure;
    }

    /**
     * Registers all default spells asynchronously.
     */
    public void registerSpells() {
        ThreadUtil.runAsync(() -> {
            spellManager.registerSpellAsync(new FireballSpell());
            spellManager.registerSpellAsync(new HealSpell());
            spellManager.registerSpellAsync(new TeleportSpell());
            spellManager.registerSpellAsync(new LightningSpell());
            spellManager.registerSpellAsync(new ShieldSpell());
            spellManager.registerSpellAsync(new EarthBlock());
            spellManager.registerSpellAsync(new Flamethrower());
            spellManager.registerSpellAsync(new AquaMissile());
            spellManager.registerSpellAsync(new AirJets());
            spellManager.registerSpellAsync(new EarthKick());

            getLogger().info("Registered " + spellManager.getAllSpells().size() + " spells");
        });
    }

    /** Registers all plugin event listeners */
    private void registerListeners() {
        getServer().getPluginManager().registerEvents(
                new PlayerListener(this, casterManager), this
        );
        getServer().getPluginManager().registerEvents(
                new SpellCastListener(this, casterManager), this
        );
        getServer().getPluginManager().registerEvents(
                new SpellBookListener(this, casterManager, spellManager, spellBookKey), this
        );
    }

    /** Registers all plugin commands and their tab completers */
    private void registerCommands() {
        Objects.requireNonNull(getCommand("spell"))
                .setExecutor(new SpellCommand(this, spellManager, casterManager));

        Objects.requireNonNull(getCommand("spellbind"))
                .setExecutor(new SpellBindCommand(this, spellManager, casterManager));

        Objects.requireNonNull(getCommand("spellbook"))
                .setExecutor(new SpellBookCommand(this, spellManager, spellBookKey));

        Objects.requireNonNull(getCommand("house"))
                .setExecutor(new HouseCommand(casterManager));
        Objects.requireNonNull(getCommand("house"))
                .setTabCompleter(new HouseCommand(casterManager));
    }

    /** Registers the crafting recipe for the spellbook if enabled */
    private void registerSpellBookRecipe() {
        if (!getConfig().getBoolean("spellbook.craftable", true)) return;

        var item = new SpellBookImpl("Ancient SpellBook", spellBookKey).toItemStack();
        NamespacedKey key = new NamespacedKey(this, "spellbook_recipe");

        ShapelessRecipe recipe = new ShapelessRecipe(key, item);
        recipe.addIngredient(Material.PAPER);
        recipe.addIngredient(Material.BOOK);
        recipe.addIngredient(Material.NETHER_STAR);

        getServer().addRecipe(recipe);
    }

    /** Gets the plugin singleton instance */
    public static SpellCraftPlugin getInstance() {
        return plugin;
    }

    public SpellManager getSpellManager() {
        return spellManager;
    }

    public SpellManagerImpl getSpellManagerImpl() {
        return spellManagerImpl;
    }

    public SpellCasterManager getCasterManager() {
        return casterManager;
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    public MagicBar getMagicBar() {
        return magicBar;
    }

    public PerkManager getPerkManager() {
        return perkManager;
    }

    public FileConfiguration getPerksConfig() {
        return perksConfig;
    }

    public NamespacedKey getSpellBookKey() {
        return spellBookKey;
    }
}
