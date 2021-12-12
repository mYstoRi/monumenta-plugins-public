package com.playmonumenta.plugins.plots;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.checkerframework.checker.nullness.qual.Nullable;

import com.goncalomb.bukkit.mylib.utils.CustomInventory;
import com.playmonumenta.plugins.plots.PlotManager.PlotInfo;
import com.playmonumenta.plugins.plots.PlotManager.PlotInfo.OtherAccessRecord;
import com.playmonumenta.worlds.paper.ScoreboardUtils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.md_5.bungee.api.ChatColor;

public class PlotAccessCustomInventory extends CustomInventory {
	private static final Material FILLER = Material.GRAY_STAINED_GLASS_PANE;
	private static ArrayList<Integer> LOCATIONS = new ArrayList<>(Arrays.asList(19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43));
	private int mNumPages = 1;
	private ArrayList<PlotEntry> mAccessList = new ArrayList<PlotEntry>();
	private int mCurrentPage = 1;
	private PlotInfo mInfo;

	private static class PlotEntry {
		boolean mSelf;
		@Nullable OtherAccessRecord mEntry;

		public PlotEntry(@Nullable OtherAccessRecord record) {
			mSelf = false;
			mEntry = record;
		}

		public PlotEntry() {
			mSelf = true;
			mEntry = null;
		}
	}


	public PlotAccessCustomInventory(Player player, PlotInfo info) {
		//super creates the GUI with arguments of player to open for, slots in GUI,
		//and the name of the container (top line in the chest)
		super(player, 54, "Available Plots");
		mInfo = info;
		if (info.mOwnedPlotId > 0) {
			mAccessList.add(new PlotEntry());
		}
		for (Entry<UUID, OtherAccessRecord> entry : info.mOwnerAccessToOtherPlots.entrySet()) {
			mAccessList.add(new PlotEntry(entry.getValue()));
		}
		mNumPages = (int) Math.ceil((double) mAccessList.size() / (double) LOCATIONS.size());

		setLayout(player, 1);
	}

	@Override
	protected void inventoryClick(InventoryClickEvent event) {
		//Always cancel at the start if you want to avoid item removal
		event.setCancelled(true);
		//Check to make sure they clicked the GUI, didn't shift click, and
		//did not click the filler item
		if (event.getClickedInventory() != _inventory ||
				event.getCurrentItem() == null ||
				event.getCurrentItem().getType() == FILLER ||
				event.isShiftClick()) {
			return;
		}
		//back and next buttons
		if (event.getSlot() == 0) {
			setLayout((Player) event.getWhoClicked(), mCurrentPage - 1);
			return;
		} else if (event.getSlot() == 8) {
			setLayout((Player) event.getWhoClicked(), mCurrentPage + 1);
			return;
		}

		//clicked a head
		if (LOCATIONS.contains(event.getSlot())) {
			int whichHead = getItemIndex(event.getSlot());
			Player player = (Player) event.getWhoClicked();
			PlotEntry targetLoc = mAccessList.get(whichHead);
			if (targetLoc.mSelf) {
				ScoreboardUtils.setScoreboardValue(player, "CurrentPlot", mInfo.mOwnedPlotId);
			} else {
				ScoreboardUtils.setScoreboardValue(player, "CurrentPlot", targetLoc.mEntry.mPlotId);
			}
			PlotManager.sendPlayerToPlot(player);
			player.closeInventory();
		}
	}

	private void setLayout(Player player, int page) {
		mCurrentPage = page;
		_inventory.clear();
		int pageOffset = (mCurrentPage - 1) * LOCATIONS.size();
		for (int i = 0; i < LOCATIONS.size(); i++) {
			if (i + pageOffset < mAccessList.size()) {
				_inventory.setItem(LOCATIONS.get(i), makeHead(mAccessList.get(i + pageOffset), player));
			}
		}
		makeControlButtons();
		fillJunk();
	}

	private void makeControlButtons() {
		_inventory.setItem(4, createBasicItem(Material.SCUTE, "Plot Selection", NamedTextColor.AQUA, false, "Click the head of the plot you would like to visit.",
				ChatColor.GOLD));
		if (mCurrentPage != 1) {
			_inventory.setItem(0, createBasicItem(Material.ARROW, "Back", NamedTextColor.GRAY, false, "Click to go to page " + (mCurrentPage - 1),
					ChatColor.GRAY));
		}
		if (mCurrentPage < mNumPages) {
			_inventory.setItem(8, createBasicItem(Material.ARROW, "Next", NamedTextColor.GRAY, false, "Click to go to page " + (mCurrentPage + 1),
					ChatColor.GRAY));
		}
	}

	private int getItemIndex(int slot) {
		int itemsPerPage = LOCATIONS.size();
		int currentPageLoc = LOCATIONS.indexOf(slot);
		return (itemsPerPage * (mCurrentPage - 1)) + currentPageLoc;
	}

	private ItemStack makeHead(PlotEntry record, Player player) {
		if (record.mSelf) {
			return makeYourHead(player);
		} else {
			return record.mEntry.mHead;
		}
	}

	private ItemStack makeYourHead(Player player) {
		ItemStack head = new ItemStack(Material.PLAYER_HEAD, 1);
		SkullMeta meta = (SkullMeta) head.getItemMeta();
		meta.setOwningPlayer(player);
		meta.displayName(Component.text("Your Plot", NamedTextColor.GOLD)
				.decoration(TextDecoration.ITALIC, false));
		meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		head.setItemMeta(meta);
		return head;
	}

	public void splitLoreLine(ItemMeta meta, String lore, int maxLength, ChatColor defaultColor, boolean clean) {
		String[] splitLine = lore.split(" ");
		String currentString = defaultColor + "";
		List<String> finalLines = clean ? new ArrayList<String>() : meta.getLore();
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

	public ItemStack createBasicItem(Material mat, String name, NamedTextColor nameColor, boolean nameBold, String desc, ChatColor loreColor) {
		ItemStack item = new ItemStack(mat, 1);
		ItemMeta meta = item.getItemMeta();
		meta.displayName(Component.text(name, nameColor)
				.decoration(TextDecoration.ITALIC, false)
				.decoration(TextDecoration.BOLD, nameBold));
		splitLoreLine(meta, desc, 30, loreColor, true);
		meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
		item.setItemMeta(meta);
		return item;
	}


	private void fillJunk() {
		for (int i = 0; i < 54; i++) {
			if (_inventory.getItem(i) == null) {
				_inventory.setItem(i, new ItemStack(FILLER, 1));
			}
		}
	}
}