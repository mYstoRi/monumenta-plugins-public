package com.playmonumenta.plugins.abilities.alchemist.apothecary;

import com.playmonumenta.plugins.Plugin;
import com.playmonumenta.plugins.abilities.Ability;
import com.playmonumenta.plugins.classes.ClassAbility;
import com.playmonumenta.plugins.effects.PercentHeal;
import com.playmonumenta.plugins.particle.PPCircle;
import com.playmonumenta.plugins.particle.PPPeriodic;
import com.playmonumenta.plugins.particle.PartialParticle;
import com.playmonumenta.plugins.utils.AbsorptionUtils;
import com.playmonumenta.plugins.utils.ItemUtils;
import com.playmonumenta.plugins.utils.PlayerUtils;
import javax.annotation.Nullable;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class WardingRemedy extends Ability {

	public static final int WARDING_REMEDY_1_COOLDOWN = 20 * 30;
	public static final int WARDING_REMEDY_2_COOLDOWN = 20 * 25;
	public static final int WARDING_REMEDY_PULSES = 12;
	public static final int WARDING_REMEDY_PULSE_DELAY = 10;
	public static final int WARDING_REMEDY_MAX_ABSORPTION = 6;
	public static final int WARDING_REMEDY_ABSORPTION_DURATION = 20 * 30;
	private static final int WARDING_REMEDY_RANGE = 12;
	private static final double WARDING_REMEDY_HEAL_MULTIPLIER = 0.1;
	public static final double WARDING_REMEDY_ACTIVE_RADIUS = 6;
	private static final Color APOTHECARY_LIGHT_COLOR = Color.fromRGB(255, 255, 100);
	private static final Particle.DustOptions APOTHECARY_DARK_COLOR = new Particle.DustOptions(Color.fromRGB(83, 0, 135), 1.5f);

	public WardingRemedy(Plugin plugin, @Nullable Player player) {
		super(plugin, player, "Warding Remedy");
		mInfo.mScoreboardId = "WardingRemedy";
		mInfo.mLinkedSpell = ClassAbility.WARDING_REMEDY;
		mInfo.mCooldown = getAbilityScore() == 1 ? WARDING_REMEDY_1_COOLDOWN : WARDING_REMEDY_2_COOLDOWN;
		mInfo.mIgnoreCooldown = true;
		mInfo.mShorthandName = "WR";
		mInfo.mDescriptions.add("Swap hands while sneaking and holding an Alchemist's Bag to give players (including yourself) within a 6 block radius 1 absorption health per 0.5 seconds for 6 seconds, lasting 30 seconds, up to 6 absorption health. Cooldown: 30s.");
		mInfo.mDescriptions.add("You and allies in a 12 block radius passively gain 10% increased healing while having absorption health, and cooldown decreased to 25s.");
		mDisplayItem = new ItemStack(Material.GOLDEN_CARROT, 1);
	}

	@Override
	public void playerSwapHandItemsEvent(PlayerSwapHandItemsEvent event) {
		event.setCancelled(true);

		if (mPlayer == null || !mPlayer.isSneaking() || isTimerActive() || !ItemUtils.isAlchemistItem(mPlayer.getInventory().getItemInMainHand())) {
			return;
		}

		putOnCooldown();

		World world = mPlayer.getWorld();
		world.playSound(mPlayer.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1f, 2f);
		world.playSound(mPlayer.getLocation(), Sound.BLOCK_GLASS_BREAK, 1f, 0.5f);
		world.playSound(mPlayer.getLocation(), Sound.BLOCK_CONDUIT_ATTACK_TARGET, 1f, 1.5f);
		world.playSound(mPlayer.getLocation(), Sound.BLOCK_ENDER_CHEST_OPEN, 1f, 1.5f);

		new PartialParticle(Particle.END_ROD, mPlayer.getLocation().clone().add(0, 1, 0), 50, 0.25, 0.25, 0.25, 0.2).spawnAsPlayerActive(mPlayer);
		new PartialParticle(Particle.REDSTONE, mPlayer.getLocation(), 80, 2.8, 2.8, 2.8, new Particle.DustOptions(APOTHECARY_LIGHT_COLOR, 3.0f)).spawnAsPlayerActive(mPlayer);
		new PartialParticle(Particle.REDSTONE, mPlayer.getLocation().clone().add(0, 1, 0), 40, 0.35, 0.5, 0.35, APOTHECARY_DARK_COLOR).spawnAsPlayerActive(mPlayer);
		new PartialParticle(Particle.CLOUD, mPlayer.getLocation(), 60, 0.25, 0.25, 0.25, 0.2).spawnAsPlayerActive(mPlayer);
		new PartialParticle(Particle.REDSTONE, mPlayer.getLocation().clone().add(0, 0.15, 0), 100, 2.8, 0, 2.8, APOTHECARY_DARK_COLOR).spawnAsPlayerActive(mPlayer);

		new BukkitRunnable() {
			int mPulses = 0;
			int mTick = 10;
			final PPPeriodic mParticle = new PPPeriodic(Particle.END_ROD, mPlayer.getLocation()).count(1).delta(0.35, 0.15, 0.35).extra(0.05);

			@Override
			public void run() {
				if (mPlayer == null) {
					this.cancel();
					return;
				}

				mParticle.location(mPlayer.getLocation().add(0, 0.5, 0)).spawnAsPlayerActive(mPlayer);

				if (mTick >= WARDING_REMEDY_PULSE_DELAY) {
					world.playSound(mPlayer.getLocation(), Sound.ENTITY_PLAYER_HURT_ON_FIRE, 0.7f, 2f);

					new PPCircle(Particle.REDSTONE, mPlayer.getLocation().clone().add(0, 0.15, 0), 6).ringMode(true).count(1).data(APOTHECARY_DARK_COLOR).spawnAsPlayerActive(mPlayer);
					new PartialParticle(Particle.SPELL_INSTANT, mPlayer.getLocation().clone().add(0, 0.15, 0), 15, 2.8, 0, 2.8, 0).spawnAsPlayerActive(mPlayer);
					new PartialParticle(Particle.REDSTONE, mPlayer.getLocation(), 40, 2.8, 2.8, 2.8, new Particle.DustOptions(APOTHECARY_LIGHT_COLOR, 1.5f)).spawnAsPlayerActive(mPlayer);
					new PartialParticle(Particle.CLOUD, mPlayer.getLocation(), 20, 2.8, 2.8, 2.8, 0).spawnAsPlayerActive(mPlayer);

					for (Player p : PlayerUtils.playersInRange(mPlayer.getLocation(), WARDING_REMEDY_ACTIVE_RADIUS, true)) {
						AbsorptionUtils.addAbsorption(p, 1, WARDING_REMEDY_MAX_ABSORPTION, WARDING_REMEDY_ABSORPTION_DURATION);

						new PartialParticle(Particle.REDSTONE, p.getLocation().clone().add(0, 0.5, 0), 10, 0.35, 0.15, 0.35, new Particle.DustOptions(APOTHECARY_LIGHT_COLOR, 1.0f)).spawnAsPlayerActive(mPlayer);
						new PartialParticle(Particle.SPELL, p.getLocation().clone().add(0, 0.5, 0), 5, 0.35, 0.15, 0.35, 0).spawnAsPlayerActive(mPlayer);
					}
					mTick = 0;
					mPulses++;
					if (mPulses >= WARDING_REMEDY_PULSES) {
						this.cancel();
					}
				}

				mTick++;
			}
		}.runTaskTimer(mPlugin, 0, 1);
	}

	@Override
	public void periodicTrigger(boolean twoHertz, boolean oneSecond, int ticks) {
		//Triggers four times a second

		if (mPlayer == null || getAbilityScore() == 1) {
			return;
		}

		for (Player p : PlayerUtils.playersInRange(mPlayer.getLocation(), WARDING_REMEDY_RANGE, true).stream().filter(player -> AbsorptionUtils.getAbsorption(player) > 0).toList()) {
			mPlugin.mEffectManager.addEffect(p, "WardingRemedy", new PercentHeal(20 * 1, WARDING_REMEDY_HEAL_MULTIPLIER));
			new PartialParticle(Particle.SPELL_WITCH, p.getLocation().add(0, 1, 0), 2, 0.3, 0.5, 0.3).spawnAsPlayerBuff(mPlayer);
			new PartialParticle(Particle.REDSTONE, p.getLocation().add(0, 1, 0), 3, 0.4, 0.5, 0.4, APOTHECARY_DARK_COLOR).spawnAsPlayerBuff(mPlayer);
		}
	}

}
