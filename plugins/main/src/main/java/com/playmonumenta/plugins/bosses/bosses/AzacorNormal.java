package com.playmonumenta.plugins.bosses.bosses;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.playmonumenta.plugins.bosses.BossBarManager;
import com.playmonumenta.plugins.bosses.BossBarManager.BossHealthAction;
import com.playmonumenta.plugins.bosses.SpellManager;
import com.playmonumenta.plugins.bosses.spells.Spell;
import com.playmonumenta.plugins.bosses.spells.SpellBaseLaser;
import com.playmonumenta.plugins.bosses.spells.SpellBlockBreak;
import com.playmonumenta.plugins.bosses.spells.SpellChangeFloor;
import com.playmonumenta.plugins.bosses.spells.SpellConditionalTeleport;
import com.playmonumenta.plugins.bosses.spells.SpellFireball;
import com.playmonumenta.plugins.bosses.spells.SpellMinionResist;
import com.playmonumenta.plugins.bosses.utils.DamageUtils;
import com.playmonumenta.plugins.bosses.utils.SerializationUtils;
import com.playmonumenta.plugins.bosses.utils.Utils;

public class AzacorNormal extends BossAbilityGroup {
	public static final String identityTag = "boss_azacornorm";
	public static final int detectionRange = 50;

	private final LivingEntity mBoss;
	private final Location mSpawnLoc;
	private final Location mEndLoc;
	private final Random mRand = new Random();

	public static BossAbilityGroup deserialize(Plugin plugin, LivingEntity boss) throws Exception {
		return SerializationUtils.statefulBossDeserializer(boss, identityTag, (spawnLoc, endLoc) -> {
			return new AzacorNormal(plugin, boss, spawnLoc, endLoc);
		});
	}

	@Override
	public String serialize() {
		return SerializationUtils.statefulBossSerializer(mSpawnLoc, mEndLoc);
	}

