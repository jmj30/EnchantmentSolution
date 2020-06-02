package org.ctp.enchantmentsolution.enums;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;

public enum ItemBreakType {
	NETHERITE_AXE(), NETHERITE_SHOVEL(), NETHERITE_PICKAXE(), NETHERITE_HOE(), DIAMOND_AXE(), DIAMOND_SHOVEL(), DIAMOND_PICKAXE(), DIAMOND_HOE(),
	IRON_AXE(), IRON_SHOVEL(), IRON_PICKAXE(), IRON_HOE(), GOLDEN_AXE(), GOLDEN_SHOVEL(), GOLDEN_PICKAXE(), GOLDEN_HOE(), STONE_AXE(), STONE_SHOVEL(),
	STONE_PICKAXE(), STONE_HOE(), WOODEN_AXE(), WOODEN_SHOVEL(), WOODEN_PICKAXE(), WOODEN_HOE();

	private List<Material> breakTypes, silkBreakTypes, fortuneBreakTypes;
	private final List<String> DIAMOND_PICKAXE_BLOCKS = Arrays.asList("OBSIDIAN", "CRYING_OBSIDIAN", "RESPAWN_ANCHOR", "ANCIENT_DEBRIS", "NETHERITE_BLOCK");

	ItemBreakType() {
		breakTypes = getItemBreakMaterials("");
		silkBreakTypes = getItemBreakMaterials("silk");
		fortuneBreakTypes = getItemBreakMaterials("fortune");
	}

	public List<String> getDiamondPickaxeBlocks() {
		return DIAMOND_PICKAXE_BLOCKS;
	}

	public List<Material> getBreakTypes() {
		return breakTypes;
	}

	public List<Material> getSilkBreakTypes() {
		return silkBreakTypes;
	}

	public List<Material> getFortuneBreakTypes() {
		return fortuneBreakTypes;
	}

	public static ItemBreakType getType(Material type) {
		for(ItemBreakType breakType: ItemBreakType.values())
			if (type.name().equals(breakType.name())) return breakType;
		return null;
	}

	public static List<Material> allBreakTypes() {
		List<Material> itemTypes = new ArrayList<Material>();
		for(ItemBreakType type: ItemBreakType.values())
			itemTypes.addAll(type.getBreakTypes());
		return itemTypes;
	}

	private List<Material> getItemBreakMaterials(String type) {
		List<Material> materials = new ArrayList<Material>();
		List<String> strings = type.equals("silk") ? getSilkTouchStrings() : type.equals("fortune") ? getFortuneStrings() : getItemBreakStrings();
		for(String s: strings)
			try {
				materials.add(Material.valueOf(s));
			} catch (Exception ex) {

			}

		return materials;
	}

