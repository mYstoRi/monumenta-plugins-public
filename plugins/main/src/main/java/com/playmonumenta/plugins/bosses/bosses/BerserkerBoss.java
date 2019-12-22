package com.playmonumenta.plugins.bosses.bosses;

import java.util.Arrays;

import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.Plugin;

import com.playmonumenta.plugins.bosses.SpellManager;
import com.playmonumenta.plugins.bosses.spells.SpellBerserk;

public class BerserkerBoss extends BossAbilityGroup {
	public static final String identityTag = "boss_berserker";
	public static final int detectionRange = 35;

	public static BossAbilityGroup deserialize(Plugin plugin, LivingEntity boss) {
		return new BerserkerBoss(plugin, boss);
	}

	public BerserkerBoss(Plugin plugin, LivingEntity boss) {

		SpellManager activeSpells = new SpellManager(Arrays.asList(
			new SpellBerserk(boss)
		));

		super.constructBoss(plugin, identityTag, boss, activeSpells, null, detectionRange, null);
	}
}
