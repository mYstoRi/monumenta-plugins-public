package com.playmonumenta.plugins.depths.abilities.frostborn;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.playmonumenta.plugins.Plugin;
import com.playmonumenta.plugins.classes.ClassAbility;
import com.playmonumenta.plugins.depths.DepthsTree;
import com.playmonumenta.plugins.depths.DepthsUtils;
import com.playmonumenta.plugins.depths.abilities.DepthsAbility;
import com.playmonumenta.plugins.potion.PotionManager.PotionID;
import com.playmonumenta.plugins.utils.AbilityUtils;
import com.playmonumenta.plugins.utils.AbsorptionUtils;
import com.playmonumenta.plugins.utils.EntityUtils;
import com.playmonumenta.plugins.utils.MessagingUtils;
import com.playmonumenta.plugins.utils.MovementUtils;

import net.md_5.bungee.api.ChatColor;

public class Cryobox extends DepthsAbility {

	public static final String ABILITY_NAME = "Cryobox";
	public static final int[] ABSORPTION = {1, 2, 3, 4, 5};
	public static final int COOLDOWN = 90 * 20;
	private static final int TRIGGER_HEALTH = 6;
	private static final float RADIUS = 4.0f;
	private static final float KNOCKBACK_SPEED = 0.7f;
	private static final int DURATION = 12 * 20;
	private static final int ICE_DURATION = 20 * 20;

	public Cryobox(Plugin plugin, Player player) {
		super(plugin, player, ABILITY_NAME);
		mDisplayItem = Material.GHAST_TEAR;
		mTree = DepthsTree.FROSTBORN;
		mInfo.mCooldown = COOLDOWN;
		mInfo.mLinkedSpell = ClassAbility.CRYOBOX;
	}

	@Override
	public boolean playerDamagedByLivingEntityEvent(EntityDamageByEntityEvent event) {
		if (event.isCancelled()) {
			return true;
		}

		execute(event);
		return true;
	}

	@Override
	public boolean playerDamagedByProjectileEvent(EntityDamageByEntityEvent event) {
		if (event.isCancelled()) {
			return true;
		}

		execute(event);
		return true;
	}

	/*
	 * Works against all types of damage
	 */
	@Override
	public boolean playerDamagedEvent(EntityDamageEvent event) {
		// Do not process cancelled damage events
		if (event.isCancelled() || event instanceof EntityDamageByEntityEvent) {
			return true;
		}

		execute(event);
		return true;
	}

	private void execute(EntityDamageEvent event) {
		if (AbilityUtils.isBlocked(event)) {
			return;
		}

		// Calculate whether this effect should not be run based on player health.
		// It is intentional that Cryobox saves you from death if you take a buttload of damage somehow.
		double healthRemaining = mPlayer.getHealth() + AbsorptionUtils.getAbsorption(mPlayer) - EntityUtils.getRealFinalDamage(event);

		// Health is less than 0 but does not penetrate the absorption shield
		boolean dealDamageLater = healthRemaining < 0 && healthRemaining > -4 * (ABSORPTION[mRarity - 1] + 1);


		if (healthRemaining > TRIGGER_HEALTH) {
			return;
		} else if (dealDamageLater) {
			// The player has taken fatal damage BUT will be saved by the absorption, so set damage to 0 and compensate later
			event.setCancelled(true);
		}

		// Put on cooldown before processing results to prevent infinite recursion
		putOnCooldown();

		// Conditions match - prismatic shield
		for (LivingEntity mob : EntityUtils.getNearbyMobs(mPlayer.getLocation(), RADIUS, mPlayer)) {
			MovementUtils.knockAway(mPlayer, mob, KNOCKBACK_SPEED);
		}

		mPlugin.mPotionManager.addPotion(mPlayer, PotionID.ABILITY_SELF,
		                                 new PotionEffect(PotionEffectType.ABSORPTION, DURATION, ABSORPTION[mRarity - 1], true, true));
		World world = mPlayer.getWorld();
		world.spawnParticle(Particle.FIREWORKS_SPARK, mPlayer.getLocation().add(0, 1.15, 0), 150, 0.2, 0.35, 0.2, 0.5);
		world.spawnParticle(Particle.SPELL_INSTANT, mPlayer.getLocation().add(0, 1.15, 0), 100, 0.2, 0.35, 0.2, 1);
		world.playSound(mPlayer.getLocation(), Sound.ITEM_TOTEM_USE, 1, 1.35f);
		MessagingUtils.sendActionBarMessage(mPlugin, mPlayer, "Cryobox has been activated");

		if (dealDamageLater) {
			mPlayer.setHealth(1);
			AbsorptionUtils.subtractAbsorption(mPlayer, 1 - (float) healthRemaining);
		}

		//Ripped this straight from frost giant, epic
		Location center = mPlayer.getLocation();
		Location[] locs = new Location[] {
			//First Layer
			center.clone().add(1, 0, 0),
			center.clone().add(-1, 0, 0),
			center.clone().add(0, 0, 1),
			center.clone().add(0, 0, -1),

			//Second Layer
			center.clone().add(1, 1, 0),
			center.clone().add(-1, 1, 0),
			center.clone().add(0, 1, 1),
			center.clone().add(0, 1, -1),

			//Top & Bottom
			center.clone().add(0, 2, 0),
			center.clone().add(0, -1, 0),
		};

		Material[] mats = new Material[locs.length];
		for (int i = 0; i < locs.length; i++) {
			Location loc = locs[i];
			mats[i] = loc.getBlock().getType();
			DepthsUtils.spawnIceTerrain(locs[i], ICE_DURATION);
		}
	}



	@Override
	public String getDescription(int rarity) {
		return "When your health drops below " + TRIGGER_HEALTH / 2 + " hearts, gain Absorption " + DepthsUtils.getRarityColor(rarity) + (ABSORPTION[rarity - 1] + 1) + ChatColor.WHITE + " for " + DURATION / 20 + " seconds, knock enemies away, and encase yourself in a cage of ice. Cooldown: " + COOLDOWN / 20 + "s.";
	}

	@Override
	public DepthsTree getDepthsTree() {
		return DepthsTree.FROSTBORN;
	}
}

