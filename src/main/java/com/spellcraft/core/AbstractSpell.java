package com.spellcraft.core;

import com.spellcraft.SpellCraftPlugin;
import com.spellcraft.api.Spell;
import com.spellcraft.api.SpellCategory;
import com.spellcraft.api.SpellCaster;
import com.spellcraft.api.SpellResult;
import com.spellcraft.util.ThreadUtil;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

/**
 * Base implementation for all SpellCraft spells.
 * <p>
 * Handles shared spell lifecycle logic such as casting, tracking,
 * cooldowns, and cleanup. Concrete spells should extend this class
 * and implement the abstract hooks.
 */
public abstract class AbstractSpell implements Spell {

    protected final @Nullable String name;
    protected final @Nullable String description;
    protected final @Nullable SpellCategory category;
    protected final @Nullable Integer magicCost;
    protected final @Nullable Long cooldown;
    protected final @Nullable Double range;
    protected final @Nullable String instructions;
    protected @Nullable Location location;

    protected boolean enabled;
    protected boolean removed;
    protected long startTime;
    private ThreadUtil.ThreadTask progressTask;

    /**
     * Creates a new base spell instance with the given metadata.
     *
     * @param name         The display name of the spell
     * @param description A short description of the spell
     * @param category     The spell category
     * @param magicCost    Magic cost required to cast
     * @param cooldown     Cooldown duration in milliseconds
     * @param range        Maximum effective range
     * @param enabled      Whether the spell is enabled
     * @param instructions Player-facing casting instructions
     */
    protected AbstractSpell(
            @Nullable String name,
            @Nullable String description,
            @Nullable SpellCategory category,
            @Nullable Integer magicCost,
            @Nullable Long cooldown,
            @Nullable Double range,
            boolean enabled,
            @Nullable String instructions
    ) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.magicCost = magicCost;
        this.cooldown = cooldown;
        this.range = range;
        this.enabled = enabled;
        this.instructions = instructions;
    }

    @Override
    public @Nullable String getName() {
        return name;
    }

    @Override
    public @Nullable String getDescription() {
        return description;
    }

    @Override
    public @Nullable SpellCategory getCategory() {
        return category;
    }

    @Override
    public @Nullable Integer getMagicCost() {
        return magicCost;
    }

    /**
     * Assigns the task responsible for calling {@link #progress()}.
     * <p>
     * This task is cancelled automatically when the spell is removed.
     *
     * @param task The thread-safe task controlling spell progression
     */
    public void setProgressTask(ThreadUtil.ThreadTask task) {
        this.progressTask = task;
    }

    @Override
    public @Nullable Long getCooldown() {
        return cooldown;
    }

    @Override
    public @Nullable Double getRange() {
        return range;
    }

    @Override
    public @Nullable String getInstructions() {
        return instructions;
    }

    @Override
    public Boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public String getPermission() {
        return "spellcraft.spell." + name.toLowerCase().replace(" ", "");
    }

    @Override
    public Boolean canCast(SpellCaster caster) {
        if (!enabled) return false;
        if (!caster.hasMagic(magicCost)) return false;
        if (caster.isOnCooldown(this)) return false;
        return caster.getPlayer().hasPermission(getPermission());
    }

    @Override
    public SpellResult cast(SpellCaster caster) {
        if (!enabled) return SpellResult.FAILURE;
        if (!caster.hasMagic(magicCost)) return SpellResult.INSUFFICIENT_MAGIC;
        if (caster.isOnCooldown(this)) return SpellResult.ON_COOLDOWN;
        if (!caster.getPlayer().hasPermission(getPermission())) return SpellResult.NO_PERMISSION;

        startTime = System.currentTimeMillis();
        removed = false;

        SpellResult result = execute(caster);

        if (result.isSuccess()) {
            caster.consumeMagic(magicCost);
            caster.setCooldown(this, cooldown);
            SpellCraftPlugin.getInstance()
                    .getSpellManagerImpl()
                    .track(this);
        }

        return result;
    }

    /**
     * Executes the core logic of the spell.
     * <p>
     * This method is called once when the spell is successfully cast.
     *
     * @param caster The caster performing the spell
     * @return The result of the spell execution
     */
    protected abstract SpellResult execute(SpellCaster caster);

    /**
     * Called repeatedly while the spell is active.
     * <p>
     * Used for ongoing effects such as movement, checks, or animations.
     */
    public abstract void progress();

    /**
     * Removes the spell and performs cleanup.
     * <p>
     * Cancels the progress task and invokes {@link #onStop()} once.
     */
    public void remove() {
        if (removed) return;
        removed = true;

        if (progressTask != null) progressTask.cancel();
        onStop();
    }

    /**
     * Checks whether this spell has already been removed.
     *
     * @return {@code true} if the spell is no longer active
     */
    public boolean isRemoved() {
        return removed;
    }

    /**
     * Called once when the spell is first loaded or registered.
     * <p>
     * Use this for setup or initialization logic.
     */
    protected abstract void onLoad();

    /**
     * Called once when the spell is stopped or removed.
     * <p>
     * Use this to clean up entities, tasks, or other resources.
     */
    protected abstract void onStop();
}
