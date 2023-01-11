package com.playmonumenta.plugins.bosses.bosses;

import com.playmonumenta.plugins.bosses.BossBarManager;
import com.playmonumenta.plugins.bosses.BossBarManager.BossHealthAction;
import com.playmonumenta.plugins.bosses.SpellManager;
import com.playmonumenta.plugins.bosses.spells.Spell;
import com.playmonumenta.plugins.bosses.spells.SpellBaseLaser;
import com.playmonumenta.plugins.bosses.spells.SpellBlockBreak;
import com.playmonumenta.plugins.bosses.spells.SpellConditionalTeleport;
import com.playmonumenta.plugins.bosses.spells.SpellGenericCharge;
import com.playmonumenta.plugins.bosses.spells.SpellShieldStun;
import com.playmonumenta.plugins.bosses.spells.SpellTpSwapPlaces;
import com.playmonumenta.plugins.events.DamageEvent;
import com.playmonumenta.plugins.events.DamageEvent.DamageType;
import com.playmonumenta.plugins.particle.PartialParticle;
import com.playmonumenta.plugins.utils.BossUtils;
import com.playmonumenta.plugins.utils.EntityUtils;
import com.playmonumenta.plugins.utils.MessagingUtils;
import com.playmonumenta.plugins.utils.PlayerUtils;
import com.playmonumenta.plugins.utils.SerializationUtils;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;

public final class Varcosa extends BossAbilityGroup {
	public static final String identityTag = "boss_varcosa";
	public static final int detectionRange = 110;

	private final Location mSpawnLoc;
	private final Location mEndLoc;
	private double mCoef;

	public static BossAbilityGroup deserialize(Plugin plugin, LivingEntity boss) throws Exception {
		return SerializationUtils.statefulBossDeserializer(boss, identityTag, (spawnLoc, endLoc) -> {
			return new Varcosa(plugin, boss, spawnLoc, endLoc);
		});
	}

	@Override
	public String serialize() {
		return SerializationUtils.statefulBossSerializer(mSpawnLoc, mEndLoc);
	}

