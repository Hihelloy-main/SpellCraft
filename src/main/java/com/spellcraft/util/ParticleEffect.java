package com.spellcraft.util;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.ItemStack;

/**
 * Enum representing all supported particle effects in the game.
 * Wraps {@link Particle} and provides utility methods for displaying
 * the particle at a location with optional data and offsets.
 */
public enum ParticleEffect {

	ASH(Particle.ASH),

	// Block data particles
	BLOCK_CRACK(Particle.BLOCK_CRACK),
	BLOCK_DUST(Particle.BLOCK_DUST),
	FALLING_DUST(Particle.FALLING_DUST),

	BUBBLE_COLUMN_UP(Particle.BUBBLE_COLUMN_UP),
	BUBBLE_POP(Particle.BUBBLE_POP),
	CAMPFIRE_COSY_SMOKE(Particle.CAMPFIRE_COSY_SMOKE),
	CAMPFIRE_SIGNAL_SMOKE(Particle.CAMPFIRE_SIGNAL_SMOKE),
	CLOUD(Particle.CLOUD),
	COMPOSTER(Particle.COMPOSTER),
	CRIMSON_SPORE(Particle.CRIMSON_SPORE),
	CRIT(Particle.CRIT),
	CURRENT_DOWN(Particle.CURRENT_DOWN),
	DAMAGE_INDICATOR(Particle.DAMAGE_INDICATOR),
	DOLPHIN(Particle.DOLPHIN),
	DRAGON_BREATH(Particle.DRAGON_BREATH),
	DRIP_LAVA(Particle.DRIP_LAVA),
	DRIP_WATER(Particle.DRIP_WATER),
	DRIPPING_HONEY(Particle.DRIPPING_HONEY),
	DRIPPING_OBSIDIAN_TEAR(Particle.DRIPPING_OBSIDIAN_TEAR),
	ENCHANTMENT_TABLE(Particle.ENCHANTMENT_TABLE),
	END_ROD(Particle.END_ROD),

	EXPLOSION_HUGE(Particle.EXPLOSION_HUGE),
	EXPLOSION_LARGE(Particle.EXPLOSION_LARGE),
	EXPLOSION_NORMAL(Particle.EXPLOSION_NORMAL),

	FALLING_HONEY(Particle.FALLING_HONEY),
	FALLING_LAVA(Particle.FALLING_LAVA),
	FALLING_NECTAR(Particle.FALLING_NECTAR),
	FALLING_OBSIDIAN_TEAR(Particle.FALLING_OBSIDIAN_TEAR),
	FALLING_WATER(Particle.FALLING_WATER),

	FIREWORKS_SPARK(Particle.FIREWORKS_SPARK),
	FLAME(Particle.FLAME),
	FLASH(Particle.FLASH),
	HEART(Particle.HEART),

	// Item data
	ITEM_CRACK(Particle.ITEM_CRACK),

	LANDING_HONEY(Particle.LANDING_HONEY),
	LANDING_LAVA(Particle.LANDING_LAVA),
	LANDING_OBSIDIAN_TEAR(Particle.LANDING_OBSIDIAN_TEAR),
	LAVA(Particle.LAVA),
	MOB_APPEARANCE(Particle.MOB_APPEARANCE),
	NAUTILUS(Particle.NAUTILUS),
	NOTE(Particle.NOTE),
	PORTAL(Particle.PORTAL),

	// Dust options
	REDSTONE(Particle.REDSTONE),

	REVERSE_PORTAL(Particle.REVERSE_PORTAL),
	SLIME(Particle.SLIME),
	SMOKE_NORMAL(Particle.SMOKE_NORMAL),
	SMOKE_LARGE(Particle.SMOKE_LARGE),
	SNEEZE(Particle.SNEEZE),
	SNOW_SHOVEL(Particle.SNOW_SHOVEL),
	SNOWBALL(Particle.SNOWBALL),
	SOUL(Particle.SOUL),
	SOUL_FIRE_FLAME(Particle.SOUL_FIRE_FLAME),

	SPELL(Particle.SPELL),
	SPELL_INSTANT(Particle.SPELL_INSTANT),
	SPELL_MOB(Particle.SPELL_MOB),
	SPELL_WITCH(Particle.SPELL_WITCH),