	private List<String> getItemBreakStrings() {
		List<String> itemTypes = new ArrayList<String>();
		switch (name()) {
			case "NETHERITE_PICKAXE":
			case "DIAMOND_PICKAXE":
				itemTypes.addAll(DIAMOND_PICKAXE_BLOCKS);
			case "IRON_PICKAXE":
				itemTypes.addAll(Arrays.asList("DIAMOND_BLOCK", "EMERALD_BLOCK", "GOLD_BLOCK", "DIAMOND_ORE", "EMERALD_ORE", "GOLD_ORE", "REDSTONE_ORE"));
			case "STONE_PICKAXE":
				itemTypes.addAll(Arrays.asList("IRON_BLOCK", "LAPIS_BLOCK", "IRON_ORE", "LAPIS_ORE"));
			case "GOLDEN_PICKAXE":
			case "WOODEN_PICKAXE":
				itemTypes.addAll(Arrays.asList("BLACKSTONE", "POLISHED_BLACKSTONE", "CHISELED_POLISHED_BLACKSTONE", "GILDED_BLACKSTONE", "POLISHED_BLACKSTONE_BRICKS", "CHISELED_POLISHED_BLACKSTONE_BRICKS", "QUARTZ_BRICKS", "CRACKED_NETHER_BRICKS", "CHISELED_NETHER_BRICKS", "BASALT", "POLISHED_BASALT", "NETHER_GOLD_ORE", "CRIMSON_NYLIUM", "WARPED_NYLIUM", "ICE", "PACKED_ICE", "FROSTED_ICE", "BLUE_ICE", "REDSTONE_BLOCK", "IRON_BARS", "ACTIVATOR_RAIL", "DETECTOR_RAIL", "POWERED_RAIL", "RAIL", "ANDESITE", "COAL_BLOCK", "QUARTZ_BLOCK", "BRICKS", "COAL_ORE", "COBBLESTONE", "COBBLESTONE_WALL", "BLACK_CONCRETE", "BLUE_CONCRETE", "BROWN_CONCRETE", "CYAN_CONCRETE", "GRAY_CONCRETE", "GREEN_CONCRETE", "LIGHT_BLUE_CONCRETE", "LIGHT_GRAY_CONCRETE", "LIME_CONCRETE", "MAGENTA_CONCRETE", "ORANGE_CONCRETE", "PINK_CONCRETE", "PURPLE_CONCRETE", "RED_CONCRETE", "WHITE_CONCRETE", "YELLOW_CONCRETE", "DARK_PRISMARINE", "DIORITE", "END_STONE", "END_STONE_BRICKS", "TERRACOTTA", "BLACK_GLAZED_TERRACOTTA", "BLUE_GLAZED_TERRACOTTA", "BROWN_GLAZED_TERRACOTTA", "CYAN_GLAZED_TERRACOTTA", "GRAY_GLAZED_TERRACOTTA", "GREEN_GLAZED_TERRACOTTA", "LIGHT_BLUE_GLAZED_TERRACOTTA", "LIGHT_GRAY_GLAZED_TERRACOTTA", "LIME_GLAZED_TERRACOTTA", "MAGENTA_GLAZED_TERRACOTTA", "ORANGE_GLAZED_TERRACOTTA", "PINK_GLAZED_TERRACOTTA", "PURPLE_GLAZED_TERRACOTTA", "RED_GLAZED_TERRACOTTA", "WHITE_GLAZED_TERRACOTTA", "YELLOW_GLAZED_TERRACOTTA", "GRANITE", "MOSSY_COBBLESTONE", "MOSSY_COBBLESTONE_WALL", "NETHER_BRICK", "NETHER_BRICK_FENCE", "NETHER_QUARTZ_ORE", "NETHERRACK", "POLISHED_ANDESITE", "POLISHED_DIORITE", "POLISHED_GRANITE", "PRISMARINE", "PRISMARINE_BRICKS", "RED_SANDSTONE", "SANDSTONE", "BLACK_TERRACOTTA", "BLUE_TERRACOTTA", "BROWN_TERRACOTTA", "CYAN_TERRACOTTA", "GRAY_TERRACOTTA", "GREEN_TERRACOTTA", "LIGHT_BLUE_TERRACOTTA", "LIGHT_GRAY_TERRACOTTA", "LIME_TERRACOTTA", "MAGENTA_TERRACOTTA", "ORANGE_TERRACOTTA", "PINK_TERRACOTTA", "PURPLE_TERRACOTTA", "RED_TERRACOTTA", "WHITE_TERRACOTTA", "YELLOW_TERRACOTTA", "STONE", "STONE_BRICKS", "MOSSY_STONE_BRICKS", "CRACKED_STONE_BRICKS", "CHISELED_STONE_BRICKS", "INFESTED_CHISELED_STONE_BRICKS", "INFESTED_COBBLESTONE", "INFESTED_CRACKED_STONE_BRICKS", "INFESTED_MOSSY_STONE_BRICKS", "INFESTED_STONE", "INFESTED_STONE_BRICKS", "BRAIN_CORAL_BLOCK", "BUBBLE_CORAL_BLOCK", "FIRE_CORAL_BLOCK", "HORN_CORAL_BLOCK", "TUBE_CORAL_BLOCK", "SMOOTH_QUARTZ", "MAGMA_BLOCK", "SMOOTH_STONE", "SMOOTH_RED_SANDSTONE", "SMOOTH_SANDSTONE"));
				return itemTypes;
			case "NETHERITE_AXE":
			case "DIAMOND_AXE":
			case "GOLDEN_AXE":
			case "IRON_AXE":
			case "STONE_AXE":
			case "WOODEN_AXE":
				itemTypes.addAll(Arrays.asList("CAMPFIRE", "CRIMSON_HYPHAE", "STRIPPED_CRIMSON_HYPHAE", "WARPED_HYPHAE", "STRIPPED_WARPED_HYPHAE", "SHROOMLIGHT", "CRIMSON_STEM", "STRIPPED_CRIMSON_STEM", "WARPED_STEM", "STRIPPED_WARPED_STEM", "CRIMSON_PLANKS", "WARPED_PLANKS", "COCOA", "JACK_O_LANTERN", "PUMPKIN", "MELON", "BOOKSHELF", "ACACIA_FENCE", "ACACIA_FENCE_GATE", "BIRCH_FENCE", "BIRCH_FENCE_GATE", "DARK_OAK_FENCE", "DARK_OAK_FENCE_GATE", "JUNGLE_FENCE", "JUNGLE_FENCE_GATE", "OAK_FENCE", "OAK_FENCE_GATE", "SPRUCE_FENCE", "SPRUCE_FENCE_GATE", "ACACIA_LOG", "ACACIA_PLANKS", "BIRCH_LOG", "BIRCH_PLANKS", "DARK_OAK_LOG", "DARK_OAK_PLANKS", "JUNGLE_LOG", "JUNGLE_PLANKS", "OAK_LOG", "OAK_PLANKS", "SPRUCE_LOG", "SPRUCE_PLANKS", "ACACIA_WOOD", "BIRCH_WOOD", "DARK_OAK_WOOD", "JUNGLE_WOOD", "OAK_WOOD", "SPRUCE_WOOD", "STRIPPED_ACACIA_LOG", "STRIPPED_BIRCH_LOG", "STRIPPED_DARK_OAK_LOG", "STRIPPED_JUNGLE_LOG", "STRIPPED_OAK_LOG", "STRIPPED_SPRUCE_LOG", "STRIPPED_ACACIA_WOOD", "STRIPPED_BIRCH_WOOD", "STRIPPED_DARK_OAK_WOOD", "STRIPPED_JUNGLE_WOOD", "STRIPPED_OAK_WOOD", "STRIPPED_SPRUCE_WOOD", "BROWN_MUSHROOM_BLOCK", "RED_MUSHROOM_BLOCK", "LADDER"));
				return itemTypes;
			case "NETHERITE_SHOVEL":
			case "DIAMOND_SHOVEL":
			case "IRON_SHOVEL":
			case "GOLDEN_SHOVEL":
			case "STONE_SHOVEL":
			case "WOODEN_SHOVEL":
				itemTypes.addAll(Arrays.asList("SOUL_SOIL", "SNOW", "SNOW_BLOCK", "CLAY", "COARSE_DIRT", "DIRT", "FARMLAND", "GRASS_BLOCK", "GRASS_PATH", "GRAVEL", "MYCELIUM", "PODZOL", "RED_SAND", "SAND", "SOUL_SAND", "BLACK_CONCRETE_POWDER", "BLUE_CONCRETE_POWDER", "BROWN_CONCRETE_POWDER", "CYAN_CONCRETE_POWDER", "GRAY_CONCRETE_POWDER", "GREEN_CONCRETE_POWDER", "LIGHT_BLUE_CONCRETE_POWDER", "LIGHT_GRAY_CONCRETE_POWDER", "LIME_CONCRETE_POWDER", "MAGENTA_CONCRETE_POWDER", "ORANGE_CONCRETE_POWDER", "PINK_CONCRETE_POWDER", "PURPLE_CONCRETE_POWDER", "RED_CONCRETE_POWDER", "WHITE_CONCRETE_POWDER", "YELLOW_CONCRETE_POWDER"));
				return itemTypes;
			case "NETHERITE_HOE":
			case "DIAMOND_HOE":
			case "IRON_HOE":
			case "GOLDEN_HOE":
			case "STONE_HOE":
			case "WOODEN_HOE":
				itemTypes.addAll(Arrays.asList("OAK_LEAVES", "BIRCH_LEAVES", "SPRUCE_LEAVES", "JUNGLE_LEAVES", "DARK_OAK_LEAVES", "ACACIA_LEAVES", "NETHER_WART_BLOCK", "WARPED_WART_BLOCK", "SHROOMLIGHT", "HAY_BALE", "TARGET", "DRIED_KELP_BLOCK", "SPONGE", "WET_SPONGE"));
				return itemTypes;
			default:
				break;
		}
		return null;
	}

