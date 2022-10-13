package com.playmonumenta.plugins.utils;

import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.format.NamedTextColor;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;


public class GUIUtils {

	public static void splitLoreLine(ItemMeta meta, String lore, int maxLength, ChatColor defaultColor, boolean clean) {
		if (lore.isEmpty()) {
			return;
		}
		String[] splitLine = lore.split(" |(?<=\n)");
		StringBuilder currentLine = new StringBuilder(defaultColor + "");
		List<String> finalLines = (clean || meta.getLore() == null) ? new ArrayList<>() : meta.getLore();

		for (String word : splitLine) {
			boolean newline = currentLine.length() > 0 && currentLine.charAt(currentLine.length() - 1) == '\n';
			if (newline || currentLine.length() + word.length() > maxLength) {
				if (newline) {
					currentLine.setLength(currentLine.length() - 1);
				}
				finalLines.add(currentLine.toString());
				currentLine.setLength(0);
				currentLine.append(defaultColor + "");
			}
			currentLine.append(word).append(" ");
		}
		if (!currentLine.toString().equals(defaultColor + "")) {
			finalLines.add(currentLine.toString());
		}
		meta.setLore(finalLines);
	}

	public static void fillWithFiller(Inventory inventory, Material fillerMaterial) {
		ItemStack filler = new ItemStack(fillerMaterial, 1);
		ItemMeta meta = filler.getItemMeta();
		meta.setDisplayName(ChatColor.RESET + "");
		filler.setItemMeta(meta);
		for (int i = 0; i < inventory.getSize(); i++) {
			if (inventory.getItem(i) == null) {
				inventory.setItem(i, filler.clone());
			}
		}
	}

	public static void fillWithFiller(Inventory inventory, Material fillerMaterial, Boolean clearInv) {
		ItemStack filler = new ItemStack(fillerMaterial, 1);
		ItemMeta meta = filler.getItemMeta();
		meta.setDisplayName(ChatColor.RESET + "");
		filler.setItemMeta(meta);
		for (int i = 0; i < inventory.getSize(); i++) {
			if (inventory.getItem(i) == null || clearInv) {
				inventory.setItem(i, filler.clone());
			}
		}
	}

	public static ChatColor namedTextColorToChatColor(NamedTextColor color) {
		return ChatColor.of(color.toString());
	}

}