	SPIT(Particle.SPIT),
	SQUID_INK(Particle.SQUID_INK),
	SUSPENDED(Particle.SUSPENDED),
	SUSPENDED_DEPTH(Particle.SUSPENDED_DEPTH),
	SWEEP_ATTACK(Particle.SWEEP_ATTACK),
	TOTEM(Particle.TOTEM),
	TOWN_AURA(Particle.TOWN_AURA),

	VILLAGER_ANGRY(Particle.VILLAGER_ANGRY),
	VILLAGER_HAPPY(Particle.VILLAGER_HAPPY),

	WARPED_SPORE(Particle.WARPED_SPORE),
	WATER_BUBBLE(Particle.WATER_BUBBLE),
	WATER_DROP(Particle.WATER_DROP),
	WATER_SPLASH(Particle.WATER_SPLASH),
	WATER_WAKE(Particle.WATER_WAKE),
	WHITE_ASH(Particle.WHITE_ASH);

	private final Particle particle;
	private final Class<?> dataClass;

	/**
	 * Constructs a ParticleEffect wrapping the given {@link Particle}.
	 *
	 * @param particle the underlying {@link Particle}
	 */
	ParticleEffect(Particle particle) {
		this.particle = particle;
		this.dataClass = particle.getDataType();
	}

	/** @return the underlying {@link Particle} of this effect */
	public Particle getParticle() {
		return particle;
	}

	/**
	 * Display the particle at the given location with default settings.
	 *
	 * @param loc the {@link Location} where the particle will appear
	 * @param amount the number of particles to spawn
	 */
	public void display(Location loc, int amount) {
		display(loc, amount, 0, 0, 0, 0, null);
	}

	/**
	 * Display the particle at the location with offsets.
	 *
	 * @param loc the {@link Location} where the particle will appear
	 * @param amount the number of particles to spawn
	 * @param offsetX X-axis offset
	 * @param offsetY Y-axis offset
	 * @param offsetZ Z-axis offset
	 */
	public void display(Location loc, int amount, double offsetX, double offsetY, double offsetZ) {
		display(loc, amount, offsetX, offsetY, offsetZ, 0, null);
	}

	/**
	 * Display the particle at the location with offsets and extra data.
	 *
	 * @param loc the {@link Location} where the particle will appear
	 * @param amount the number of particles to spawn
	 * @param offsetX X-axis offset
	 * @param offsetY Y-axis offset
	 * @param offsetZ Z-axis offset
	 * @param extra particle speed or size, depending on the particle type
	 */
	public void display(Location loc, int amount, double offsetX, double offsetY, double offsetZ, double extra) {
		display(loc, amount, offsetX, offsetY, offsetZ, extra, null);
	}

	/**
	 * Display the particle at the location with offsets and optional data.
	 *
	 * @param loc the {@link Location} where the particle will appear
	 * @param amount the number of particles to spawn
	 * @param offsetX X-axis offset
	 * @param offsetY Y-axis offset
	 * @param offsetZ Z-axis offset
	 * @param data additional data for the particle (e.g., {@link BlockData} or {@link ItemStack})
	 */
	public void display(Location loc, int amount, double offsetX, double offsetY, double offsetZ, Object data) {
		display(loc, amount, offsetX, offsetY, offsetZ, 0, data);
	}

	/**
	 * Display the particle at the location with full customization.
	 *
	 * @param loc the {@link Location} where the particle will appear
	 * @param amount the number of particles to spawn
	 * @param offsetX X-axis offset
	 * @param offsetY Y-axis offset
	 * @param offsetZ Z-axis offset
	 * @param extra particle speed or size, depending on the particle type
	 * @param data additional data for the particle (e.g., {@link BlockData} or {@link ItemStack})
	 */
	public void display(
			Location loc,
			int amount,
			double offsetX,
			double offsetY,
			double offsetZ,
			double extra,
			Object data
	) {
		if (loc == null || loc.getWorld() == null) return;

		if (data == null || dataClass == Void.class || !dataClass.isInstance(data)) {
			loc.getWorld().spawnParticle(
					particle,
					loc,
					amount,
					offsetX,
					offsetY,
					offsetZ,
					extra
			);
		} else {
			loc.getWorld().spawnParticle(
					particle,
					loc,
					amount,
					offsetX,
					offsetY,
					offsetZ,
					extra,
					data
			);
		}
	}
}
