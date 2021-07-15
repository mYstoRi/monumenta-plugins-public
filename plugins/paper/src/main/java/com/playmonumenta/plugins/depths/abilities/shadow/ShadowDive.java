package com.playmonumenta.plugins.depths.abilities.shadow;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.playmonumenta.plugins.Plugin;
import com.playmonumenta.plugins.abilities.AbilityTrigger;
import com.playmonumenta.plugins.classes.ClassAbility;
import com.playmonumenta.plugins.depths.DepthsTree;
import com.playmonumenta.plugins.depths.DepthsUtils;
import com.playmonumenta.plugins.depths.abilities.DepthsAbility;
import com.playmonumenta.plugins.depths.abilities.DepthsTrigger;
import com.playmonumenta.plugins.utils.AbilityUtils;
import com.playmonumenta.plugins.utils.EntityUtils;

import net.md_5.bungee.api.ChatColor;

public class ShadowDive extends DepthsAbility {

	public static final String ABILITY_NAME = "Cloak of Shadows";
	public static final int COOLDOWN = 20 * 15;
	public static final int WEAKEN_DURATION = 20 * 6;
	public static final int[] DAMAGE = {8, 10, 12, 14, 16};
	public static final int[] STEALTH_DURATION = {30, 35, 40, 45, 50};
	public static final double[] WEAKEN_AMPLIFIER = {0.2, 0.25, 0.3, 0.35, 0.4};
	private static final double VELOCITY = 0.7;
	private static final double RADIUS = 5.0;

	public ShadowDive(Plugin plugin, Player player) {
		super(plugin, player, ABILITY_NAME);
		mDisplayItem = Material.BLACK_CONCRETE;
		mTree = DepthsTree.SHADOWS;
		mInfo.mTrigger = AbilityTrigger.LEFT_CLICK;
		mInfo.mCooldown = COOLDOWN;
		mInfo.mLinkedSpell = ClassAbility.SHADOW_DIVE;
	}

	@Override
	public void cast(Action trigger) {

		Location loc = mPlayer.getEyeLocation();
		ItemStack itemTincture = new ItemStack(Material.BLACK_CONCRETE);
		World world = mPlayer.getWorld();
		world.playSound(loc, Sound.ENTITY_SNOWBALL_THROW, 1, 0.15f);
		Item tincture = world.dropItem(loc, itemTincture);
		tincture.setPickupDelay(Integer.MAX_VALUE);

		Vector vel = mPlayer.getEyeLocation().getDirection().normalize();
		vel.multiply(VELOCITY);

		tincture.setVelocity(vel);
		tincture.setGlowing(true);

		putOnCooldown();
		AbilityUtils.applyStealth(mPlugin, mPlayer, STEALTH_DURATION[mRarity - 1]);

		new BukkitRunnable() {

			int mExpire = 0;
			World mWorld = mPlayer.getWorld();

			@Override
			public void run() {
				if (tincture.isOnGround()) {
					mWorld.spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, tincture.getLocation(), 30, 3, 0, 3);
					mWorld.spawnParticle(Particle.EXPLOSION_NORMAL, tincture.getLocation(), 30, 2, 0, 2);
					world.playSound(tincture.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1, 0.15f);
					List<LivingEntity> mobs = EntityUtils.getNearbyMobs(tincture.getLocation(), RADIUS);
					for (LivingEntity mob : mobs) {
						EntityUtils.applyWeaken(mPlugin, WEAKEN_DURATION, WEAKEN_AMPLIFIER[mRarity - 1], mob);
					}

					tincture.remove();
					this.cancel();
				}
				mExpire++;
				if (mExpire >= 10 * 20) {
					tincture.remove();
					this.cancel();
				}
			}
		}.runTaskTimer(mPlugin, 0, 1);
	}

	@Override
	public boolean livingEntityDamagedByPlayerEvent(EntityDamageByEntityEvent event) {
	    if (event.getCause().equals(DamageCause.ENTITY_ATTACK)) {
	        cast(Action.LEFT_CLICK_AIR);
	    }

	    return true;
	}

	@Override
	public boolean runCheck() {
		return (mPlayer.isSneaking() && !isOnCooldown() && DepthsUtils.isWeaponItem(mPlayer.getInventory().getItemInMainHand()));
	}

	@Override
	public String getDescription(int rarity) {
		return "Left click while sneaking and holding a weapon to throw a shadow bomb, which explodes on landing, applying " + DepthsUtils.getRarityColor(rarity) + DepthsUtils.roundPercent(WEAKEN_AMPLIFIER[rarity - 1]) + "%" + ChatColor.WHITE + " weaken for " + WEAKEN_DURATION / 20 + " seconds. You enter stealth for " + STEALTH_DURATION[rarity - 1] / 20.0 + " seconds upon casting. Cooldown: " + COOLDOWN / 20 + "s.";
	}

	@Override
	public DepthsTree getDepthsTree() {
		return DepthsTree.SHADOWS;
	}

	@Override
	public DepthsTrigger getTrigger() {
		return DepthsTrigger.SHIFT_LEFT_CLICK;
	}
}