	private List<String> getSilkTouchStrings() {
		List<String> itemTypes = new ArrayList<String>();
		itemTypes.addAll(Arrays.asList("CAMPFIRE", "BOOKSHELF", "CLAY", "GLASS", "GLASS_PANE", "GLOWSTONE", "GRASS_BLOCK", "MYCELIUM", "BROWN_MUSHROOM_BLOCK", "RED_MUSHROOM_BLOCK", "MUSHROOM_STEM", "MELON", "PODZOL", "SEA_LANTERN", "ICE", "PACKED_ICE", "BLUE_ICE", "ACACIA_LEAVES", "BIRCH_LEAVES", "DARK_OAK_LEAVES", "JUNGLE_LEAVES", "OAK_LEAVES", "SPRUCE_LEAVES", "BLACK_STAINED_GLASS", "BLUE_STAINED_GLASS", "BROWN_STAINED_GLASS", "CYAN_STAINED_GLASS", "GRAY_STAINED_GLASS", "GREEN_STAINED_GLASS", "LIGHT_BLUE_STAINED_GLASS", "LIGHT_GRAY_STAINED_GLASS", "LIME_STAINED_GLASS", "MAGENTA_STAINED_GLASS", "ORANGE_STAINED_GLASS", "PINK_STAINED_GLASS", "PURPLE_STAINED_GLASS", "RED_STAINED_GLASS", "WHITE_STAINED_GLASS", "YELLOW_STAINED_GLASS", "BLACK_STAINED_GLASS_PANE", "BLUE_STAINED_GLASS_PANE", "BROWN_STAINED_GLASS_PANE", "CYAN_STAINED_GLASS_PANE", "GRAY_STAINED_GLASS_PANE", "GREEN_STAINED_GLASS_PANE", "LIGHT_BLUE_STAINED_GLASS_PANE", "LIGHT_GRAY_STAINED_GLASS_PANE", "LIME_STAINED_GLASS_PANE", "MAGENTA_STAINED_GLASS_PANE", "ORANGE_STAINED_GLASS_PANE", "PINK_STAINED_GLASS_PANE", "PURPLE_STAINED_GLASS_PANE", "RED_STAINED_GLASS_PANE", "WHITE_STAINED_GLASS_PANE", "YELLOW_STAINED_GLASS_PANE", "BRAIN_CORAL", "BRAIN_CORAL_FAN", "BUBBLE_CORAL", "BUBBLE_CORAL_FAN", "FIRE_CORAL", "FIRE_CORAL_FAN", "HORN_CORAL", "HORN_CORAL_FAN", "TUBE_CORAL", "TUBE_CORAL_FAN", "BRAIN_CORAL_WALL_FAN", "BUBBLE_CORAL_WALL_FAN", "FIRE_CORAL_WALL_FAN", "HORN_CORAL_WALL_FAN", "TUBE_CORAL_WALL_FAN", "BEE_NEST", "BEEHIVE"));
		switch (name()) {
			case "NETHERITE_PICKAXE":
			case "DIAMOND_PICKAXE":
			case "IRON_PICKAXE":
				itemTypes.addAll(Arrays.asList("DIAMOND_ORE", "EMERALD_ORE", "REDSTONE_ORE"));
			case "STONE_PICKAXE":
				itemTypes.addAll(Arrays.asList("LAPIS_ORE"));
			case "GOLDEN_PICKAXE":
			case "WOODEN_PICKAXE":
				itemTypes.addAll(Arrays.asList("CRIMSON_NYLIUM", "WARPED_NYLIUM", "GILDED_BLACKSTONE", "ENDER_CHEST", "NETHER_GOLD_ORE", "ICE", "PACKED_ICE", "BLUE_ICE", "COAL_ORE", "NETHER_QUARTZ_ORE", "STONE", "BRAIN_CORAL_BLOCK", "BUBBLE_CORAL_BLOCK", "FIRE_CORAL_BLOCK", "HORN_CORAL_BLOCK", "TUBE_CORAL_BLOCK"));
				return itemTypes;
			case "NETHERITE_AXE":
			case "DIAMOND_AXE":
			case "GOLDEN_AXE":
			case "IRON_AXE":
			case "STONE_AXE":
			case "WOODEN_AXE":
				return itemTypes;
			case "NETHERITE_SHOVEL":
			case "DIAMOND_SHOVEL":
			case "IRON_SHOVEL":
			case "GOLDEN_SHOVEL":
			case "STONE_SHOVEL":
			case "WOODEN_SHOVEL":
				itemTypes.addAll(Arrays.asList("SNOW", "SNOW_BLOCK"));
				return itemTypes;
			case "NETHERITE_HOE":
			case "DIAMOND_HOE":
			case "IRON_HOE":
			case "GOLDEN_HOE":
			case "STONE_HOE":
			case "WOODEN_HOE":
				return itemTypes;
			default:
				break;
		}
		return itemTypes;
	}