	public AzacorNormal(Plugin plugin, LivingEntity boss, Location spawnLoc, Location endLoc) {
		mBoss = boss;
		mSpawnLoc = spawnLoc;
		mEndLoc = endLoc;

		mBoss.setRemoveWhenFarAway(false);
		SpellManager activeSpells = new SpellManager(Arrays.asList(
			new SpellChangeFloor(plugin, mBoss, spawnLoc, 24, 3, Material.LAVA, 400),
			new SpellFireball(plugin, boss, detectionRange, 40, 1, 100, 2.0f, true, false,
			                  // Launch effect
			                  (Location loc) -> {
			                      loc.getWorld().playSound(loc, Sound.ENTITY_GHAST_SHOOT, 1.0f, 1.0f);
			                      loc.getWorld().spawnParticle(Particle.VILLAGER_ANGRY, loc, 10, 0.4, 0.4, 0.4, 0);
			                  }),
			new SpellBaseLaser(plugin, boss, detectionRange, 100, false, false, 160,
			                   // Tick action per player
			                   (Player player, int ticks, boolean blocked) -> {
			                       player.playSound(player.getLocation(), Sound.UI_TOAST_IN, 2, 0.5f + (ticks / 80f) * 1.5f);
			                       boss.getLocation().getWorld().playSound(boss.getLocation(), Sound.UI_TOAST_IN, 2, 0.5f + (ticks / 80f) * 1.5f);
			                       if (ticks == 0) {
			                           boss.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 110, 4), true);
			                       }
			                   },
			                   // Particles generated by the laser
			                   (Location loc) -> {
			                       loc.getWorld().spawnParticle(Particle.SMOKE_LARGE, loc, 1, 0.02, 0.02, 0.02, 0);
			                       loc.getWorld().spawnParticle(Particle.FLAME, loc, 1, 0.02, 0.02, 0.02, 0);
			                   },
			                   // Damage generated at the end of the attack
			                   (Player player, Location loc, boolean blocked) -> {
			                       loc.getWorld().playSound(loc, Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 1f, 1.5f);
			                       loc.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, loc, 30, 0, 0, 0, 0.3);

			                       if (!blocked) {
			                           DamageUtils.damagePercent(mBoss, player, 0.74);
			                       } else {
			                           Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "summon tnt " + loc.getX() + " " + loc.getY() + " " + loc.getZ() + " {Fuse:0}");
			                       }
			                   })
		));
		List<Spell> passiveSpells = Arrays.asList(
			new SpellBlockBreak(mBoss),
			// Teleport the boss to spawnLoc if he gets too far away from where he spawned
			new SpellConditionalTeleport(mBoss, spawnLoc, b -> spawnLoc.distance(b.getLocation()) > 80),
			// Teleport the boss to spawnLoc if he is stuck in bedrock
			new SpellConditionalTeleport(mBoss, spawnLoc, b -> b.getLocation().getBlock().getType() == Material.BEDROCK ||
			                                                   b.getLocation().add(0, 1, 0).getBlock().getType() == Material.BEDROCK ||
			                                                   b.getLocation().getBlock().getType() == Material.LAVA),
			new SpellMinionResist(mBoss, new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 30, 1), detectionRange, 5)
		);

		Map<Integer, BossHealthAction> events = new HashMap<Integer, BossHealthAction>();
		int player_count = Utils.playersInRange(mSpawnLoc, detectionRange).size();
		events.put(100, (mBoss) -> {
			randomMinion("tellraw @s [\"\",{\"text\":\"I took his offer and I remain here. Even assassins cannot make me face death! What makes you think you can fare better?\",\"color\":\"dark_red\"}]");
			if (player_count >= 3) {
				randomMinion("");
			}
		});
		events.put(50, (mBoss) -> {
			randomMinion("tellraw @s [\"\",{\"text\":\"Foolish mortals! Your efforts mean nothing. You cannot stop me. You will fall, just like the rest.\",\"color\":\"dark_red\"}]");
			if (player_count >= 3) {
				randomMinion("");
			}
		});
		BossBarManager bossBar = new BossBarManager(plugin, boss, detectionRange, BarColor.RED, BarStyle.SEGMENTED_10, events);

		super.constructBoss(plugin, identityTag, mBoss, activeSpells, passiveSpells, detectionRange, bossBar);
	}


	private void randomMinion(String tellraw) {
		Azacor.randomMinion(tellraw, mSpawnLoc, mRand, 100.0 + Utils.playersInRange(mSpawnLoc, detectionRange).size() * 50.0);
	}

	@Override
	public void init() {
		int bossTargetHp = 0;
		int player_count = Utils.playersInRange(mBoss.getLocation(), detectionRange).size();
		int hp_del = 512;
		int armor = (int)(Math.sqrt(player_count * 2) - 1);
		while (player_count > 0) {
			bossTargetHp = bossTargetHp + hp_del;
			hp_del = hp_del / 2;
			player_count--;
		}
		mBoss.getAttribute(Attribute.GENERIC_ARMOR).setBaseValue(armor);
		mBoss.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(bossTargetHp);
		mBoss.setHealth(bossTargetHp);

		//launch event related spawn commands
		Utils.executeCommandOnNearbyPlayers(mBoss.getLocation(), detectionRange, "effect @s minecraft:blindness 2 2");
		Utils.executeCommandOnNearbyPlayers(mBoss.getLocation(), detectionRange, "title @s title [\"\",{\"text\":\"Azacor\",\"color\":\"dark_gray\",\"bold\":true}]");
		Utils.executeCommandOnNearbyPlayers(mBoss.getLocation(), detectionRange, "title @s subtitle [\"\",{\"text\":\"The Dark Summoner\",\"color\":\"gray\",\"bold\":true}]");
		Utils.executeCommandOnNearbyPlayers(mBoss.getLocation(), detectionRange, "playsound minecraft:entity.wither.spawn master @s ~ ~ ~ 10 0.7");
	}

	@Override
	public void death() {
		Utils.executeCommandOnNearbyPlayers(mBoss.getLocation(), detectionRange, "playsound minecraft:entity.enderdragon.death master @s ~ ~ ~ 100 0.8");
		Utils.executeCommandOnNearbyPlayers(mBoss.getLocation(), detectionRange, "tellraw @s [\"\",{\"text\":\"No... it's not possible... I was promised...\",\"color\":\"dark_red\"}]");
		for (Player player : Utils.playersInRange(mBoss.getLocation(), detectionRange)) {
			player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20 * 10, 2));
			player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 20 * 10, 2));
		}
		mEndLoc.getBlock().setType(Material.REDSTONE_BLOCK);
	}
}
