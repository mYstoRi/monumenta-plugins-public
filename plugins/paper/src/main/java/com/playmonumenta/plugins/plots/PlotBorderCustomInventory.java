package com.playmonumenta.plugins.plots;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.goncalomb.bukkit.mylib.utils.CustomInventory;
import com.playmonumenta.plugins.utils.ItemUtils;
import com.playmonumenta.scriptedquests.utils.ScoreboardUtils;
import com.playmonumenta.structures.StructuresAPI;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.checkerframework.checker.nullness.qual.Nullable;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.md_5.bungee.api.ChatColor;

public class PlotBorderCustomInventory extends CustomInventory {
	private static final Material FILLER = Material.GRAY_STAINED_GLASS_PANE;
	private static final Map<UUID, Long> COOLDOWNS = new HashMap<>();

	/*
	 * Pages explanation: Page x0 of the x0-x9 set is the default landing for the gui.
	 * Pages 0-9: Plots
	 * Pages 10-19: Region 1
	 * Pages 20-29: Region 2
	 * Pages 50-on: Common pages
	 */

	public static class TeleportEntry {
		int mPage = 1;
		int mSlot;
		String mName;
		@Nullable String mScoreboard;
		String mLore;
		int mScoreRequired;
		Material mType;
		String mLeftClick;
		@Nullable UUID mBuilder;

		public TeleportEntry(int p, int s, String n, String l, Material t, @Nullable String sc, int sr, String left, @Nullable String builder) {
			mPage = p;
			mSlot = s;
			mName = n;
			mLore = l;
			mType = t;
			mScoreboard = sc;
			mScoreRequired = sr;
			mLeftClick = left;
			mBuilder = UUID.fromString(builder);
		}

		public TeleportEntry(int p, int s, String n, String l, Material t, @Nullable String sc, int sr, String left) {
			mPage = p;
			mSlot = s;
			mName = n;
			mLore = l;
			mType = t;
			mScoreboard = sc;
			mScoreRequired = sr;
			mLeftClick = left;
			mBuilder = null;
		}
	}
	/* Page Info
	 * Page 0: Common for 1-9
	 * Page 1: Region 1
	 * Page 2: Region 2
	 * Page 3: Plots
	 * Page 10: Common for 11-19
	 * Page 11: Region Instance Choice
	 */

	private static ArrayList<UUID> BUILDER_IDS = new ArrayList<>();

	private static ArrayList<TeleportEntry> BORDER_ITEMS = new ArrayList<>();

