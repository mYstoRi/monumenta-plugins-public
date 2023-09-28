package com.playmonumenta.plugins.itemstats.enums;

import com.playmonumenta.plugins.itemstats.ItemStat;
import com.playmonumenta.plugins.itemstats.enchantments.Festive;
import com.playmonumenta.plugins.itemstats.enchantments.Gilded;
import com.playmonumenta.plugins.itemstats.infusions.*;
import com.playmonumenta.plugins.utils.StringUtils;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.jetbrains.annotations.Nullable;

public enum InfusionType {
	// Infusions
	ACUMEN(new Acumen(), "", true, false, false, false, true, false),
	FOCUS(new Focus(), "", true, false, false, false, true, false),
	PERSPICACITY(new Perspicacity(), "", true, false, false, false, true, false),
	TENACITY(new Tenacity(), "", true, false, false, false, true, false),
	VIGOR(new Vigor(), "", true, false, false, false, true, false),
	VITALITY(new Vitality(), "", true, false, false, false, true, false),

	// Delve Infusions
	ANTIGRAV(new AntiGrav(), "", true, false, false, false, true, true),
	ARDOR(new Ardor(), "", true, false, false, false, true, true),
	AURA(new Aura(), "", true, false, false, false, true, true),
	CARAPACE(new Carapace(), "", true, false, false, false, true, true),
	CHOLER(new Choler(), "", true, false, false, false, true, true),
	DECAPITATION(new Decapitation(), "", true, false, false, false, true, true),
	EMPOWERED(new Empowered(), "", true, false, false, false, true, true),
	ENERGIZE(new Energize(), "", true, false, false, false, true, true),
	EPOCH(new Epoch(), "", true, false, false, false, true, true),
	EXECUTION(new Execution(), "", true, false, false, false, true, true),
	EXPEDITE(new Expedite(), "", true, false, false, false, true, true),
	FUELED(new Fueled(), "", true, false, false, false, true, true),
	GALVANIC(new Galvanic(), "", true, false, false, false, true, true),
	GRACE(new Grace(), "", true, false, false, false, true, true),
	MITOSIS(new Mitosis(), "", true, false, false, false, true, true),
	NATANT(new Natant(), "", true, false, false, false, true, true),
	NUTRIMENT(new Nutriment(), "", true, false, false, false, true, true),
	PENNATE(new Pennate(), "", true, false, false, false, true, true),
	QUENCH(new Quench(), "", true, false, false, false, true, true),
	REFLECTION(new Reflection(), "", true, false, false, false, true, true),
	REFRESH(new Refresh(), "", true, false, false, false, true, true),
	SOOTHING(new Soothing(), "", true, false, false, false, true, true),
	UNDERSTANDING(new Understanding(), "", true, false, false, false, true, false),
	UNYIELDING(new Unyielding(), "", true, false, false, false, true, true),
	USURPER(new Usurper(), "", true, false, false, false, true, true),
	VENGEFUL(new Vengeful(), "", true, false, false, false, true, true),
	// Other Added Tags
	LOCKED(new Locked(), "", false, false, false, false, false, false),
	BARKING(new Barking(), "", true, false, true, false, false, false),
	DEBARKING(new Debarking(), "", false, false, false, false, false, false),
	RUSTWORTHY(new Rustworthy(), "", true, true, false, false, false, false),
	UNRUSTWORTHY(new Unrustworthy(), "", false, false, false, false, false, false),
	WAX_ON(new WaxOn(), "", false, false, false, false, false, false),
	WAX_OFF(new WaxOff(), "", false, false, false, false, false, false),
	HOPE(new Hope(), "Hoped", false, false, true, false, false, false),
	COLOSSAL(new Colossal(), "Reinforced", false, false, false, false, false, false),
	PHYLACTERY(new Phylactery(), "Embalmed", false, false, false, false, false, false),
	SOULBOUND(new Soulbound(), "Soulbound", false, false, false, false, false, false),
	FESTIVE(new Festive(), "Decorated", false, false, true, false, false, false),
	GILDED(new Gilded(), "Gilded", false, false, true, false, false, false),
	SHATTERED(new Shattered(), "", true, false, false, false, false, false),
	// Stat tracking stuff
	STAT_TRACK(new StatTrack(), "Tracked", false, false, false, false, false, false),
	STAT_TRACK_KILLS(new StatTrackKills(), "", true, false, false, true, false, false),
	STAT_TRACK_DAMAGE(new StatTrackDamage(), "", true, false, false, true, false, false),
	STAT_TRACK_MELEE(new StatTrackMelee(), "", true, false, false, true, false, false),
	STAT_TRACK_PROJECTILE(new StatTrackProjectile(), "", true, false, false, true, false, false),
	STAT_TRACK_MAGIC(new StatTrackMagic(), "", true, false, false, true, false, false),
	STAT_TRACK_BOSS(new StatTrackBoss(), "", true, false, false, true, false, false),
	STAT_TRACK_SPAWNER(new StatTrackSpawners(), "", true, false, false, true, false, false),
	STAT_TRACK_CONSUMED(new StatTrackConsumed(), "", true, false, false, true, false, false),
	STAT_TRACK_BLOCKS(new StatTrackBlocks(), "", true, false, false, true, false, false),
	STAT_TRACK_RIPTIDE(new StatTrackRiptide(), "", true, false, false, true, false, false),
	STAT_TRACK_BLOCKS_BROKEN(new StatTrackBlocksBroken(), "", true, false, false, true, false, false),
	STAT_TRACK_SHIELD_BLOCKED(new StatTrackShielded(), "", true, false, false, true, false, false),
	STAT_TRACK_DEATH(new StatTrackDeath(), "", true, false, false, true, false, false),
	STAT_TRACK_REPAIR(new StatTrackRepair(), "", true, false, false, true, false, false),
	STAT_TRACK_CONVERT(new StatTrackConvert(), "", true, false, false, true, false, false),
	STAT_TRACK_DAMAGE_TAKEN(new StatTrackDamageTaken(), "", true, false, false, true, false, false),
	STAT_TRACK_HEALING_DONE(new StatTrackHealingDone(), "", true, false, false, true, false, false),
	STAT_TRACK_FISH_CAUGHT(new StatTrackFishCaught(), "", true, false, false, true, false, false);
	public static final Map<String, InfusionType> REVERSE_MAPPINGS = Arrays.stream(InfusionType.values())
		.collect(Collectors.toUnmodifiableMap(type -> type.getName().replace(" ", ""), type -> type));

