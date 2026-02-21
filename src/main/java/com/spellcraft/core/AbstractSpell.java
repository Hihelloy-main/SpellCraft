package com.spellcraft.core;

import com.spellcraft.SpellCraftPlugin;
import com.spellcraft.api.Spell;
import com.spellcraft.api.SpellCaster;
import com.spellcraft.api.SpellCategory;
import com.spellcraft.api.SpellResult;
import com.spellcraft.api.magic.MagicElement;
import com.spellcraft.util.ThreadUtil;

import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.enginehub.linbus.stream.token.LinToken;

import java.util.Objects;


public abstract class AbstractSpell implements Spell {


    public final String name;
    public final String description;
    public final SpellCategory category;
    public final Integer magicCost;
    public final Long cooldown;
    public final Double range;
    public boolean enabled;
    public final String instructions;


    public SpellCaster caster;

    public volatile boolean removed;
    public long startTime;

    public ThreadUtil.ThreadTask progressTask;



    protected AbstractSpell(
            String name,
            String description,
            SpellCategory category,
            Integer magicCost,
            Long cooldown,
            Double range,
            boolean enabled,
            String instructions
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
    public final SpellResult cast(SpellCaster caster) {


        if (!enabled)
            return SpellResult.FAILURE;


        Player player = caster.getPlayer();


        if (!canCast(caster))
            return SpellResult.FAILURE;


        if (caster.isOnCooldown(this))
            return SpellResult.ON_COOLDOWN;


        if (!caster.hasMagic(magicCost))
            return SpellResult.INSUFFICIENT_MAGIC;


        if (getElement() != null) {

            String perm =
                    "spellcraft.element."
                            + getElement().getName().toLowerCase();

            if (!player.hasPermission(perm))
                return SpellResult.NO_PERMISSION;

        }


        this.caster = caster;

        this.removed = false;

        this.startTime = System.currentTimeMillis();



        SpellResult result = execute(caster);



        if (!result.isSuccess())
            return result;



        caster.consumeMagic(magicCost);



        SpellCraftPlugin
                .getInstance()
                .getSpellManagerImpl()
                .track(this);



        return SpellResult.SUCCESS;

    }



    protected abstract SpellResult execute(SpellCaster caster);




    public void remove() {


        if (removed)
            return;


        removed = true;



        if (progressTask != null)
            progressTask.cancel();



        onStop();



        if (caster != null)
            caster.setCooldown(this, cooldown);


    }



    public boolean isRemoved() {

        return removed;

    }




    public void setProgressTask(ThreadUtil.ThreadTask task) {

        this.progressTask = task;

    }



    public long getAliveTime() {

        return System.currentTimeMillis() - startTime;

    }



    protected SpellCaster getCaster() {

        return caster;

    }


    public abstract void progress();

    protected abstract void onLoad();

    protected abstract void onStop();


    @Override
    public Boolean canCast(SpellCaster caster) {

        return true;

    }


    @Override
    public String getPermission() {

        return "spellcraft.spell." + name.toLowerCase();

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
    public String getName() {

        return name;

    }


    @Override
    public String getDescription() {

        return description;

    }


    @Override
    public SpellCategory getCategory() {

        return category;

    }


    @Override
    public Integer getMagicCost() {

        return magicCost;

    }


    @Override
    public Long getCooldown() {

        return cooldown;

    }


    @Override
    public Double getRange() {

        return range;

    }


    @Override
    public String getInstructions() {

        return instructions;

    }


}