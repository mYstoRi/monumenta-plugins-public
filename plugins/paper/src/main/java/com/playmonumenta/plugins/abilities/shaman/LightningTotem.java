package com.playmonumenta.plugins.abilities.shaman;

import com.playmonumenta.plugins.Plugin;
import com.playmonumenta.plugins.abilities.AbilityInfo;
import com.playmonumenta.plugins.abilities.AbilityTrigger;
import com.playmonumenta.plugins.abilities.AbilityTriggerInfo;
import com.playmonumenta.plugins.abilities.shaman.hexbreaker.HexbreakerPassive;
import com.playmonumenta.plugins.abilities.shaman.soothsayer.SoothsayerPassive;
import com.playmonumenta.plugins.classes.ClassAbility;
import com.playmonumenta.plugins.classes.Shaman;
import com.playmonumenta.plugins.events.DamageEvent;
import com.playmonumenta.plugins.itemstats.ItemStatManager;
import com.playmonumenta.plugins.listeners.AuditListener;
import com.playmonumenta.plugins.particle.PPCircle;
import com.playmonumenta.plugins.particle.PPLightning;
import com.playmonumenta.plugins.particle.PartialParticle;
import com.playmonumenta.plugins.utils.AbilityUtils;
import com.playmonumenta.plugins.utils.DamageUtils;
import com.playmonumenta.plugins.utils.EntityUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.Nullable;

public class LightningTotem extends TotemAbility {

	private static final int COOLDOWN = 20 * 20;
	private static final int TOTEM_DURATION = 10 * 20;
	private static final int INTERVAL = 2 * 20;
	private static final int AOE_RANGE = 6;
	private static final int DAMAGE_1 = 12;
	private static final int DAMAGE_2 = 20;
	public static String TOTEM_NAME = "Lightning Totem";
	public static final Particle.DustOptions YELLOW = new Particle.DustOptions(Color.fromRGB(255, 255, 0), 1.25f);

	private double mDamage;
	private @Nullable LivingEntity mTarget = null;

	public static final AbilityInfo<LightningTotem> INFO =
		new AbilityInfo<>(LightningTotem.class, "Lightning Totem", LightningTotem::new)
			.linkedSpell(ClassAbility.LIGHTNING_TOTEM)
			.scoreboardId("LightningTotem")
			.shorthandName("LT")
			.descriptions(
				String.format("Press right click with a melee weapon while sneaking to summon a lightning totem. The totem will target a " +
					"mob within %s blocks with priority towards boss and elite mobs and deal %s magic damage every %s seconds. Duration: %ss. Cooldown: %ss.",
					AOE_RANGE,
					DAMAGE_1,
					INTERVAL / 20,
					TOTEM_DURATION / 20,
					COOLDOWN / 20
				),
				String.format("The totem deals %s magic damage per hit.",
					DAMAGE_2)
			)
			.simpleDescription("Summon a totem which will strike a mob within range for high damage throughout its duration.")
			.cooldown(COOLDOWN)
			.addTrigger(new AbilityTriggerInfo<>("cast", "cast", LightningTotem::cast, new AbilityTrigger(AbilityTrigger.Key.RIGHT_CLICK).sneaking(true)
				.keyOptions(AbilityTrigger.KeyOptions.NO_USABLE_ITEMS)
				.keyOptions(AbilityTrigger.KeyOptions.NO_PICKAXE)))
			.displayItem(Material.YELLOW_WOOL);

	public LightningTotem(Plugin plugin, Player player) {
		super(plugin, player, INFO, "Lightning Totem Projectile", "LightningTotem");
		if (!player.hasPermission(Shaman.PERMISSION_STRING)) {
			AuditListener.logSevere(player.getName() + " has accessed shaman abilities incorrectly, class has been reset, please report to developers.");
			AbilityUtils.resetClass(player);
		}
		mDamage = isLevelOne() ? DAMAGE_1 : DAMAGE_2;
		mDamage *= HexbreakerPassive.damageBuff(mPlayer);
		mDamage *= SoothsayerPassive.damageBuff(mPlayer);
	}

	@Override
	public int getTotemDuration() {
		return TOTEM_DURATION;
	}

	@Override
	public void onTotemTick(int ticks, ArmorStand stand, World world, Location standLocation, ItemStatManager.PlayerItemStats stats) {
		new PPCircle(Particle.REDSTONE, standLocation, AOE_RANGE).data(YELLOW).ringMode(true).count(15).spawnAsPlayerActive(mPlayer);
		if (ticks % INTERVAL == 0) {
			if (mTarget == null || mTarget.isDead() || !mTarget.isValid() || mTarget.getLocation().distance(standLocation) > AOE_RANGE) {
				List<LivingEntity> affectedMobs = new ArrayList<>(EntityUtils.getNearbyMobsInSphere(standLocation, AOE_RANGE, null));
				affectedMobs.removeIf(mob -> DamageUtils.isImmuneToDamage(mob, DamageEvent.DamageType.MAGIC));
				if (!affectedMobs.isEmpty()) {
					if (affectedMobs.size() > 1) {
						affectedMobs.remove(mMobStuckWithEffect);
					}
					Collections.shuffle(affectedMobs);
					for (LivingEntity mob : affectedMobs) {
						if (mTarget == null) {
							mTarget = mob;
						}
						if (EntityUtils.isBoss(mob) || EntityUtils.isElite(mob)) {
							mTarget = mob;
							break;
						}
					}
				}
			}
			if (mTarget != null && !mTarget.isDead() && mTarget.getLocation().distance(standLocation) <= AOE_RANGE) {
				DamageUtils.damage(mPlayer, mTarget, new DamageEvent.Metadata(DamageEvent.DamageType.MAGIC, mInfo.getLinkedSpell(), stats), mDamage, true, false, false);
				PPLightning lightning = new PPLightning(Particle.END_ROD, mTarget.getLocation())
					.count(8).duration(3);
				mPlayer.getWorld().playSound(mTarget.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1, 1.25f);
				lightning.init(6, 2.5, 0.3, 0.3);
				lightning.spawnAsPlayerActive(mPlayer);
			}
		}
	}

	@Override
	public void onTotemExpire(World world, Location standLocation) {
		new PartialParticle(Particle.FLASH, standLocation, 3, 0.3, 1.1, 0.3, 0.15).spawnAsPlayerActive(mPlayer);
		world.playSound(standLocation, Sound.ENTITY_BLAZE_DEATH, 0.7f, 0.5f);
		mTarget = null;
	}

}