	static {
		BORDER_ITEMS.add(new TeleportEntry(0, 47, "Base Choices", "Click to view the plot borders with no requirements.", Material.GRASS_BLOCK, null, 0, "page 1"));
		BORDER_ITEMS.add(new TeleportEntry(0, 49, "Unlockable Choices", "Click to view plot borders locked behind completion of content.", Material.IRON_INGOT, null, 0, "page 2"));
		BORDER_ITEMS.add(new TeleportEntry(0, 51, "Patreon Choices", "Click to view options only available to Patrons.", Material.GOLD_INGOT, "Patreon", 5, "page 3"));


		BORDER_ITEMS.add(new TeleportEntry(1, 20, "Narsen Village", "A small town of Narsen citizens, bearing some resemblance to the old plots world.", Material.LIGHT_BLUE_CONCRETE, null, 0, "narsen_village"));
		BORDER_ITEMS.add(new TeleportEntry(1, 24, "King's Valley Jungle", "A plot nestled in the jungles of the King's Valley.", Material.GREEN_CONCRETE, null, 0, "kings_valley_jungle"));


		BORDER_ITEMS.add(new TeleportEntry(2, 20, "Celsian Isles: Chillwind", "Located in the frosty forests of Chillwind.", Material.SNOW_BLOCK, "Quest101", 13, "celsian_isles_chillwind"));
		BORDER_ITEMS.add(new TeleportEntry(2, 21, "Celsian Isles: Ishnir", "Located in the desert of Ishnir.", Material.SANDSTONE, "Quest101", 13, "celsian_isles_ishnir"));
		BORDER_ITEMS.add(new TeleportEntry(2, 22, "Kaul's Arena", "Located in the Kaul arena.", Material.JUNGLE_LEAVES, "KaulWins", 1, "kaul_arena"));
		BORDER_ITEMS.add(new TeleportEntry(2, 23, "Eldrask's Arena", "Located in the Eldrask arena.", Material.PACKED_ICE, "FGWins", 1, "eldrask_arena"));
		BORDER_ITEMS.add(new TeleportEntry(2, 24, "Hekawt's Arena", "Located in the Hekawt arena.", Material.RED_SANDSTONE, "LichWins", 1, "hekawt_arena"));


		BORDER_ITEMS.add(new TeleportEntry(3, 19, "Celsian Isles Ocean", "A plot drowned beneath the waters of the Celsian Isles.", Material.BUBBLE_CORAL, "Quest101", 13, "celsian_isles_ocean"));
		BORDER_ITEMS.add(new TeleportEntry(3, 21, "Salazar's Folly", "Located right in the middle of the Viridian City.", Material.LIME_WOOL, "Lime", 1, "dungeons/lime"));
		BORDER_ITEMS.add(new TeleportEntry(3, 23, "Grasp of Avarice", "A plot found at the end of the orange branch of the dungeon.", Material.PURPLE_WOOL, "Purple", 1, "dungeons/purple"));
		BORDER_ITEMS.add(new TeleportEntry(3, 25, "Echoes of Oblivion", "Warp to another time with this plot located in Era 3!", Material.CYAN_CONCRETE_POWDER, "Teal", 1, "dungeons/teal"));
	}

	private int mCurrentPage = 0;
	private Boolean mOverridePermissions = false;

	public PlotBorderCustomInventory(Player player, boolean fullAccess) {
		super(player, 54, "Border Choices");
		mCurrentPage = 1;
		mOverridePermissions = fullAccess;
		setLayout(player);
	}

	@Override
	protected void inventoryClick(InventoryClickEvent event) {
		event.setCancelled(true);
		Player player = null;
		if (event.getWhoClicked() instanceof Player) {
			player = (Player) event.getWhoClicked();
		} else {
			return;
		}
		ItemStack clickedItem = event.getCurrentItem();
		if (event.getClickedInventory() != _inventory) {
			return;
		}

		int commonPage = (int) Math.floor(mCurrentPage / 10) * 10;
		if (clickedItem != null && clickedItem.getType() != FILLER && !event.isShiftClick()) {
			int chosenSlot = event.getSlot();
			for (TeleportEntry item : BORDER_ITEMS) {
				if (item.mSlot == chosenSlot && item.mPage == mCurrentPage) {
					if (event.isLeftClick()) {
						completeCommand(player, item.mName, item.mLeftClick);
					}
				}
				if (item.mSlot == chosenSlot && item.mPage == commonPage) {
					if (event.isLeftClick()) {
						completeCommand(player, item.mName, item.mLeftClick);
					}
				}
			}
		}
	}

	public Boolean isInternalCommand(String command) {
		if (command.equals("exit") || command.startsWith("page") || command.startsWith("instancebot") || command.equals("back")) {
			return true;
		}
		return false;
	}

	public void runInternalCommand(Player player, String cmd) {
		if (cmd.startsWith("page")) {
			mCurrentPage = Integer.parseInt(cmd.split(" ")[1]);
			setLayout(player);
			return;
		} else if (cmd.startsWith("exit")) {
			player.closeInventory();
			return;
		} else if (cmd.equals("back")) {
			mCurrentPage = 1;
			setLayout(player);
		}
	}

