package com.playmonumenta.plugins.bosses.bosses.lich;

import com.playmonumenta.plugins.bosses.SpellManager;
import com.playmonumenta.plugins.bosses.bosses.BossAbilityGroup;
import com.playmonumenta.plugins.bosses.spells.CrowdControlImmunity;
import com.playmonumenta.plugins.bosses.spells.Spell;
import com.playmonumenta.plugins.bosses.spells.SpellBasePassiveAbility;
import com.playmonumenta.plugins.bosses.spells.lich.undeadplayers.SpellSmokescreen;
import java.util.List;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.Plugin;

public class LichRogueBoss extends BossAbilityGroup {
	public static final String identityTag = "boss_lichrogue";
	public static final int detectionRange = 20;

	public LichRogueBoss(Plugin plugin, LivingEntity boss) {
		super(plugin, identityTag, boss);

		SpellManager activeSpells = new SpellManager(List.of(
			new SpellSmokescreen(plugin, mBoss)
		));

		List<Spell> passiveSpells = List.of(
			new SpellBasePassiveAbility(20 * 4, new CrowdControlImmunity(mBoss))
		);

		super.constructBoss(activeSpells, passiveSpells, detectionRange, null);
	}
}
