package com.playmonumenta.plugins.cosmetics.skills.cleric;

import com.google.common.collect.ImmutableMap;
import com.playmonumenta.plugins.Plugin;
import com.playmonumenta.plugins.classes.ClassAbility;
import com.playmonumenta.plugins.cosmetics.Cosmetic;
import com.playmonumenta.plugins.cosmetics.skills.CosmeticSkill;
import com.playmonumenta.plugins.particle.PartialParticle;
import com.playmonumenta.plugins.utils.ParticleUtils;
import org.bukkit.*;
import org.bukkit.entity.Player;

public class HandOfLightCS implements CosmeticSkill {

	public static final ImmutableMap<String, HandOfLightCS> SKIN_LIST = ImmutableMap.<String, HandOfLightCS>builder()
		.put(TouchOfEntropyCS.NAME, new TouchOfEntropyCS())
		.build();

	@Override
	public Cosmetic getCosmetic() {
		return null;
	}

	@Override
	public ClassAbility getAbilityName() {
		return ClassAbility.HAND_OF_LIGHT;
	}

	@Override
	public Material getDisplayItem() {
		return Material.PINK_DYE;
	}

	public void lightHealEffect(Player mPlayer, Location loc) {
		new PartialParticle(Particle.HEART, loc.add(0, 1, 0), 10, 0.7, 0.7, 0.7, 0.001).spawnAsPlayerActive(mPlayer);
		new PartialParticle(Particle.END_ROD, loc.add(0, 1, 0), 10, 0.7, 0.7, 0.7, 0.001).spawnAsPlayerActive(mPlayer);
		mPlayer.getWorld().playSound(loc, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 2.0f, 1.6f);
		mPlayer.getWorld().playSound(loc, Sound.ENTITY_PLAYER_LEVELUP, 0.05f, 1.0f);
	}

	public void lightHealCastEffect(World world, Location userLoc, Plugin mPlugin, Player mPlayer, float radius, double angle) {
		final float HEALING_RADIUS = radius;
		final double HEALING_DOT_ANGLE = angle;
		world.playSound(userLoc, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 2.0f, 1.6f);
		world.playSound(userLoc, Sound.ENTITY_PLAYER_LEVELUP, 0.05f, 1.0f);
		ParticleUtils.explodingConeEffect(mPlugin, mPlayer, HEALING_RADIUS, Particle.SPIT, 0.35f, Particle.PORTAL, 3.0f, HEALING_DOT_ANGLE);
	}

	public void lightDamageEffect(Player mPlayer, Location loc) {
		new PartialParticle(Particle.DAMAGE_INDICATOR, loc.add(0, 1, 0), 10, 0.7, 0.7, 0.7, 0.001).spawnAsPlayerActive(mPlayer);
		new PartialParticle(Particle.END_ROD, loc.add(0, 1, 0), 10, 0.7, 0.7, 0.7, 0.001).spawnAsPlayerActive(mPlayer);
		mPlayer.getWorld().playSound(loc, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.5f, 0.8f);
	}

	public void lightDamageCastEffect(World world, Location userLoc, Plugin mPlugin, Player mPlayer, float radius, double angle) {
		final float DAMAGE_RADIUS = radius;
		final double HEALING_DOT_ANGLE = angle;
		world.playSound(userLoc, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.5f, 0.8f);
		ParticleUtils.explodingConeEffect(mPlugin, mPlayer, DAMAGE_RADIUS, Particle.SPIT, 0.35f, Particle.PORTAL, 3.0f, HEALING_DOT_ANGLE);
	}
}
