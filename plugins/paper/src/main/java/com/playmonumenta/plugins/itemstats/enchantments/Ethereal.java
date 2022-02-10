package com.playmonumenta.plugins.itemstats.enchantments;

import com.playmonumenta.plugins.Plugin;
import com.playmonumenta.plugins.effects.Effect;
import com.playmonumenta.plugins.effects.OnHitTimerEffect;
import com.playmonumenta.plugins.events.DamageEvent;
import com.playmonumenta.plugins.itemstats.Enchantment;
import com.playmonumenta.plugins.utils.ItemStatUtils.EnchantmentType;
import javax.annotation.Nullable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.NavigableSet;

public class Ethereal implements Enchantment {

	private static final double AGIL_BONUS_PER_LEVEL = 0.2;
	private static final int PAST_HIT_DURATION_TIME = 30;
	private static final String ETHEREAL_EFFECT_NAME = "EtherealEffect";

	@Override
	public @NotNull String getName() {
		return "Ethereal";
	}

	@Override
	public EnchantmentType getEnchantmentType() {
		return EnchantmentType.ETHEREAL;
	}

	@Override
	public void onHurt(Plugin plugin, Player player, double value, DamageEvent event, @Nullable Entity damager, @Nullable LivingEntity source) {
		if (plugin.mEffectManager.getEffects(player, ETHEREAL_EFFECT_NAME) != null) {
			plugin.mEffectManager.clearEffects(player, ETHEREAL_EFFECT_NAME);
		}
		plugin.mEffectManager.addEffect(player, ETHEREAL_EFFECT_NAME, new OnHitTimerEffect(PAST_HIT_DURATION_TIME));
	}

	public static double applyEthereal(DamageEvent event, Plugin plugin, Player player) {
		NavigableSet<Effect> eth = plugin.mEffectManager.getEffects(player, ETHEREAL_EFFECT_NAME);
		if (eth != null) {
			return plugin.mItemStatManager.getEnchantmentLevel(player, EnchantmentType.ETHEREAL) * AGIL_BONUS_PER_LEVEL;
		} else {
			return 0;
		}
	}

}