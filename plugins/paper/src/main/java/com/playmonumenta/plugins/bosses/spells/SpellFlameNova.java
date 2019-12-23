package com.playmonumenta.plugins.bosses.spells;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.playmonumenta.plugins.utils.BossUtils;
import com.playmonumenta.plugins.utils.PlayerUtils;

public class SpellFlameNova extends SpellBaseAoE {

	public SpellFlameNova(Plugin plugin, LivingEntity launcher, int radius, int time) {
		super(plugin, launcher, radius, time, 0, false, Sound.BLOCK_FIRE_AMBIENT,
			(Location loc) -> {
				World world = loc.getWorld();
				world.spawnParticle(Particle.LAVA, loc, 1, ((double) radius) / 2, ((double) radius) / 2, ((double) radius) / 2, 0.05);
			},
			(Location loc) -> {
				World world = loc.getWorld();
				world.spawnParticle(Particle.FLAME, loc, 1, 0.25, 0.25, 0.25, 0.1);
			},
			(Location loc) -> {
				World world = loc.getWorld();
				world.playSound(loc, Sound.ENTITY_WITHER_SHOOT, 1.5f, 0.65F);
			},
			(Location loc) -> {
				World world = loc.getWorld();
				world.spawnParticle(Particle.FLAME, loc, 1, 0.1, 0.1, 0.1, 0.3);
				world.spawnParticle(Particle.SMOKE_NORMAL, loc, 4, 0.25, 0.25, 0.25, 0.1);
			},
			(Location loc) -> {
				for (Player player : PlayerUtils.playersInRange(launcher.getLocation(), radius)) {
					BossUtils.bossDamage(launcher, player, 11);
					player.setFireTicks(80);
				}
			}
		);
	}
}
