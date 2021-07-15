package com.playmonumenta.plugins.depths.abilities.frostborn;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import com.playmonumenta.plugins.Plugin;
import com.playmonumenta.plugins.classes.ClassAbility;
import com.playmonumenta.plugins.classes.magic.MagicType;
import com.playmonumenta.plugins.depths.DepthsTree;
import com.playmonumenta.plugins.depths.DepthsUtils;
import com.playmonumenta.plugins.depths.abilities.DepthsAbility;
import com.playmonumenta.plugins.depths.abilities.DepthsTrigger;
import com.playmonumenta.plugins.depths.abilities.steelsage.DepthsSharpshooter;
import com.playmonumenta.plugins.depths.abilities.steelsage.ProjectileMastery;
import com.playmonumenta.plugins.utils.EntityUtils;

import net.md_5.bungee.api.ChatColor;

public class FrostSpear extends DepthsAbility {

	public static final String ABILITY_NAME = "Piercing Cold";
	public static final int[] DAMAGE = {14, 17, 20, 23, 26};
	public static final int ICE_TICKS = 10 * 20;
	public static final int COOLDOWN = 8 * 20;
	private static final Particle.DustOptions ENCHANTED_ARROW_COLOR = new Particle.DustOptions(Color.fromRGB(80, 32, 140), 2.0f);
	private static final Particle.DustOptions ENCHANTED_ARROW_FRINGE_COLOR = new Particle.DustOptions(Color.fromRGB(168, 255, 252), 2.0f);
	private static final int MAX_DIST = 50;

	public static String tree;

	public FrostSpear(Plugin plugin, Player player) {
		super(plugin, player, ABILITY_NAME);
		mDisplayItem = Material.PRISMARINE_SHARD;
		mTree = DepthsTree.FROSTBORN;
		mInfo.mCooldown = COOLDOWN;
		mInfo.mLinkedSpell = ClassAbility.FROST_SPEAR;
	}

