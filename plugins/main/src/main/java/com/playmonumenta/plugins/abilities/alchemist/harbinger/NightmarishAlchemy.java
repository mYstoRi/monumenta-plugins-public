package com.playmonumenta.plugins.abilities.alchemist.harbinger;

import java.util.Collection;
import java.util.List;
import java.util.Random;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Creature;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import com.playmonumenta.plugins.Plugin;
import com.playmonumenta.plugins.abilities.Ability;
import com.playmonumenta.plugins.utils.EntityUtils;

/*
 * Your Alchemist Potions deal +3/7 damage and have a 20%
 * chance of causing mobs to attack nearby mobs for 8 seconds.
 * If you splash 5 or more mobs, at least 1 is guaranteed to be
 * confused.
 */

public class NightmarishAlchemy extends Ability {
	private static final int NIGHTMARISH_ALCHEMY_1_DAMAGE = 3;
	private static final int NIGHTMARISH_ALCHEMY_2_DAMAGE = 7;
	private static final int NIGHTMARISH_ALCHEMY_CONFUSION_DURATION = 20 * 8;
	private static final float NIGHTMARISH_ALCHEMY_CONFUSION_CHANCE = 0.2f;

	private int mDamage;

	public NightmarishAlchemy(Plugin plugin, World world, Random random, Player player) {
		super(plugin, world, random, player);
		mInfo.scoreboardId = "Nightmarish";
		mDamage = getAbilityScore() == 1 ? NIGHTMARISH_ALCHEMY_1_DAMAGE : NIGHTMARISH_ALCHEMY_2_DAMAGE;
	}

	@Override
	public boolean PlayerSplashPotionEvent(Collection<LivingEntity> affectedEntities, ThrownPotion potion, PotionSplashEvent event) {
		boolean guaranteedApplicationApplied = false;
		if (potion.hasMetadata("AlchemistPotion")) {
			if (affectedEntities != null && !affectedEntities.isEmpty()) {
				int nightmarishAlchemy = getAbilityScore() == 1 ? NIGHTMARISH_ALCHEMY_1_DAMAGE : NIGHTMARISH_ALCHEMY_2_DAMAGE;
				int size = affectedEntities.size();
				for (LivingEntity entity : affectedEntities) {
					if (EntityUtils.isHostileMob(entity)) {
						guaranteedApplicationApplied = apply(entity, size, guaranteedApplicationApplied);
					}
				}
			}
			mWorld.spawnParticle(Particle.SPELL_WITCH, potion.getLocation(), 50, 1, 0, 1, 1);
		}

		return true;
	}

	public boolean apply(LivingEntity mob, int size, boolean guaranteedApplicationApplied) {
		EntityUtils.damageEntity(mPlugin, mob, mDamage, mPlayer);
		if (mob instanceof Creature) {
			if (mRandom.nextFloat() < NIGHTMARISH_ALCHEMY_CONFUSION_CHANCE || !guaranteedApplicationApplied && size >= 5) {
				EntityUtils.applyConfusion(mPlugin, NIGHTMARISH_ALCHEMY_CONFUSION_DURATION, mob);
				guaranteedApplicationApplied = true;
			}
		}

		return guaranteedApplicationApplied;
	}

}
