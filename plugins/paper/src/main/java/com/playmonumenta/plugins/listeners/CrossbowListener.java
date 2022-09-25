package com.playmonumenta.plugins.listeners;

import com.playmonumenta.plugins.Plugin;
import com.playmonumenta.plugins.utils.InventoryUtils;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionType;


public class CrossbowListener implements Listener {

	private Plugin mPlugin;

	public CrossbowListener(Plugin plugin) {
		mPlugin = plugin;
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void projectileLaunchEvent(ProjectileLaunchEvent event) {
		//Has to be an arrow
		if (!(event.getEntity() instanceof Arrow arrow)
			    || !(arrow.getShooter() instanceof LivingEntity shooter)) {
			return;
		}

		ItemStack item = shooter.getEquipment().getItemInMainHand();

		//For non player entities that shoot a crossbow
		if (item.getType().equals(Material.CROSSBOW) && !(arrow.getShooter() instanceof Player)) {
			if (item.getEnchantmentLevel(Enchantment.ARROW_FIRE) > 0) {
				arrow.setFireTicks(100);
			}
			if (item.getEnchantmentLevel(Enchantment.ARROW_KNOCKBACK) > 0) {
				arrow.setKnockbackStrength(item.getEnchantmentLevel(Enchantment.ARROW_KNOCKBACK));
			}
		}

		//Has to be a player-shot normal arrow
		if (arrow.getShooter() instanceof Player player) {
			ItemStack itemInMainHand = player.getEquipment().getItemInMainHand();
			ItemStack itemInOffHand = player.getEquipment().getItemInOffHand();

			// Check if the player has a crossbow in main or offhand
			if (itemInMainHand.getType().equals(Material.CROSSBOW)
				    || itemInOffHand.getType().equals(Material.CROSSBOW)) {

				//Infinity gives arrow to player if the arrow shot had no potion nor custom effects
				if (itemInMainHand.getEnchantmentLevel(Enchantment.ARROW_INFINITE) > 0
					    && !arrow.hasCustomEffects()
					    && arrow.getBasePotionData().getType() == PotionType.UNCRAFTABLE // plain arrow
					    && arrow.getPickupStatus() == AbstractArrow.PickupStatus.ALLOWED) {
					arrow.setPickupStatus(AbstractArrow.PickupStatus.CREATIVE_ONLY);
					if (player.getGameMode() != GameMode.CREATIVE) {
						InventoryUtils.giveItem(player, new ItemStack(Material.ARROW), true);
					}
				}

				//Flame level metadata given, based off of mainhand if both have enchants
				if (itemInMainHand.getEnchantmentLevel(Enchantment.ARROW_FIRE) > 0
						|| itemInOffHand.getEnchantmentLevel(Enchantment.ARROW_FIRE) > 0) {
					arrow.setFireTicks(100);
				}

				//Sets Punch manually to the arrow
				if (itemInMainHand.getEnchantmentLevel(Enchantment.ARROW_KNOCKBACK) > 0
						|| itemInOffHand.getEnchantmentLevel(Enchantment.ARROW_KNOCKBACK) > 0) {
					int level = itemInMainHand.getEnchantmentLevel(Enchantment.ARROW_KNOCKBACK) > 0 ?
							itemInMainHand.getEnchantmentLevel(Enchantment.ARROW_KNOCKBACK) : itemInOffHand.getEnchantmentLevel(Enchantment.ARROW_KNOCKBACK);
					arrow.setKnockbackStrength(level);
				}
				//Has to manually call projectile launch event because of arrow changes
				mPlugin.mItemStatManager.onLaunchProjectile(mPlugin, player, event, event.getEntity());
			}
		}
	}
}
