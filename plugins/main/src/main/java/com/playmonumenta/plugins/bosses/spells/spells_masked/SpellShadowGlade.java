package com.playmonumenta.plugins.bosses.spells.spells_masked;

import java.util.List;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import com.playmonumenta.plugins.bosses.spells.Spell;
import com.playmonumenta.plugins.bosses.utils.Utils;

public class SpellShadowGlade extends Spell {

	private final Plugin mPlugin;
	private final int mCount;
	private final Location mLoc;
	private final Random mRand = new Random();

	public SpellShadowGlade(Plugin plugin, Location loc, int count) {
		mPlugin = plugin;
		mLoc = loc;
		mCount = count;
	}

	@Override
	public void run() {
		boolean[] isQuadrantDone = new boolean[4];
		Location[] possibleLocs = new Location[4];
		int i = 0;
		for (int x = 0; x < 2; x++) {
			for (int y = 0; y < 2; y++) {
				possibleLocs[i] = new Location(mLoc.getWorld(), mLoc.getX() - 8.25 + x * 12.5, mLoc.getY() + 0.5, mLoc.getZ() - 8.25 + y * 12.5);
				i++;
			}
		}
		int chosen;
		int count = mCount;
		while (count > 0) {
			chosen = mRand.nextInt(4);
			if (!isQuadrantDone[chosen]) {
				count--;
				isQuadrantDone[chosen] = true;
				run(possibleLocs[chosen]);
			}
		}
	}

	@Override
	public int duration() {
		return 200; // 10 seconds
	}

	private void run(Location zoneStart) {
		final int PERIOD = 4;
		List<Player> pList = Utils.playersInRange(zoneStart, 40);

		BukkitRunnable loop = new BukkitRunnable() {
			private int j = 0;

			@Override
			public void run() {
				zoneStart.getWorld().playSound(zoneStart, Sound.ENTITY_PLAYER_HURT_ON_FIRE, 2f, 0.5f);
				zoneStart.getWorld().spawnParticle(Particle.FLAME, zoneStart, (j / mCount) * 10, 4, 0, 4, 0.01);
				if (j / mCount >= 24) {
					for (Player player : pList) {
						Location pPos = player.getLocation();
						pPos.getWorld().playSound(pPos, Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 1f, 0.8f);
					}
					zoneStart.getWorld().spawnParticle(Particle.LAVA, zoneStart, (j / mCount) * 10, 4, 0, 4, 0.01);
				}
				j++;

				if (j * PERIOD > 140) {
					this.cancel();

					for (Player player : pList) {
						Location pPos = player.getLocation();
						if (pPos.getX() > zoneStart.getX() - 8.25 && pPos.getX() < zoneStart.getX() + 8.25 && pPos.getZ() > zoneStart.getZ() - 8.25 && pPos.getZ() < zoneStart.getZ() + 8.25) {
							pPos.getWorld().playSound(pPos, Sound.ENTITY_GHAST_HURT, 1f, 0.7f);
							player.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 7 * 20, 3));
							player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 7 * 20, 1));
							player.setFireTicks(20 * 7);
						} else {
							pPos.getWorld().playSound(pPos, Sound.BLOCK_FIRE_EXTINGUISH, 1f, 0.85f);
						}
					}
				}
			}
		};

		loop.runTaskTimer(mPlugin, 4, 4);
		mActiveRunnables.add(loop);
	}
}
