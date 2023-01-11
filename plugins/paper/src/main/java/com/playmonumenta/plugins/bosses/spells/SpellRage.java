package com.playmonumenta.plugins.bosses.spells;

import com.playmonumenta.plugins.particle.PartialParticle;
import com.playmonumenta.plugins.utils.EntityUtils;
import com.playmonumenta.plugins.utils.PotionUtils;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class SpellRage extends SpellBaseAoE {

	public boolean mIsCasting = false;

	public SpellRage(Plugin plugin, LivingEntity launcher, int radius, int time) {
		super(plugin, launcher, radius, time, 160, false, Sound.UI_TOAST_OUT);
	}

	@Override
	protected void chargeAuraAction(Location loc) {
		World world = loc.getWorld();

		if (!mIsCasting) {
			world.playSound(loc, Sound.ENTITY_ZOMBIE_VILLAGER_CONVERTED, SoundCategory.HOSTILE, 1f, 0.6f);
			mIsCasting = true;
		}

		new PartialParticle(Particle.SPELL_WITCH, loc, 1, mRadius / 2.0, mRadius / 2.0, mRadius / 2.0, 0.05).spawnAsEntityActive(mLauncher);
	}

	@Override
	protected void chargeCircleAction(Location loc) {
		new PartialParticle(Particle.CRIT_MAGIC, loc, 1, 0.25, 0.25, 0.25, 0.1).spawnAsEntityActive(mLauncher);
	}

	@Override
	protected void outburstAction(Location loc) {
		World world = loc.getWorld();
		world.playSound(loc, Sound.BLOCK_BEACON_ACTIVATE, SoundCategory.HOSTILE, 1.5f, 1.5f);
		world.playSound(loc, Sound.ENTITY_RAVAGER_HURT, SoundCategory.HOSTILE, 1.5f, 0.5f);
		world.playSound(loc, Sound.ENTITY_BLAZE_SHOOT, SoundCategory.HOSTILE, 1.5f, 0.75f);
	}

	@Override
	protected void circleOutburstAction(Location loc) {
		new PartialParticle(Particle.SPELL_WITCH, loc, 1, 0.1, 0.1, 0.1, 0.3).spawnAsEntityActive(mLauncher);
		new PartialParticle(Particle.BUBBLE_POP, loc, 2, 0.25, 0.25, 0.25, 0.1).spawnAsEntityActive(mLauncher);
	}

	@Override
	protected void dealDamageAction(Location loc) {
		for (LivingEntity mob : EntityUtils.getNearbyMobs(mLauncher.getLocation(), mRadius)) {
			PotionUtils.applyPotion(mob, mob, new PotionEffect(PotionEffectType.SPEED, 120, 0, true, false));
			PotionUtils.applyPotion(mob, mob, new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 120, 1, true, false));
			new PartialParticle(Particle.SPELL_WITCH, mob.getLocation().add(0, mob.getHeight() / 2, 0), 15, 0.25, 0.45, 0.25, 1).spawnAsEntityActive(mLauncher);
			new PartialParticle(Particle.VILLAGER_ANGRY, mob.getLocation().add(0, mob.getHeight() / 2, 0), 5, 0.35, 0.5, 0.35, 0).spawnAsEntityActive(mLauncher);
		}

		mIsCasting = false;
	}

}