	public List<String> getFortuneStrings() {
		List<String> itemTypes = new ArrayList<String>();
		itemTypes.addAll(Arrays.asList("WEEPING_VINES", "TWISTING_VINES", "WHEAT", "BEETROOTS", "CARROTS", "POTATOES", "NETHER_WART", "SEA_LANTERN", "MELON", "GLOWSTONE", "GRASS", "GRAVEL", "ACACIA_LEAVES", "BIRCH_LEAVES", "DARK_OAK_LEAVES", "JUNGLE_LEAVES", "OAK_LEAVES", "SPRUCE_LEAVES"));

		switch (name()) {
			case "NETHERITE_PICKAXE":
			case "DIAMOND_PICKAXE":
			case "IRON_PICKAXE":
				itemTypes.addAll(Arrays.asList("DIAMOND_ORE", "EMERALD_ORE", "REDSTONE_ORE"));
			case "STONE_PICKAXE":
				itemTypes.addAll(Arrays.asList("LAPIS_ORE"));
			case "GOLDEN_PICKAXE":
			case "WOODEN_PICKAXE":
				itemTypes.addAll(Arrays.asList("GILDED_BLACKSTONE", "NETHER_GOLD_ORE", "COAL_ORE", "NETHER_QUARTZ_ORE"));
				return itemTypes;
			case "NETHERITE_AXE":
			case "DIAMOND_AXE":
			case "GOLDEN_AXE":
			case "IRON_AXE":
			case "STONE_AXE":
			case "WOODEN_AXE":
			case "NETHERITE_SHOVEL":
			case "DIAMOND_SHOVEL":
			case "IRON_SHOVEL":
			case "GOLDEN_SHOVEL":
			case "STONE_SHOVEL":
			case "WOODEN_SHOVEL":
			case "NETHERITE_HOE":
			case "DIAMOND_HOE":
			case "IRON_HOE":
			case "GOLDEN_HOE":
			case "STONE_HOE":
			case "WOODEN_HOE":
				return itemTypes;
			default:
				break;
		}
		return itemTypes;
	}
}