	public Varcosa(Plugin plugin, LivingEntity boss, Location spawnLoc, Location endLoc) {
		super(plugin, identityTag, boss);
		mSpawnLoc = spawnLoc;
		mEndLoc = endLoc;
		boss.setRemoveWhenFarAway(false);

		boss.addScoreboardTag("Boss");

		SpellManager activeSpells = new SpellManager(Arrays.asList(
			new SpellGenericCharge(plugin, boss, detectionRange, 15.0F),
			new SpellTpSwapPlaces(plugin, boss, 5),
			new SpellBaseLaser(plugin, boss, detectionRange, 100, false, false, 160,

				// Tick action per player
				(LivingEntity player, int ticks, boolean blocked) -> {
					player.getWorld().playSound(player.getLocation(), Sound.UI_TOAST_IN, SoundCategory.HOSTILE, 2, 0.5f + (ticks / 80f) * 1.5f);
					boss.getLocation().getWorld().playSound(boss.getLocation(), Sound.UI_TOAST_IN, SoundCategory.HOSTILE, 2, 0.5f + (ticks / 80f) * 1.5f);

					if (ticks == 0) {
						boss.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 110, 4));
					}
				},

				// Particles generated by the laser
				(Location loc) -> {
					new PartialParticle(Particle.CLOUD, loc, 1, 0.02, 0.02, 0.02, 0).spawnAsEntityActive(boss);
					new PartialParticle(Particle.FLAME, loc, 1, 0.04, 0.04, 0.04, 1).spawnAsEntityActive(boss);
				},

				// Damage generated at the end of the attack
				(LivingEntity target, Location loc, boolean blocked) -> {
					loc.getWorld().playSound(loc, Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, SoundCategory.HOSTILE, 1f, 1.5f);
					new PartialParticle(Particle.FIREWORKS_SPARK, loc, 300, 0.8, 0.8, 0.8, 0).spawnAsEntityActive(boss);

					if (!blocked) {
						BossUtils.blockableDamage(boss, target, DamageType.MAGIC, 30);
						// Shields don't stop fire!
						EntityUtils.applyFire(com.playmonumenta.plugins.Plugin.getInstance(), 4 * 20, target, boss);
					}
				})));

		List<Spell> passiveSpells = Arrays.asList(
			new SpellBlockBreak(boss),
			new SpellShieldStun(6 * 20),
			// Teleport the boss to spawnLoc if he is stuck in bedrock
			new SpellConditionalTeleport(boss, spawnLoc, b -> b.getLocation().getBlock().getType() == Material.BEDROCK ||
				                                                  b.getLocation().add(0, 1, 0).getBlock().getType() == Material.BEDROCK ||
				                                                  b.getLocation().getBlock().getType() == Material.LAVA)
		);

		Map<Integer, BossHealthAction> events = new HashMap<Integer, BossHealthAction>();
		events.put(100, (mob) -> {
			PlayerUtils.executeCommandOnNearbyPlayers(spawnLoc, detectionRange, "tellraw @s [\"\",{\"text\":\"" + ChatColor.GOLD + "[Captain Varcosa] " + ChatColor.WHITE + "Yarharhar! Thank ye fer comin' and seein' me, but now this will be ye grave as well!\",\"color\":\"purple\"}]");
		});
		events.put(50, (mob) -> {
			PlayerUtils.executeCommandOnNearbyPlayers(spawnLoc, detectionRange, "tellraw @s [\"\",{\"text\":\"" + ChatColor.GOLD + "[Captain Varcosa] " + ChatColor.WHITE + "I will hang ye out to dry!\",\"color\":\"purple\"}]");
		});
		events.put(25, (mob) -> {
			PlayerUtils.executeCommandOnNearbyPlayers(spawnLoc, detectionRange, "tellraw @s [\"\",{\"text\":\"" + ChatColor.GOLD + "[Captain Varcosa] " + ChatColor.WHITE + "Yarharhar! Do ye feel it as well? That holy fleece? It be waitin' fer me!\",\"color\":\"purple\"}]");
		});
		events.put(10, (mob) -> {
			PlayerUtils.executeCommandOnNearbyPlayers(spawnLoc, detectionRange, "tellraw @s [\"\",{\"text\":\"" + ChatColor.GOLD + "[Captain Varcosa] " + ChatColor.WHITE + "I be too close ter be stoppin' now! Me greed will never die!\",\"color\":\"purple\"}]");
		});
		BossBarManager bossBar = new BossBarManager(plugin, boss, detectionRange, BarColor.RED, BarStyle.SEGMENTED_10, events);

		super.constructBoss(activeSpells, passiveSpells, detectionRange, bossBar);

		new BukkitRunnable() {

			@Override
			public void run() {
				if (mBoss.isDead() || !mBoss.isValid()) {
					this.cancel();
				}
				int playerCount = BossUtils.getPlayersInRangeForHealthScaling(mBoss, detectionRange);
				mCoef = BossUtils.healthScalingCoef(playerCount, 0.5, 0.5);
			}
		}.runTaskTimer(mPlugin, 0, 100);
	}

	@Override
	public void init() {
		int bossTargetHp = 1650;
		int playerCount = BossUtils.getPlayersInRangeForHealthScaling(mBoss, detectionRange);
		mCoef = BossUtils.healthScalingCoef(playerCount, 0.5, 0.5);
		EntityUtils.setAttributeBase(mBoss, Attribute.GENERIC_MAX_HEALTH, bossTargetHp);
		mBoss.setHealth(bossTargetHp);

		for (Player player : PlayerUtils.playersInRange(mBoss.getLocation(), detectionRange, true)) {
			MessagingUtils.sendBoldTitle(player, ChatColor.DARK_PURPLE + "Captain Varcosa", ChatColor.RED + "The Legendary Pirate King");
			player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 2, false, true, true));
			player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, SoundCategory.HOSTILE, 10, 0.7f);
		}
	}

	@Override
	public void death(@Nullable EntityDeathEvent event) {
		for (Player player : PlayerUtils.playersInRange(mBoss.getLocation(), detectionRange, true)) {
			player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_DEATH, SoundCategory.HOSTILE, 100.0f, 0.8f);
			player.sendMessage(Component.text("", NamedTextColor.WHITE)
				.append(Component.text("[Captain Varcosa] ", NamedTextColor.GOLD))
				.append(Component.text("Ye thought I be the one in control here? Yarharhar! N'argh me lad, I merely be its pawn! But now me soul can rest, and ye will be its next meal! Yarharhar!")));
		}
		mEndLoc.getBlock().setType(Material.REDSTONE_BLOCK);
	}

	//Reduce damage taken for each player by a percent
	@Override
	public void onHurt(DamageEvent event) {
		event.setDamage(event.getDamage() / mCoef);
	}
}
