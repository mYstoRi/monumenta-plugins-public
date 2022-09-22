package com.playmonumenta.plugins.bosses.bosses;

import com.playmonumenta.plugins.bosses.SpellManager;
import com.playmonumenta.plugins.events.DamageEvent;
import com.playmonumenta.plugins.events.DamageEvent.DamageType;
import com.playmonumenta.plugins.utils.DamageUtils;
import com.playmonumenta.plugins.utils.EntityUtils;
import com.playmonumenta.plugins.utils.PlayerUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class WhispersBoss extends BossAbilityGroup {
	public static final String identityTag = "boss_whispers";
	public static final int detectionRange = 50;

	public static class Parameters extends BossParameters {
		public double PERCENT_DAMAGE = 0;
	}

	final Parameters mParam;
	private List<Player> mStolenPlayers = new ArrayList<>();
	@Nullable
	private List<BukkitRunnable> mCleanse = new ArrayList<>();



	public static BossAbilityGroup deserialize(Plugin plugin, LivingEntity boss) throws Exception {
		return new WhispersBoss(plugin, boss);
	}

	public WhispersBoss(Plugin plugin, LivingEntity boss) {
		super(plugin, identityTag, boss);
		mParam = Parameters.getParameters(boss, identityTag, new WhispersBoss.Parameters());
		super.constructBoss(SpellManager.EMPTY, Collections.emptyList(), detectionRange, null);
	}

	@Override
	public void onDamage(DamageEvent event, LivingEntity damagee) {
		if (damagee instanceof Player player && !EntityUtils.hasAttributesContaining(player, Attribute.GENERIC_MAX_HEALTH, "Whispers" + mBoss.getUniqueId())
			&& !event.getType().equals(DamageType.TRUE) && !event.isBlocked()) {
			double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
			double trueDamage = mParam.PERCENT_DAMAGE / 100.0 * maxHealth;
			PotionEffect resistance = player.getPotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
			double resistanceLevel = (resistance == null ? 0 : resistance.getAmplifier() + 1);
			double resistanceMod = (resistanceLevel >= 5 ? 1.0 : 1.0 / (1.0 - 0.2 * resistanceLevel));

			new BukkitRunnable() {
				@Override
				public void run() {
					if (!player.isDead() && player.getAbsorptionAmount() == 0 && !event.isCancelled()) {
						mStolenPlayers.add(player);
						player.playSound(player.getLocation(), Sound.BLOCK_BUBBLE_COLUMN_WHIRLPOOL_INSIDE, 5, 1f);
						DamageUtils.damage(null, damagee, new DamageEvent.Metadata(DamageType.TRUE, null, null, "Whispers"), trueDamage * resistanceMod, true, false, false);
						EntityUtils.addAttribute(player, Attribute.GENERIC_MAX_HEALTH, new AttributeModifier("Whispers" + mBoss.getUniqueId(), -mParam.PERCENT_DAMAGE / 100.0, AttributeModifier.Operation.MULTIPLY_SCALAR_1));
						EntityUtils.addAttribute(mBoss, Attribute.GENERIC_MAX_HEALTH, new AttributeModifier("Whispers", trueDamage, AttributeModifier.Operation.ADD_NUMBER));
						mBoss.setHealth(mBoss.getHealth() + trueDamage);
						BukkitRunnable cleanse = new BukkitRunnable() {
							@Override
							public void run() {
								mStolenPlayers.remove(player);
								mCleanse.remove(this);
								EntityUtils.removeAttribute(player, Attribute.GENERIC_MAX_HEALTH, "Whispers" + mBoss.getUniqueId());
							}
						};
						mCleanse.add(cleanse);
						cleanse.runTaskLater(mPlugin, 30 * 20);
					}
				}
			}.runTaskLater(mPlugin, 1);
		}
	}

	@Override
	public void death(EntityDeathEvent event) {
		for (int i = 0; i < mStolenPlayers.size(); i++) {
			Player stolenPlayer = mStolenPlayers.get(i);
			if (EntityUtils.hasAttributesContaining(stolenPlayer, Attribute.GENERIC_MAX_HEALTH, "Whispers")) {
				mCleanse.get(i).cancel();
				double amtHeal = (mParam.PERCENT_DAMAGE / 100.0) / (1 - mParam.PERCENT_DAMAGE / 100.0) * stolenPlayer.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
				EntityUtils.removeAttribute(stolenPlayer, Attribute.GENERIC_MAX_HEALTH, "Whispers" + mBoss.getUniqueId());
				PlayerUtils.healPlayer(com.playmonumenta.plugins.Plugin.getInstance(), stolenPlayer, amtHeal);
			}
		}
	}
}
