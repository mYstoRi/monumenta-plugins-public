package com.playmonumenta.plugins.bosses.spells;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;

public class SpellShadePossessedParticle extends Spell {

	private final World mWorld;
	private final LivingEntity mBoss;

	public SpellShadePossessedParticle(LivingEntity boss) {
		mWorld = boss.getWorld();
		mBoss = boss;
	}

	@Override
	public void run() {
		Location loc = mBoss.getLocation().add(0, 1, 0);
		mWorld.spawnParticle(Particle.SOUL_FIRE_FLAME, loc, 6, 0.25, 0.6, 0.25, 0);
	}

	@Override
	public int duration() {
		// This is the period of run()
		return 5;
	}
}
