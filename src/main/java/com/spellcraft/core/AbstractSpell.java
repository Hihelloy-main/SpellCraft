package com.spellcraft.core;

import com.spellcraft.SpellCraftPlugin;
import com.spellcraft.api.Spell;
import com.spellcraft.api.SpellCategory;
import com.spellcraft.api.SpellCaster;
import com.spellcraft.api.SpellResult;
import com.spellcraft.util.ThreadUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

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

    protected SpellCaster caster;

    private ThreadUtil.ThreadTask progressTask;


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

        return "spellcraft.spell." +
                name.toLowerCase().replace(" ", "");

    }


    public void setProgressTask(ThreadUtil.ThreadTask task) {

        this.progressTask = task;

    }


    @Override
    public Boolean canCast(SpellCaster caster) {

        if (!enabled)
            return false;


        Player player = caster.getPlayer();


        if (!player.hasPermission(getPermission()))
            return false;


        if (getElement() != null) {

            String perm =
                    "spellcraft.element." +
                            getElement().getName().toLowerCase();

            if (!player.hasPermission(perm))
                return false;

        }


        if (caster.getHouse() != null) {

            if (!player.hasPermission(
                    caster.getHouse().getPermission()))
                return false;

        }


        if (!caster.hasMagic(magicCost))
            return false;


        if (caster.isOnCooldown(this))
            return false;


        return true;

    }


    @Override
    public SpellResult cast(SpellCaster caster) {

        if (!enabled)
            return SpellResult.FAILURE;


        Player player = caster.getPlayer();


        if (!player.hasPermission(getPermission()))
            return SpellResult.NO_PERMISSION;


        if (getElement() != null) {

            String elementPerm =
                    "spellcraft.element." +
                            getElement().getName().toLowerCase();

            if (!player.hasPermission(elementPerm))
                return SpellResult.NO_PERMISSION;

        }


        if (caster.getHouse() != null) {

            if (!player.hasPermission(
                    caster.getHouse().getPermission()))
                return SpellResult.NO_PERMISSION;

        }


        if (!caster.hasMagic(magicCost))
            return SpellResult.INSUFFICIENT_MAGIC;


        if (caster.isOnCooldown(this))
            return SpellResult.ON_COOLDOWN;


        this.caster = caster;

        this.startTime = System.currentTimeMillis();

        this.removed = false;


        SpellResult result = execute(caster);


        if (result.isSuccess()) {

            SpellCraftPlugin
                    .getInstance()
                    .getSpellManagerImpl()
                    .track(this);

        }


        return result;

    }


    public void remove() {

        if (removed)
            return;


        removed = true;


        if (progressTask != null)
            progressTask.cancel();


        if (caster != null)
            caster.setCooldown(this, cooldown);


        onStop();

    }


    public boolean isRemoved() {

        return removed;

    }


    protected abstract SpellResult execute(SpellCaster caster);


    public abstract void progress();


    protected abstract void onLoad();


    protected abstract void onStop();

}
