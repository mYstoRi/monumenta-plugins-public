package com.playmonumenta.plugins.bosses.spells.kaul;

import com.playmonumenta.plugins.bosses.bosses.PrimordialElementalKaulBoss;
import com.playmonumenta.plugins.bosses.spells.SpellBaseBolt;
import com.playmonumenta.plugins.events.DamageEvent.DamageType;
import com.playmonumenta.plugins.utils.BossUtils;
import com.playmonumenta.plugins.utils.LocationUtils;
import com.playmonumenta.plugins.utils.MovementUtils;
import com.playmonumenta.plugins.utils.PlayerUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class SpellPrimordialBolt extends SpellBaseBolt {

	public SpellPrimordialBolt(Plugin plugin, LivingEntity boss) {
		super(plugin, boss, 20 * 2, 20 * 5, 1.1, PrimordialElementalKaulBoss.detectionRange, 0.5, false, true, 1, 1,
				(Entity entity, int tick) -> {
					if (entity.getLocation().getY() > 60) {
						return;
					}
					float t = tick / 15;
					World world = boss.getWorld();
					world.spawnParticle(Particle.LAVA, boss.getLocation(), 1, 0.35, 0, 0.35, 0.005);
					world.spawnParticle(Particle.BLOCK_CRACK, boss.getLocation(), 3, 0, 0, 0, 0.5,
					Material.STONE.createBlockData());
					world.playSound(boss.getLocation(), Sound.UI_TOAST_IN, 10, t);
					boss.removePotionEffect(PotionEffectType.SLOW);
					boss.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 1));
				},

				(Entity entity) -> {
					World world = boss.getWorld();
					world.playSound(boss.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 5, 0.5f);
					world.spawnParticle(Particle.FLAME, boss.getLocation().add(0, 1, 0), 80, 0.2, 0.45,
					0.2, 0.2);
					world.spawnParticle(Particle.SMOKE_LARGE, boss.getLocation().add(0, 1, 0), 30, 0.2,
					0.45, 0.2, 0.1);
				},

				(Location loc) -> {
					if (loc.getY() > 60) {
						return;
					}
					World world = boss.getWorld();
					world.spawnParticle(Particle.BLOCK_DUST, loc, 6, 0.45, 0.45, 0.45, 0.25,
					Material.STONE.createBlockData());
					world.spawnParticle(Particle.EXPLOSION_LARGE, loc, 2, 0.2, 0.2, 0.2, 0.25);
					for (Block block : LocationUtils.getNearbyBlocks(loc.getBlock(), 1)) {
						if (block.getType().isSolid()) {
							Material material = block.getType();
							if (material == Material.SMOOTH_SANDSTONE
							|| material == Material.SMOOTH_RED_SANDSTONE
							|| material == Material.NETHERRACK
							|| material == Material.MAGMA_BLOCK) {
								block.setType(Material.AIR);
							}
						}
					}
				},

				(Player player, Location loc, boolean blocked) -> {
					if (player == null || player.getLocation().getY() > 60 || (loc != null && loc.getY() > 60)) {
						return;
					}
					if (!blocked) {
						//TODO no official name for this ability yet so there's no death message
						BossUtils.dualTypeBlockableDamage(boss, player, DamageType.BLAST, DamageType.MAGIC, 30, 0.5);
						player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20 * 15, 1));
						player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 20 * 15, 0));
					} else {
						for (Player p : PlayerUtils.playersInRange(loc, 2.5, true)) {
							if (p.getLocation().getY() <= 60) {
								BossUtils.blockableDamage(boss, p, DamageType.BLAST, 16 * 0.5);
								BossUtils.blockableDamage(boss, p, DamageType.MAGIC, 16 * 0.5);
								MovementUtils.knockAway(loc, p, 0.3f, false);
								p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20 * 10, 1));
								p.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 20 * 10, 0));
							}
						}
					}
					World world = boss.getWorld();
					world.spawnParticle(Particle.FLAME, loc, 125, 0, 0, 0, 0.175);
					world.spawnParticle(Particle.SMOKE_LARGE, loc, 50, 0, 0, 0, 0.25);
					world.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1, 0.9f);
				},

				// Only allow targetting players below y=60
				(Player player) -> {
					return player != null && player.getLocation().getY() < 60;
				}
			);
	}
}