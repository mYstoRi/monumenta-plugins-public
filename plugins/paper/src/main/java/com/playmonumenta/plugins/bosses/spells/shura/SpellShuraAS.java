package com.playmonumenta.plugins.bosses.spells.shura;

import com.playmonumenta.plugins.bosses.spells.Spell;
import com.playmonumenta.plugins.events.DamageEvent;
import com.playmonumenta.plugins.utils.BossUtils;
import com.playmonumenta.plugins.utils.PlayerUtils;
import java.util.Collections;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class SpellShuraAS extends Spell {

	private Plugin mPlugin;
	private LivingEntity mBoss;
	private double mRange;
	private Location mCenter;
	private Player mTarget;
	private boolean mMarked = false;
	private final Particle.DustOptions DARK_RED = new Particle.DustOptions(Color.fromRGB(150, 0, 0), 1.0f);

	public SpellShuraAS(Plugin plugin, LivingEntity boss, double range, Location center) {
		mPlugin = plugin;
		mBoss = boss;
		mRange = range;
		mCenter = center;
	}

	@Override
	public void run() {
		World world = mBoss.getWorld();

		//choose target
		List<Player> players = PlayerUtils.playersInRange(mCenter, mRange, true);
		Collections.shuffle(players);
		mTarget = players.get(0);
		world.playSound(mTarget.getLocation(), Sound.ENTITY_WITCH_AMBIENT, 1.4f, 0.5f);

		Bukkit.getScheduler().runTaskLater(mPlugin, () -> {
			mMarked = true;
			Location loc = mBoss.getLocation();
			world.spawnParticle(Particle.SMOKE_LARGE, loc.clone().add(0, 1, 0), 15, 0.25, 0.5, 0.25, 0.1f);
			world.spawnParticle(Particle.CRIT_MAGIC, loc.clone().add(0, 1, 0), 25, 0.3, 0.5, 0.3, 0.5f);
			world.playSound(loc, Sound.ENTITY_BLAZE_SHOOT, SoundCategory.HOSTILE, 1, 1);
			mBoss.teleport(mCenter.clone().add(0, -10, 0));
			mTarget.sendMessage(ChatColor.AQUA + "A chill runs down your spine.");
			mTarget.playSound(mTarget.getLocation(), Sound.ENTITY_GUARDIAN_HURT, SoundCategory.HOSTILE, 1f, 0.5f);

			BukkitRunnable tp = new BukkitRunnable() {
				int mT = 0;
				boolean mTrigger = true;
				@Override
				public void run() {
					mT += 10;
					world.spawnParticle(Particle.REDSTONE, mTarget.getLocation().add(0, 1.5, 0), 6, 0.5, 0.5, 0.5, 0, DARK_RED);
					if (mT >= 2 * 20 && mTrigger) {
						mTrigger = false;
						//tp behind
						Location loc = mTarget.getLocation();
						loc.setY(loc.getY() + 0.1f);
						Vector shift = loc.getDirection();
						shift.setY(0).normalize().multiply(-3);
						loc.add(shift);
						mBoss.teleport(loc);
						((Mob) mBoss).setTarget(mTarget);
						world.spawnParticle(Particle.SMOKE_LARGE, loc.clone().add(0, 1, 0), 15, 0.25, 0.5, 0.25, 0.1f);
						world.spawnParticle(Particle.CRIT_MAGIC, loc.clone().add(0, 1, 0), 25, 0.3, 0.5, 0.3, 0.5f);
						world.playSound(loc, Sound.ENTITY_BLAZE_SHOOT, SoundCategory.HOSTILE, 1, 1);
					}
					if (mT >= 7 * 20) {
						mMarked = false;
						mTarget.playSound(mTarget.getLocation(), Sound.ENTITY_ILLUSIONER_CAST_SPELL, SoundCategory.HOSTILE, 1, 1.25f);
						this.cancel();
					}
				}
			};
			tp.runTaskTimer(mPlugin, 0, 10);
			mActiveRunnables.add(tp);
		}, 50);
	}

	@Override
	public void onDamage(DamageEvent event, LivingEntity damagee) {
		if (damagee == mTarget && event.getType() == DamageEvent.DamageType.MELEE && mMarked) {
			mMarked = false;
			World world = mBoss.getWorld();
			world.playSound(damagee.getLocation(), Sound.ENTITY_BLAZE_DEATH, SoundCategory.HOSTILE, 1f, 2f);
			damagee.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 20 * 6, -1));
			BukkitRunnable dot = new BukkitRunnable() {
				int mT = 0;
				@Override public void run() {
					mT++;
					BossUtils.blockableDamage(mBoss, damagee, DamageEvent.DamageType.MAGIC, 10, "Advancing Shadows", null);
					if (mT >= 6) {
						this.cancel();
						damagee.removePotionEffect(PotionEffectType.WITHER);
					}
				}
			};
			dot.runTaskTimer(mPlugin, 0, 20);
			mActiveRunnables.add(dot);
		}
	}

	@Override
	public int cooldownTicks() {
		return 6 * 20;
	}
}