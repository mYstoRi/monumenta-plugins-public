package com.playmonumenta.plugins.abilities.mage.arcanist;

import com.playmonumenta.plugins.Constants;
import com.playmonumenta.plugins.Plugin;
import com.playmonumenta.plugins.abilities.Ability;
import com.playmonumenta.plugins.abilities.AbilityInfo;
import com.playmonumenta.plugins.abilities.AbilityManager;
import com.playmonumenta.plugins.classes.ClassAbility;
import com.playmonumenta.plugins.events.DamageEvent;
import com.playmonumenta.plugins.itemstats.ItemStatManager;
import com.playmonumenta.plugins.itemstats.abilities.CharmManager;
import com.playmonumenta.plugins.itemstats.attributes.SpellPower;
import com.playmonumenta.plugins.particle.PPPeriodic;
import com.playmonumenta.plugins.utils.DamageUtils;
import com.playmonumenta.plugins.utils.EntityUtils;
import com.playmonumenta.plugins.utils.FastUtils;
import com.playmonumenta.plugins.utils.ItemUtils;
import com.playmonumenta.plugins.utils.VectorUtils;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class CosmicMoonblade extends Ability {

	public static final String NAME = "Cosmic Moonblade";
	public static final ClassAbility ABILITY = ClassAbility.COSMIC_MOONBLADE;
	private static final float DAMAGE = 5.0f;
	private static final int SWINGS = 2;
	private static final int RADIUS = 5;
	private static final int COOLDOWN = 20 * 8;
	private static final double DOT_ANGLE = 0.6;
	public static final double REDUCTION_MULTIPLIER_1 = 0.05;
	public static final double REDUCTION_MULTIPLIER_2 = 0.1;
	public static final int CAP_TICKS_1 = (int) (0.5 * Constants.TICKS_PER_SECOND);
	public static final int CAP_TICKS_2 = (int) (1 * Constants.TICKS_PER_SECOND);
	private static final Particle.DustOptions FSWORD_COLOR1 = new Particle.DustOptions(Color.fromRGB(106, 203, 255), 1.0f);
	private static final Particle.DustOptions FSWORD_COLOR2 = new Particle.DustOptions(Color.fromRGB(168, 226, 255), 1.0f);
	public static final String CHARM_DAMAGE = "Cosmic Moonblade Damage";
	public static final String CHARM_SPELL_COOLDOWN = "Cosmic Moonblade Cooldown Reduction";
	public static final String CHARM_COOLDOWN = "Cosmic Moonblade Cooldown";
	public static final String CHARM_CAP = "Cosmic Moonblade Cooldown Cap";
	public static final String CHARM_RANGE = "Cosmic Moonblade Range";
	public static final String CHARM_SLASH = "Cosmic Moonblade Slashes";

	private final double mLevelReduction;
	private final int mLevelCap;

	public CosmicMoonblade(Plugin plugin, Player player) {
		super(plugin, player, "Cosmic Moonblade");
		mInfo.mScoreboardId = "CosmicMoonblade";
		mInfo.mShorthandName = "CM";
		mInfo.mDescriptions.add(
			String.format("Swap with a wand causes a wave of Arcane blades to hit every enemy within a %s block cone %s times (%s damage per hit) in rapid succession that if each land, reduce all your other skill cooldowns by %s%% (Max %ss). Cooldown: %ss.",
				RADIUS,
				SWINGS,
				(int)DAMAGE,
				(int)(REDUCTION_MULTIPLIER_1 * 100),
				CAP_TICKS_1 / 20.0,
				COOLDOWN / 20));
		mInfo.mDescriptions.add(
			String.format("Cooldown reduction is increased to %s%% (Max %ss) for blade.",
				(int)(REDUCTION_MULTIPLIER_2 * 100),
				CAP_TICKS_2 / 20));
		mInfo.mLinkedSpell = ABILITY;
		mInfo.mCooldown = CharmManager.getCooldown(player, CHARM_COOLDOWN, COOLDOWN);
		mInfo.mIgnoreCooldown = true;
		mDisplayItem = new ItemStack(Material.DIAMOND_SWORD, 1);
		mLevelReduction = (getAbilityScore() == 1 ? REDUCTION_MULTIPLIER_1 : REDUCTION_MULTIPLIER_2) + CharmManager.getLevelPercentDecimal(player, CHARM_SPELL_COOLDOWN);
		mLevelCap = (getAbilityScore() == 1 ? CAP_TICKS_1 : CAP_TICKS_2) + CharmManager.getExtraDuration(player, CHARM_CAP);
	}


	@Override
	public void playerSwapHandItemsEvent(PlayerSwapHandItemsEvent event) {
		if (mPlayer != null && ItemUtils.isWand(mPlayer.getInventory().getItemInMainHand())) {
			event.setCancelled(true);
			if (!isTimerActive() && !mPlayer.isSneaking()) {
				putOnCooldown();
				float charmDamage = (float) CharmManager.calculateFlatAndPercentValue(mPlayer, CHARM_DAMAGE, DAMAGE);
				float damage = SpellPower.getSpellDamage(mPlugin, mPlayer, charmDamage);
				double range = CharmManager.calculateFlatAndPercentValue(mPlayer, CHARM_RANGE, RADIUS);
				int swings = (int) CharmManager.getLevel(mPlayer, CHARM_SLASH) + SWINGS;
				ItemStatManager.PlayerItemStats playerItemStats = mPlugin.mItemStatManager.getPlayerItemStatsCopy(mPlayer);

				new BukkitRunnable() {
					int mTimes = 0;
					float mPitch = 1.2f;
					int mSwings = 0;

					@Override
					public void run() {
						mTimes++;
						mSwings++;
						Vector playerDir = mPlayer.getEyeLocation().getDirection().normalize();
						Location origin = mPlayer.getLocation();
						boolean cdr = true;
						for (LivingEntity mob : EntityUtils.getNearbyMobs(origin, range)) {
							if (cdr) {
								cdr = false;
								updateCooldowns(mLevelReduction);
							}
							Vector toMobVector = mob.getLocation().toVector().subtract(origin.toVector()).normalize();
							if (playerDir.dot(toMobVector) > DOT_ANGLE) {
								DamageUtils.damage(mPlayer, mob, new DamageEvent.Metadata(DamageEvent.DamageType.MAGIC, mInfo.mLinkedSpell, playerItemStats), damage, true, false, false);
							}
						}

						if (mTimes >= swings) {
							mPitch = 1.45f;
						}
						World world = mPlayer.getWorld();
						world.playSound(origin, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.75f, 0.8f);
						world.playSound(origin, Sound.ENTITY_WITHER_SHOOT, 0.75f, mPitch);

						new BukkitRunnable() {
							final int mI = mSwings;
							double mRoll;
							double mD = 45;
							boolean mInit = false;

							@Override
							public void run() {
								if (!mInit) {
									if (mI % 2 == 0) {
										mRoll = -8;
										mD = 45;
									} else {
										mRoll = 8;
										mD = 135;
									}
									mInit = true;
								}
								PPPeriodic particle1 = new PPPeriodic(Particle.REDSTONE, mPlayer.getLocation()).count(1).delta(0.1, 0.1, 0.1).data(FSWORD_COLOR1);
								PPPeriodic particle2 = new PPPeriodic(Particle.REDSTONE, mPlayer.getLocation()).count(1).delta(0.1, 0.1, 0.1).data(FSWORD_COLOR2);
								if (mI % 2 == 0) {
									Vector vec;
									for (double r = 1; r < range; r += 0.5) {
										for (double degree = mD; degree < mD + 30; degree += 5) {
											double radian1 = Math.toRadians(degree);
											vec = new Vector(FastUtils.cos(radian1) * r, 0, FastUtils.sin(radian1) * r);
											vec = VectorUtils.rotateZAxis(vec, mRoll);
											vec = VectorUtils.rotateXAxis(vec, origin.getPitch());
											vec = VectorUtils.rotateYAxis(vec, origin.getYaw());

											Location l = origin.clone().add(0, 1.25, 0).add(vec);
											particle1.location(l).spawnAsPlayerActive(mPlayer);
											particle2.location(l).spawnAsPlayerActive(mPlayer);
										}
									}

									mD += 30;
								} else {
									Vector vec;
									for (double r = 1; r < 5; r += 0.5) {
										for (double degree = mD; degree > mD - 30; degree -= 5) {
											double radian1 = Math.toRadians(degree);
											vec = new Vector(FastUtils.cos(radian1) * r, 0, FastUtils.sin(radian1) * r);
											vec = VectorUtils.rotateZAxis(vec, mRoll);
											vec = VectorUtils.rotateXAxis(vec, origin.getPitch());
											vec = VectorUtils.rotateYAxis(vec, origin.getYaw());

											Location l = origin.clone().add(0, 1.25, 0).add(vec);
											l.setPitch(-l.getPitch());
											particle1.location(l).spawnAsPlayerActive(mPlayer);
											particle2.location(l).spawnAsPlayerActive(mPlayer);
										}
									}
									mD -= 30;
								}

								if ((mD >= 135 && mI % 2 == 0) || (mD <= 45 && mI % 2 > 0)) {
									this.cancel();
								}
							}

						}.runTaskTimer(mPlugin, 0, 1);
						if (mTimes >= swings) {
							this.cancel();
						}
					}

				}.runTaskTimer(mPlugin, 0, 7);
			}
		}
	}

	public void updateCooldowns(double percent) {
		for (Ability abil : AbilityManager.getManager().getPlayerAbilities(mPlayer).getAbilities()) {
			AbilityInfo info = abil.getInfo();
			if (info.mLinkedSpell == mInfo.mLinkedSpell) {
				continue;
			}
			int totalCD = info.mCooldown;
			int reducedCD = Math.min((int) (totalCD * percent), mLevelCap);
			mPlugin.mTimers.updateCooldown(mPlayer, info.mLinkedSpell, reducedCD);
		}
	}
}