	public void execute() {

		BoundingBox box = BoundingBox.of(mPlayer.getEyeLocation(), 1.5, 1.5, 1.5);

		World world = mPlayer.getWorld();
		Player player = mPlayer;
		Location loc = player.getEyeLocation();
		Vector dir = loc.getDirection().normalize();

		world.playSound(loc, Sound.BLOCK_GLASS_BREAK, 1, 1.4f);
		world.playSound(loc, Sound.ENTITY_PUFFER_FISH_DEATH, 0.7f, 0.8f);
		world.playSound(loc, Sound.ITEM_TRIDENT_RIPTIDE_3, 1f, 1.5f);
		world.playSound(loc, Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1f, 0.75f);
		world.playSound(loc, Sound.ENTITY_ILLUSIONER_MIRROR_MOVE, 1f, 1f);
		world.playSound(loc, Sound.ENTITY_IRON_GOLEM_HURT, 1f, 0.75f);

		world.spawnParticle(Particle.CRIT, mPlayer.getEyeLocation().add(mPlayer.getLocation().getDirection()), 15, 0, 0, 0, 0.6f);
		world.spawnParticle(Particle.CRIT_MAGIC, mPlayer.getEyeLocation().add(mPlayer.getLocation().getDirection()), 15, 0, 0, 0, 0.6f);
		world.spawnParticle(Particle.SMOKE_NORMAL, loc.clone().add(dir), 20, 0.2, 0.2, 0.2, 0.15);
		world.spawnParticle(Particle.CRIT_MAGIC, loc.clone().add(dir), 10, 0.2, 0.2, 0.2, 0.15);

		List<LivingEntity> mobs = EntityUtils.getNearbyMobs(mPlayer.getEyeLocation(), MAX_DIST, mPlayer);

		Location pLoc = mPlayer.getEyeLocation();
		pLoc.setPitch(pLoc.getPitch() + 90);
		Vector pVec = new Vector(pLoc.getDirection().getX(), pLoc.getDirection().getY(), pLoc.getDirection().getZ());
		pVec = pVec.normalize();

		int hits = 0;
		double damage = DAMAGE[mRarity - 1] * ProjectileMastery.getDamageMultiplier(player) * DepthsSharpshooter.getDamageMultiplier(player);
		for (int i = 0; i < MAX_DIST; i++) {
			box.shift(dir);
			Location bLoc = box.getCenter().toLocation(world);
			world.spawnParticle(Particle.SMOKE_NORMAL, bLoc, 5, 0.1, 0.1, 0.1, 0.2);
			world.spawnParticle(Particle.FALLING_DUST, bLoc, 5, 0.1, 0.1, 0.1, 0, Bukkit.createBlockData("light_blue_glazed_terracotta"));
			world.spawnParticle(Particle.SPELL_INSTANT, bLoc, 6, 0.2, 0.2, 0.2, 0.15);
			world.spawnParticle(Particle.SMOKE_NORMAL, bLoc, 3, 0.1, 0.1, 0.1, 0);
			world.spawnParticle(Particle.REDSTONE, bLoc, 3, 0.1, 0.1, 0.1, 0, ENCHANTED_ARROW_COLOR);
			world.spawnParticle(Particle.CRIT, bLoc, 5, 0.15, 0.15, 0.15, 0.4);
			world.spawnParticle(Particle.REDSTONE, bLoc.clone().add(pVec), 1, 0, 0, 0, 0, ENCHANTED_ARROW_FRINGE_COLOR);
			world.spawnParticle(Particle.END_ROD, bLoc.clone().add(pVec), 1, 0, 0, 0, 0.02);
			world.spawnParticle(Particle.REDSTONE, bLoc.clone().add(dir.clone().multiply(0.5)).add(pVec.clone().rotateAroundAxis(dir, Math.PI / 12)), 1, 0, 0, 0, 0, ENCHANTED_ARROW_FRINGE_COLOR);
			Iterator<LivingEntity> iterator = mobs.iterator();
			while (iterator.hasNext()) {
				LivingEntity mob = iterator.next();
				if (mob.getBoundingBox().overlaps(box)) {
					world.spawnParticle(Particle.CRIT_MAGIC, mob.getLocation().add(0, 1, 0), 15, 0.1, 0.2, 0.1, 0.15);
					world.spawnParticle(Particle.SPELL_INSTANT, mob.getLocation().add(0, 1, 0), 20, 0.1, 0.2, 0.1, 0.15);
					world.spawnParticle(Particle.FIREWORKS_SPARK, mob.getLocation().add(0, 1, 0), 10, 0.1, 0.2, 0.1, 0.1);
					EntityUtils.damageEntity(mPlugin, mob, damage, mPlayer, MagicType.ARCANE, true, mInfo.mLinkedSpell);
					/* Prevent mob from being hit twice in one shot */
					iterator.remove();
					hits++;
				}
			}
			Block block = bLoc.getBlock();

			// Replace blocks at bottom with ice
			Location checkingLoc = bLoc.clone();
			int count = 0;
			while (!world.getBlockAt(checkingLoc).isSolid() && count < 15) {
				checkingLoc = checkingLoc.clone().add(0, -1, 0);
				count++;
			}
			if (count < 15) {
				Block centerBlock = world.getBlockAt(checkingLoc);
				ArrayList<Block> blocksToIce = new ArrayList<>();
				blocksToIce.add(centerBlock);
				blocksToIce.add(centerBlock.getRelative(BlockFace.NORTH));
				blocksToIce.add(centerBlock.getRelative(BlockFace.EAST));
				blocksToIce.add(centerBlock.getRelative(BlockFace.SOUTH));
				blocksToIce.add(centerBlock.getRelative(BlockFace.WEST));

				for (Block b : blocksToIce) {
					//Check above block first and see if it is exposed to air
					if (b.getRelative(BlockFace.UP).isSolid() && !b.getRelative(BlockFace.UP).getRelative(BlockFace.UP).isSolid()) {
						DepthsUtils.spawnIceTerrain(b.getRelative(BlockFace.UP).getLocation(), ICE_TICKS);
					} else if (b.isSolid()) {
						DepthsUtils.spawnIceTerrain(b.getLocation(), ICE_TICKS);

					} else if (b.getRelative(BlockFace.DOWN).isSolid()) {
						DepthsUtils.spawnIceTerrain(b.getRelative(BlockFace.DOWN).getLocation(), ICE_TICKS);

					}
				}
			}

			if (block.getType().isSolid()) {
				world.spawnParticle(Particle.SMOKE_LARGE, bLoc, 80, 0.1, 0.1, 0.1, 0.2);
				world.spawnParticle(Particle.CLOUD, bLoc, 80, 0.1, 0.1, 0.1, 0.2);
				world.spawnParticle(Particle.FIREWORKS_SPARK, bLoc, 50, 0.1, 0.1, 0.1, 0.3);
				world.playSound(bLoc, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1, 0.85f);
				break;
			}

			pVec.rotateAroundAxis(dir, Math.PI / 6);
		}
		DepthsSharpshooter.addStacks(mPlugin, player, hits);
	}

	@Override
	public boolean playerShotArrowEvent(AbstractArrow arrow) {
		if (isOnCooldown()) {
			return true;
		}

		if (mPlayer.isSneaking()) {
			arrow.remove();
			mPlugin.mProjectileEffectTimers.removeEntity(arrow);
			putOnCooldown();
			execute();
		}

		return true;
	}

	@Override
	public String getDescription(int rarity) {
		return "Shooting an arrow while sneaking instead shoots an enchanted beam of frost that deals " + DepthsUtils.getRarityColor(rarity) + DAMAGE[rarity - 1] + ChatColor.WHITE + " damage and leaves a trail of ice below it that lasts for " + ICE_TICKS / 20 + " seconds. Cooldown: " + COOLDOWN / 20 + "s.";
	}

	@Override
	public DepthsTree getDepthsTree() {
		return DepthsTree.FROSTBORN;
	}

	@Override
	public DepthsTrigger getTrigger() {
		return DepthsTrigger.SHIFT_BOW;
	}

}

