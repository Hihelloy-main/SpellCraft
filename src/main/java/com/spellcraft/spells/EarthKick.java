package com.spellcraft.spells;

import com.spellcraft.SpellCraftPlugin;
import com.spellcraft.api.SpellCaster;
import com.spellcraft.api.SpellCategory;
import com.spellcraft.api.SpellResult;
import com.spellcraft.api.magic.MagicElement;
import com.spellcraft.core.AbstractSpell;
import com.spellcraft.util.DamageHandler;
import com.spellcraft.util.ThreadUtil;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class EarthKick extends AbstractSpell {

    private static final Set<FallingBlock> ACTIVE_BLOCKS = new HashSet<>();
    private final List<FallingBlock> spawnedBlocks = new ArrayList<>();

    private long duration;
    private double damage;
    private int maxBlocks;
    private double lavaMultiplier;

    private long startTime;
    private Location playerLocation;
    private Location cachedloc;

    public EarthKick() {
        super(
                "EarthKick",
                "Kick earth shards at your enemies",
                SpellCategory.COMBAT,
                SpellCraftPlugin.getInstance().getConfig().getInt("spells.earthkick.magic-cost", 25),
                SpellCraftPlugin.getInstance().getConfig().getLong("spells.earthkick.cooldown", 4000L),
                20.0,
                SpellCraftPlugin.getInstance().getConfig().getBoolean("spells.earthkick.enabled", true),
                "Sneak at earth in front of you"
        );
    }

    @Override
    protected SpellResult execute(SpellCaster caster) {

        Player player = caster.getPlayer();
        var config = SpellCraftPlugin.getInstance().getConfig();

        this.damage = config.getDouble("spells.earthkick.damage", 6.0);
        this.maxBlocks = config.getInt("spells.earthkick.max-blocks", 4);
        this.lavaMultiplier = config.getDouble("spells.earthkick.lava-multiplier", 1.5);
        this.duration = config.getLong("spells.earthkick.duration", 2500);

        Block target = player.getTargetBlockExact(3);
        if (target == null) return SpellResult.INVALID_TARGET;

        Material type = target.getType();

        if (!isEarth(type)) return SpellResult.INVALID_TARGET;

        if (type == Material.LAVA) {
            type = Material.MAGMA_BLOCK;
            damage *= lavaMultiplier;
        }

        spawnBlocks(player, target, type);

        startTime = System.currentTimeMillis();
        playerLocation = player.getLocation();

        ThreadUtil.ensureLocationTimer(cachedloc, () -> progress(player), 0, 1);

        player.getWorld().playSound(player.getLocation(),
                Sound.BLOCK_STONE_BREAK, 1f, 0.8f);

        return SpellResult.SUCCESS;
    }

    private void spawnBlocks(Player player, Block block, Material type) {

        Random random = new Random();

        for (int i = 0; i < maxBlocks; i++) {

            Location spawnLoc = block.getLocation().add(0.5, 1.2, 0.5);
            FallingBlock fb = player.getWorld().spawnFallingBlock(spawnLoc, type.createBlockData());

            fb.setDropItem(false);
            fb.setHurtEntities(false);

            Location dirLoc = player.getLocation().clone();
            dirLoc.setPitch(0);
            dirLoc.setYaw(dirLoc.getYaw() + random.nextInt(25) - 12);

            Vector velocity = dirLoc.getDirection();
            velocity.setY(Math.max(0.3, Math.random() / 2));
            velocity.setX(velocity.getX() / 1.2);
            velocity.setZ(velocity.getZ() / 1.2);

            fb.setVelocity(velocity);

            spawnedBlocks.add(fb);
            ACTIVE_BLOCKS.add(fb);
        }
    }

    private void progress(Player player) {

        Iterator<FallingBlock> iterator = spawnedBlocks.iterator();

        while (iterator.hasNext()) {

            FallingBlock fb = iterator.next();

            if (fb == null || fb.isDead() || !ACTIVE_BLOCKS.contains(fb)) {
                iterator.remove();
                continue;
            }

            cachedloc = fb.getLocation();
            Location loc = fb.getLocation();

            // Block crack particles (Spigot safe)
            loc.getWorld().spawnParticle(
                    Particle.BLOCK_CRACK,
                    loc,
                    3,
                    0.1, 0.1, 0.1,
                    fb.getBlockData()
            );

            for (Entity entity : loc.getWorld().getNearbyEntities(loc, 1.5, 1.5, 1.5)) {

                if (!(entity instanceof LivingEntity living)) continue;
                if (living.equals(player)) continue;
                if (!DamageHandler.isValidTarget(player, living)) continue;

                DamageHandler.damage(player, living, damage, getName(), getElement());
                living.setNoDamageTicks(0);

                fb.remove();
                ACTIVE_BLOCKS.remove(fb);
                iterator.remove();
                break;
            }
        }

        if (spawnedBlocks.isEmpty() ||
                System.currentTimeMillis() > startTime + duration) {
            remove();
        }
    }

    private boolean isEarth(Material mat) {
        return mat == Material.DIRT
                || mat == Material.GRASS_BLOCK
                || mat == Material.STONE
                || mat == Material.SAND
                || mat == Material.RED_SAND
                || mat == Material.GRAVEL
                || mat == Material.CLAY
                || mat == Material.TERRACOTTA
                || mat == Material.PACKED_MUD
                || mat == Material.MUD
                || mat == Material.LAVA;
    }

    @Override
    public void remove() {
        super.remove();
        for (FallingBlock fb : spawnedBlocks) {
            fb.remove();
            ACTIVE_BLOCKS.remove(fb);
        }
        spawnedBlocks.clear();
    }

    @Override public void progress() {}
    @Override protected void onLoad() {}
    @Override protected void onStop() {}

    @Override public boolean isSneakingAbility() { return true; }
    @Override public Action getAbilityActivationAction() { return null; }
    @Override public MagicElement getElement() { return MagicElement.EARTH; }

    @Override
    public @NotNull Location getLocation() {
        return playerLocation;
    }
}