	public static final Set<InfusionType> STAT_TRACK_OPTIONS = Arrays.stream(InfusionType.values())
		.filter(type -> type.mIsStatTrackOption)
		.collect(Collectors.toUnmodifiableSet());

	public static final Set<InfusionType> SPAWNABLE_INFUSIONS = Arrays.stream(InfusionType.values())
		.filter(type -> type.mIsSpawnable)
		.collect(Collectors.toUnmodifiableSet());

	public static final String KEY = "Infusions";

	final ItemStat mItemStat;
	final String mName;
	final String mMessage;
	final boolean mIsSpawnable;
	final boolean mHasLevels;
	final boolean mIsCurse;
	final boolean mIsStatTrackOption;
	final boolean mIsDisabledByShatter;
	final boolean mIsDelveInfusion;

	InfusionType(ItemStat itemStat, String message, boolean hasLevels, boolean isCurse, boolean isSpawnable, boolean isStatTrackOption, boolean isDisabledByShatter, boolean isDelveInfusion) {
		mItemStat = itemStat;
		mName = itemStat.getName();
		mIsSpawnable = isSpawnable;
		mHasLevels = hasLevels;
		mIsCurse = isCurse;
		mMessage = message;
		mIsStatTrackOption = isStatTrackOption;
		mIsDisabledByShatter = isDisabledByShatter;
		mIsDelveInfusion = isDelveInfusion;
	}

	public ItemStat getItemStat() {
		return mItemStat;
	}

	public String getName() {
		return mName;
	}

	public String getMessage() {
		return mMessage;
	}

	public boolean isDisabledByShatter() {
		return mIsDisabledByShatter;
	}

	public boolean isStatTrackOption() {
		return mIsStatTrackOption;
	}

	public boolean isDelveInfusion() {
		return mIsDelveInfusion;
	}

	public Component getDisplay(int level, @Nullable String infuser) {
		TextColor color = mIsCurse ? NamedTextColor.RED : NamedTextColor.GRAY;
		if (!mHasLevels) {
			if (mMessage.isEmpty() || infuser == null) {
				return Component.text(mName, color).decoration(TextDecoration.ITALIC, false);
			} else if (this == SOULBOUND) {
				return Component.text(mName, color).decoration(TextDecoration.ITALIC, false).append(Component.text(" (" + mMessage + " to " + infuser + ")", NamedTextColor.DARK_GRAY));
			} else {
				return Component.text(mName, color).decoration(TextDecoration.ITALIC, false).append(Component.text(" (" + mMessage + " by " + infuser + ")", NamedTextColor.DARK_GRAY));
			}
		} else if (mIsStatTrackOption) {
			return Component.text(mName + ": " + (level - 1), NamedTextColor.RED).decoration(TextDecoration.ITALIC, false);
		} else {
			if (mMessage.isEmpty() || infuser == null) {
				return Component.text(mName + " " + StringUtils.toRoman(level), color).decoration(TextDecoration.ITALIC, false);
			} else {
				return Component.text(mName + " " + StringUtils.toRoman(level), color).decoration(TextDecoration.ITALIC, false).append(Component.text(" (" + mMessage + " by " + infuser + ")", NamedTextColor.DARK_GRAY));
			}
		}
	}

	public Component getDisplay(int level) {
		return getDisplay(level, null);
	}

	public boolean isHidden() {
		return this == SHATTERED;
	}

	public static @Nullable InfusionType getInfusionType(String name) {
		return REVERSE_MAPPINGS.get(name.replace(" ", ""));
	}
}