	public void completeCommand(Player player, String name, String cmd) {
		if (cmd == "") {
			return;
		}
		if (isInternalCommand(cmd)) {
			runInternalCommand(player, cmd);
			return;
		} else {
			long timeLeft = COOLDOWNS.getOrDefault(player.getUniqueId(), Long.valueOf(0)) - Instant.now().getEpochSecond();

			if (timeLeft > 0 && !player.isOp()) {
				player.sendMessage("Too fast! You can only change the border once every 120s (" + timeLeft + "s remaining)");
			} else if (!player.getWorld().getName().contains("plot")) {
				player.sendMessage("Can only load plot borders if the world's name contains 'plot', got '" + player.getWorld().getName() + "'");
			} else {
				player.sendMessage("Started loading plot border: " + name);
				COOLDOWNS.put(player.getUniqueId(), Instant.now().getEpochSecond() + 120);
				Location loc = player.getLocation();
				loc.setX(-1392);
				loc.setY(0);
				loc.setZ(-1392);
				StructuresAPI.loadAndPasteStructure("plots/borders/" + cmd, loc, false).whenComplete((unused, ex) -> {
					if (ex != null) {
						player.sendMessage("Plot border completed with error: " + ex.getMessage());
						ex.printStackTrace();
					} else {
						player.sendMessage("Plot border loading complete");
					}
				});
			}
			player.closeInventory();
			return;
		}
	}

	public ItemStack createCustomItem(TeleportEntry location) {
		ItemStack newItem = new ItemStack(location.mType, 1);
		ItemMeta meta = newItem.getItemMeta();
		meta.displayName(Component.text(location.mName, NamedTextColor.GOLD)
				.decoration(TextDecoration.ITALIC, false));
		if (location.mLore != "") {
			splitLoreLine(meta, location.mLore, 30, ChatColor.DARK_PURPLE);
		}
		meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		newItem.setItemMeta(meta);
		ItemUtils.setPlainName(newItem, location.mName);
		return newItem;
	}

	public void splitLoreLine(ItemMeta meta, String lore, int maxLength, ChatColor defaultColor) {
		String[] splitLine = lore.split(" ");
		String currentString = defaultColor + "";
		List<String> finalLines = new ArrayList<String>();
		int currentLength = 0;
		for (String word : splitLine) {
			if (currentLength + word.length() > maxLength) {
				finalLines.add(currentString);
				currentString = defaultColor + "";
				currentLength = 0;
			}
			currentString += word + " ";
			currentLength += word.length() + 1;
		}
		if (currentString != defaultColor + "") {
			finalLines.add(currentString);
		}
		meta.setLore(finalLines);
	}

	public void setLayout(Player player) {
		_inventory.clear();
		int commonPage = (int) Math.floor(mCurrentPage / 10) * 10;
		for (TeleportEntry item : BORDER_ITEMS) {
			if (item.mPage == commonPage) {
				if (item.mScoreboard == null || mOverridePermissions ||
						ScoreboardUtils.getScoreboardValue(player, item.mScoreboard) >= item.mScoreRequired ||
						BUILDER_IDS.contains(player.getUniqueId())) {
					_inventory.setItem(item.mSlot, createCustomItem(item));
				}
			} //intentionally not else, so overrides can happen
			if (item.mPage == mCurrentPage) {
				if (item.mBuilder != null) {
					player.sendMessage(player.getUniqueId().toString());
					player.sendMessage(item.mBuilder.toString());
				}
				if (item.mScoreboard == null || mOverridePermissions ||
						ScoreboardUtils.getScoreboardValue(player, item.mScoreboard) >= item.mScoreRequired ||
						player.getUniqueId().equals(item.mBuilder)) {
					_inventory.setItem(item.mSlot, createCustomItem(item));
				}
			}
		}

		for (int i = 0; i < 54; i++) {
			if (_inventory.getItem(i) == null) {
				_inventory.setItem(i, new ItemStack(FILLER, 1));
			}
		}
	}
}