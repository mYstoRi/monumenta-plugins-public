package com.playmonumenta.plugins.abilities.mage.arcanist;

import com.playmonumenta.plugins.Plugin;
import com.playmonumenta.plugins.abilities.Ability;
import com.playmonumenta.plugins.abilities.AbilityWithChargesOrStacks;
import com.playmonumenta.plugins.classes.ClassAbility;
import com.playmonumenta.plugins.effects.PercentSpeed;
import com.playmonumenta.plugins.events.AbilityCastEvent;
import com.playmonumenta.plugins.events.DamageEvent;
import com.playmonumenta.plugins.itemstats.abilities.CharmManager;
import com.playmonumenta.plugins.network.ClientModHandler;
import com.playmonumenta.plugins.particle.PartialParticle;
import com.playmonumenta.plugins.utils.MessagingUtils;
import java.util.HashMap;
import javax.annotation.Nullable;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class SagesInsight extends Ability implements AbilityWithChargesOrStacks {
	private static final int DECAY_TIMER = 20 * 4;
	private static final int MAX_STACKS = 8;
	private static final double SPEED_1 = 0.2;
	private static final double SPEED_2 = 0.3;
	private static final int SPEED_DURATION = 8 * 20;
	private static final String ATTR_NAME = "SagesExtraSpeedAttr";
	private static final int ABILITIES_COUNT_1 = 2;
	private static final int ABILITIES_COUNT_2 = 3;

	private static final float[] PITCHES = {1.6f, 1.8f, 1.6f, 1.8f, 2f};
	private static final Particle.DustOptions COLOR = new Particle.DustOptions(Color.fromRGB(222, 219, 36), 1.0f);

	private final int mResetSize;
	private final int mMaxStacks;
	private ClassAbility[] mResets;
	private final double mSpeed;
	public static String CHARM_STACKS = "Sage's Insight Stack Trigger Threshold";
	public static String CHARM_DECAY = "Sage's Insight Decay Duration";
	public static String CHARM_SPEED = "Sage's Insight Speed Amplifier";
	public static String CHARM_ABILITY = "Sage's Insight Ability Count";


	private HashMap<ClassAbility, Boolean> mStacksMap;

	public SagesInsight(Plugin plugin, @Nullable Player player) {
		super(plugin, player, "Sage's Insight");
		mInfo.mScoreboardId = "SagesInsight";
		mInfo.mShorthandName = "SgI";
		mInfo.mDescriptions.add(
			String.format("If an active spell hits an enemy, you gain an Arcane Insight. Insights stack up to %s, but decay every %ss of not gaining one. Once %s Insights are revealed, the Arcanist gains %s%% Speed for %ss and the cooldowns of the previous %s spells cast are refreshed. This sets your Insights back to 0.",
				MAX_STACKS,
				DECAY_TIMER / 20,
				MAX_STACKS,
				(int)(SPEED_1 * 100),
				SPEED_DURATION / 20,
				ABILITIES_COUNT_1
				));
		mInfo.mDescriptions.add(
			String.format("Sage's Insight now grants %s%% Speed and refreshes the cooldowns of your previous %s spells upon activating.",
				(int)(SPEED_2 * 100),
				ABILITIES_COUNT_2
				));
		mDisplayItem = new ItemStack(Material.ENDER_EYE, 1);
		mResetSize = (isLevelOne() ? ABILITIES_COUNT_1 : ABILITIES_COUNT_2) + (int) CharmManager.getLevel(player, CHARM_ABILITY);
		mMaxStacks = (int) CharmManager.getLevel(player, CHARM_STACKS) + MAX_STACKS;
		mResets = new ClassAbility[mResetSize];
		mSpeed = (isLevelOne() ? SPEED_1 : SPEED_2) + CharmManager.getLevelPercentDecimal(player, CHARM_SPEED);
		mStacksMap = new HashMap<>();
	}

	private int mStacks = 0;
	private int mTicksToStackDecay = 0;
	private int mArraySize = 0;

	@Override
	public void periodicTrigger(boolean twoHertz, boolean oneSecond, int ticks) {
		if (mPlayer != null && mStacks > 0) {
			mTicksToStackDecay -= 5;

			if (mTicksToStackDecay <= 0) {
				mTicksToStackDecay = DECAY_TIMER + CharmManager.getExtraDuration(mPlayer, CHARM_DECAY);
				mStacks--;
				MessagingUtils.sendActionBarMessage(mPlayer, "Sage's Insight Stacks: " + mStacks);
				ClientModHandler.updateAbility(mPlayer, this);
			}
		}
	}

	@Override
	public boolean onDamage(DamageEvent event, LivingEntity enemy) {
		ClassAbility ability = event.getAbility();
		if (ability == null) {
			return false;
		}
		mTicksToStackDecay = DECAY_TIMER + CharmManager.getExtraDuration(mPlayer, CHARM_DECAY);
		World world = mPlayer.getWorld();
		Location loc = mPlayer.getLocation();
		Location locD = event.getDamagee().getLocation().add(0, 1, 0);

		if (mStacks < mMaxStacks) {
			Boolean bool = mStacksMap.get(ability);
			if (bool != null && bool) {
				mStacks++;
				mStacksMap.put(ability, false);
				if (mStacks == mMaxStacks) {
					mPlugin.mEffectManager.addEffect(mPlayer, "SagesExtraSpeed", new PercentSpeed(SPEED_DURATION, mSpeed, ATTR_NAME));
					new PartialParticle(Particle.REDSTONE, loc, 20, 1.4, 1.4, 1.4, COLOR).spawnAsPlayerActive(mPlayer);
					new PartialParticle(Particle.VILLAGER_HAPPY, loc.clone().add(0, 2.1, 0), 20, 0.5, 0.1, 0.5, 0.1).spawnAsPlayerActive(mPlayer);
					for (int i = 0; i < PITCHES.length; i++) {
						float pitch = PITCHES[i];
						new BukkitRunnable() {
							@Override
							public void run() {
								world.playSound(loc, Sound.BLOCK_BELL_RESONATE, 1, pitch);
							}
						}.runTaskLater(mPlugin, i);
					}

					mStacks = 0;
					mArraySize = 0;
					for (ClassAbility s : mResets) {
						mPlugin.mTimers.removeCooldown(mPlayer, s);
					}
				} else {
					new PartialParticle(Particle.REDSTONE, locD, 15, 0.4, 0.4, 0.4, COLOR).spawnAsPlayerActive(mPlayer);
					new PartialParticle(Particle.EXPLOSION_NORMAL, locD, 15, 0, 0, 0, 0.2).spawnAsPlayerActive(mPlayer);
					MessagingUtils.sendActionBarMessage(mPlayer, "Sage's Insight Stacks: " + mStacks);
				}
				ClientModHandler.updateAbility(mPlayer, this);
			}
		}
		return false; // only used to check that an ability dealt damage, and does not cause more damage instances.
	}

	@Override
	public boolean abilityCastEvent(AbilityCastEvent event) {
		ClassAbility cast = event.getAbility();
		mStacksMap.put(cast, true);
		mArraySize = Math.min(mArraySize + 1, mResetSize);
		if (mArraySize == 0) {
			mResets[0] = cast;
			return true;
		} else if (mArraySize == 1) {
			mResets[1] = cast;
			return true;
		} else if (mArraySize == 2 && mResetSize == 2) {
			mResets[0] = mResets[1];
			mResets[1] = cast;
			return true;
		} else if (mArraySize == 2 && mResetSize == 3) {
			mResets[2] = cast;
			return true;
		} else if (mArraySize == 3 && mResetSize == 3) {
			mResets[0] = mResets[1];
			mResets[1] = mResets[2];
			mResets[2] = cast;
			return true;
		}
		return true;
	}

	@Override
	public int getCharges() {
		return mStacks;
	}

	@Override
	public int getMaxCharges() {
		return mMaxStacks;
	}

}
