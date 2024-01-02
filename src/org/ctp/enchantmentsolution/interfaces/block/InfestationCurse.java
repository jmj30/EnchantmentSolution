package org.ctp.enchantmentsolution.interfaces.block;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.ctp.crashapi.nms.ExpNMS;
import org.ctp.crashapi.utils.ItemUtils;
import org.ctp.enchantmentsolution.enchantments.RegisterEnchantments;
import org.ctp.enchantmentsolution.enums.ItemBreakType;
import org.ctp.enchantmentsolution.events.blocks.BlockDropItemAddEvent;
import org.ctp.enchantmentsolution.events.blocks.InfestationEvent;
import org.ctp.enchantmentsolution.interfaces.EnchantmentItemLocation;
import org.ctp.enchantmentsolution.interfaces.EnchantmentMultipleType;
import org.ctp.enchantmentsolution.interfaces.conditions.BlockCondition;
import org.ctp.enchantmentsolution.interfaces.effects.block.ChangeBlockDropEffect;
import org.ctp.enchantmentsolution.mcmmo.McMMOHandler;
import org.ctp.enchantmentsolution.utils.files.ItemBreakFile.ItemBreakFileType;
import org.ctp.enchantmentsolution.utils.items.EnchantmentDrops;
import org.ctp.enchantmentsolution.utils.items.Interactions;
import org.ctp.enchantmentsolution.utils.items.ItemDrop;

public class InfestationCurse extends ChangeBlockDropEffect {

	public InfestationCurse() {
		super(RegisterEnchantments.CURSE_OF_INFESTATION, EnchantmentMultipleType.HIGHEST, EnchantmentItemLocation.ATTACKED, EventPriority.HIGH, new EnchantmentDrops("/resources/drops/infestation_curse.yml", false), new BlockCondition[0]);
	}

	@Override
	public ChangeBlockDropResult run(Player player, ItemStack[] items, BlockData brokenData, BlockDropItemEvent event) {
		if (event instanceof BlockDropItemAddEvent) return null;

		ChangeBlockDropResult result = super.run(player, items, brokenData, event);
		int level = result.getLevel(), fortune = getLevel(items, Enchantment.LOOT_BONUS_BLOCKS), looting = getLevel(items, Enchantment.LOOT_BONUS_MOBS);

		if (level == 0 || result.getInteractions() == null) return null;
		ItemStack item = items[0];
		ItemBreakType type = ItemBreakType.getType(item.getType());
		if (!(type != null && type.getBreakTypes().contains(brokenData.getMaterial()) || ItemBreakType.getBasicTypes(ItemBreakFileType.BREAK).contains(brokenData.getMaterial()))) return null;

		Interactions interactions = result.getInteractions();
		List<ItemDrop> drops = new ArrayList<ItemDrop>();
		if (interactions.isPoolChance()) {
			int numDrops = interactions.getNumDrops(level);
			if (numDrops > 0) {
				List<ItemDrop> usedDrops = new ArrayList<ItemDrop>();
				do {
					double totalChance = 0;
					for(ItemDrop drop: interactions.getDrops()) {
						boolean used = false;
						for(ItemDrop d: usedDrops)
							if (d.equals(drop)) used = true;
						if (!used && drop.getType().hasMaterial()) totalChance += drop.getChance(level, fortune, looting);
					}
					if (totalChance == 0) break;
					double random = Math.random() * totalChance;
					ItemDrop finalDrop = null;
					for(ItemDrop drop: interactions.getDrops()) {
						boolean used = false;
						for(ItemDrop d: usedDrops)
							if (d.equals(drop)) used = true;
						if (!used && drop.getType().hasMaterial()) {
							random -= drop.getChance(level, fortune, looting);
							if (random <= 0) {
								finalDrop = drop;
								break;
							}
						}
					}
					if (finalDrop != null) {
						drops.add(finalDrop);
						usedDrops.add(finalDrop);
						numDrops--;
					} else
						break;
				} while (numDrops > 0);
			} else { /* placeholder */ }
		} else
			for(ItemDrop drop: interactions.getDrops())
				if (drop.getType().hasMaterial()) {
					double chance = drop.getChance(level, fortune, looting);
					double random = Math.random();
					if (chance > random) drops.add(drop);
				}

		List<ItemStack> dropItems = new ArrayList<ItemStack>();
		for(ItemDrop d: drops) {
			ItemStack i = new ItemStack(d.getType().getMaterial(), d.getAmount(level, fortune, looting, 1));
			if (i.getType() != Material.AIR) dropItems.add(i);
		}

		InfestationEvent infested = new InfestationEvent(event.getBlock(), brokenData, player, dropItems, result.getOldDrops(), interactions.isOverride(), interactions.getExp(level));
		Bukkit.getPluginManager().callEvent(infested);

		if (!infested.isCancelled()) {
			Location dropLoc = event.getBlock().getLocation();
			ExpNMS.dropExp(dropLoc, infested.getExp(), true, player);
			if (infested.willOverride()) event.setCancelled(true);
			List<Item> setItems = new ArrayList<Item>();
			for(ItemStack i: infested.getItems())
				setItems.add(ItemUtils.spawnItem(i, dropLoc));
			List<Item> finalDrops = Arrays.asList(setItems.toArray(new Item[0]));

			BlockDropItemAddEvent addItem = new BlockDropItemAddEvent(event.getBlock(), event.getBlockState(), player, finalDrops);
			Bukkit.getPluginManager().callEvent(addItem);

			if (addItem.isCancelled()) for(Item i: addItem.getItems())
				i.remove();

			McMMOHandler.handleBlockDrops(event, item, RegisterEnchantments.CURSE_OF_INFESTATION);
			return result;
		}

		return null;
	}

}
