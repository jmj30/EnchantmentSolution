package org.ctp.enchantmentsolution.utils.player;

import java.util.*;
import java.util.Map.Entry;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.ctp.crashapi.events.EquipEvent;
import org.ctp.crashapi.events.EquipEvent.EquipMethod;
import org.ctp.crashapi.item.*;
import org.ctp.crashapi.nms.DamageNMS;
import org.ctp.crashapi.utils.DamageUtils;
import org.ctp.crashapi.utils.ItemUtils;
import org.ctp.crashapi.utils.ServerUtils;
import org.ctp.enchantmentsolution.EnchantmentSolution;
import org.ctp.enchantmentsolution.advancements.ESAdvancement;
import org.ctp.enchantmentsolution.enchantments.*;
import org.ctp.enchantmentsolution.enchantments.helper.Seed;
import org.ctp.enchantmentsolution.enums.EntityItemType;
import org.ctp.enchantmentsolution.enums.ItemBreakType;
import org.ctp.enchantmentsolution.listeners.enchantments.AsyncGaiaController;
import org.ctp.enchantmentsolution.listeners.enchantments.AsyncHWDController;
import org.ctp.enchantmentsolution.listeners.enchantments.HWDModel;
import org.ctp.enchantmentsolution.rpg.RPGPlayer;
import org.ctp.enchantmentsolution.rpg.RPGUtils;
import org.ctp.enchantmentsolution.threads.ESPlayerThread;
import org.ctp.enchantmentsolution.utils.AdvancementUtils;
import org.ctp.enchantmentsolution.utils.BlockUtils;
import org.ctp.enchantmentsolution.utils.PermissionUtils;
import org.ctp.enchantmentsolution.utils.abilityhelpers.GaiaTrees;
import org.ctp.enchantmentsolution.utils.abilityhelpers.OverkillDeath;
import org.ctp.enchantmentsolution.utils.config.ConfigString;
import org.ctp.enchantmentsolution.utils.items.AbilityUtils;
import org.ctp.enchantmentsolution.utils.items.EnchantmentUtils;
import org.ctp.enchantmentsolution.utils.player.attributes.FlySpeedAttribute;

public class ESPlayer {

	private static Map<Long, Integer> GLOBAL_BLOCKS = new HashMap<Long, Integer>();
	private static double CONTAGION_CHANCE = 0.0005;
	private static long DELAY_TICKS = Long.MIN_VALUE;
	private final OfflinePlayer player;
	private Player onlinePlayer;
	private RPGPlayer rpg;
	private Map<EnchantmentWrapper, Long> cooldowns;
	private List<EnchantmentTimedDisable> timedDisable;
	private List<EnchantmentDisable> disable;
	private List<ItemStack> soulItems, telepathyItems;
	private Map<Long, Integer> blocksBroken;
	private float currentExhaustion, pastExhaustion;
	private ItemStack elytra;
	private boolean canFly, didTick, plyometricsAdvancement, willSeeEnchantments, canSeeEnchantments;
	private int flightDamage, flightDamageLimit, icarusDelay, underwaterTicks, modelKey, nextEchoShards, currentEchoShards;
	private List<OverkillDeath> overkillDeaths;
	private List<AttributeLevel> attributes;
	private List<PotionEffect> effects;
	private ESPlayerAttributeInstance flyAttribute = new FlySpeedAttribute();
	private Runnable telepathyTask;
	private List<AsyncHWDController> hwdControllers;
	private Map<GaiaTrees, List<AsyncGaiaController>> gaiaControllers;
	private List<HWDModel> models;
	private List<Seed> enchantmentSeeds = new ArrayList<Seed>();
	private long levelSeed;
	private CheckTimer equipItem;

	public ESPlayer(OfflinePlayer player) {
		this.player = player;
		onlinePlayer = player.getPlayer();
		rpg = RPGUtils.getPlayer(player);
		cooldowns = new HashMap<EnchantmentWrapper, Long>();
		timedDisable = new ArrayList<EnchantmentTimedDisable>();
		disable = new ArrayList<EnchantmentDisable>();
		blocksBroken = new HashMap<Long, Integer>();
		overkillDeaths = new ArrayList<OverkillDeath>();
		attributes = new ArrayList<AttributeLevel>();
		effects = new ArrayList<PotionEffect>();
		telepathyItems = new ArrayList<ItemStack>();
		removeSoulItems();
		flyAttribute.addModifier(new AttributeModifier(UUID.fromString("ffffffff-ffff-ffff-ffff-000000000000"), "frequentFlyerFlight", -0.08, Operation.ADD_NUMBER));
		if (EnchantmentSolution.getPlugin().isEnabled()) ESPlayerThread.getThread(this);
		hwdControllers = new ArrayList<>();
		gaiaControllers = new HashMap<>();
		models = new ArrayList<HWDModel>();
		equipItem = new CheckTimer(2);
		updateEnchantmentSeeds();
	}

	public OfflinePlayer getPlayer() {
		return player;
	}

	public boolean isOnline() {
		return onlinePlayer != null && onlinePlayer.isOnline();
	}

	public Player getOnlinePlayer() {
		return onlinePlayer;
	}

	public void logout() {
		resetFlightSpeed();
		removeAllHWDControllers();
		removeAllGaiaControllers();
	}

	public void reloadPlayer() {
		for(Player p: Bukkit.getOnlinePlayers())
			if (p.getUniqueId().equals(player.getUniqueId())) onlinePlayer = p;
	}

	public boolean isInInventory(ItemStack item) {
		for(int i = 0; i < 41; i++) {
			ItemStack content = getOnlinePlayer().getInventory().getItem(i);
			if (content == null) continue;
			if (item.equals(content)) return true;
		}
		return false;
	}

	public ItemStack getMainHand() {
		if (!isOnline()) return new ItemStack(Material.AIR);
		return getOnlinePlayer().getInventory().getItemInMainHand();
	}

	public ItemStack getOffhand() {
		if (!isOnline()) return new ItemStack(Material.AIR);
		return getOnlinePlayer().getInventory().getItemInOffHand();
	}

	public ItemStack[] getArmor() {
		ItemStack[] armor = new ItemStack[4];
		if (!isOnline()) return armor;
		armor[0] = getOnlinePlayer().getInventory().getHelmet();
		armor[1] = getOnlinePlayer().getInventory().getChestplate();
		armor[2] = getOnlinePlayer().getInventory().getLeggings();
		armor[3] = getOnlinePlayer().getInventory().getBoots();

		return armor;
	}

	public ItemSlot[] getArmorAndType() {
		ItemSlot[] armor = new ItemSlot[4];
		if (!isOnline()) return armor;
		armor[0] = new ItemSlot(getOnlinePlayer().getInventory().getHelmet(), ItemSlotType.HELMET);
		armor[1] = new ItemSlot(getOnlinePlayer().getInventory().getChestplate(), ItemSlotType.CHESTPLATE);
		armor[2] = new ItemSlot(getOnlinePlayer().getInventory().getLeggings(), ItemSlotType.LEGGINGS);
		armor[3] = new ItemSlot(getOnlinePlayer().getInventory().getBoots(), ItemSlotType.BOOTS);

		return armor;
	}

	public ItemStack[] getEquipped() {
		ItemStack[] equipped = new ItemStack[6];
		if (!isOnline()) return equipped;
		equipped[0] = getOnlinePlayer().getInventory().getHelmet();
		equipped[1] = getOnlinePlayer().getInventory().getChestplate();
		equipped[2] = getOnlinePlayer().getInventory().getLeggings();
		equipped[3] = getOnlinePlayer().getInventory().getBoots();
		equipped[4] = getOnlinePlayer().getInventory().getItemInMainHand();
		equipped[5] = getOnlinePlayer().getInventory().getItemInOffHand();

		return equipped;
	}

	public ItemSlot[] getEquippedAndType() {
		ItemSlot[] equipped = new ItemSlot[6];
		if (!isOnline()) return equipped;
		equipped[0] = new ItemSlot(getOnlinePlayer().getInventory().getHelmet(), ItemSlotType.HELMET);
		equipped[1] = new ItemSlot(getOnlinePlayer().getInventory().getChestplate(), ItemSlotType.CHESTPLATE);
		equipped[2] = new ItemSlot(getOnlinePlayer().getInventory().getLeggings(), ItemSlotType.LEGGINGS);
		equipped[3] = new ItemSlot(getOnlinePlayer().getInventory().getBoots(), ItemSlotType.BOOTS);
		equipped[4] = new ItemSlot(getOnlinePlayer().getInventory().getItemInMainHand(), ItemSlotType.MAIN_HAND);
		equipped[5] = new ItemSlot(getOnlinePlayer().getInventory().getItemInOffHand(), ItemSlotType.OFF_HAND);

		return equipped;
	}

	public ItemStack getItemFromType(ItemSlotType type) {
		for(ItemSlot e: getEquippedAndType()) {
			if (e == null) continue;
			if (e.getType() == type) return e.getItem();
		}
		return null;
	}

	public ItemStack[] getInventoryItems() {
		return isOnline() ? getOnlinePlayer().getInventory().getContents() : new ItemStack[41];
	}

	public ItemSlot[] getInventoryItemsAndType() {
		ItemSlot[] equipped = new ItemSlot[41];
		if (!isOnline()) return equipped;
		PlayerInventory inv = getOnlinePlayer().getInventory();

		for(int i = 0; i < 36; i++)
			if (i == inv.getHeldItemSlot()) equipped[i] = new ItemSlot(inv.getContents()[i], ItemSlotType.MAIN_HAND);
			else
				equipped[i] = new ItemSlot(inv.getContents()[i], ItemSlotType.getTypeFromSlot(i));
		equipped[36] = new ItemSlot(inv.getHelmet(), ItemSlotType.HELMET);
		equipped[37] = new ItemSlot(inv.getChestplate(), ItemSlotType.CHESTPLATE);
		equipped[38] = new ItemSlot(inv.getLeggings(), ItemSlotType.LEGGINGS);
		equipped[39] = new ItemSlot(inv.getBoots(), ItemSlotType.BOOTS);
		equipped[40] = new ItemSlot(inv.getItemInOffHand(), ItemSlotType.OFF_HAND);
		return equipped;
	}

	public ItemSlot[] getInventoryItemsAndType(ItemSlot item) {
		ItemSlot[] equipped = new ItemSlot[41];
		if (!isOnline()) return equipped;
		PlayerInventory inv = getOnlinePlayer().getInventory();

		for(int i = 0; i < 36; i++)
			if (i == inv.getHeldItemSlot()) {
				ItemStack it = inv.getContents()[i];
				if (item.getItem() != null && item.getType() == ItemSlotType.MAIN_HAND) it = item.getItem();
				equipped[i] = new ItemSlot(it, ItemSlotType.MAIN_HAND);
			} else {
				ItemStack it = inv.getContents()[i];
				if (item.getItem() != null && item.getType() == ItemSlotType.getTypeFromSlot(i)) it = item.getItem();
				equipped[i] = new ItemSlot(it, ItemSlotType.getTypeFromSlot(i));
			}
		equipped[36] = new ItemSlot(item.getItem() != null && item.getType() == ItemSlotType.HELMET ? item.getItem() : inv.getHelmet(), ItemSlotType.HELMET);
		equipped[37] = new ItemSlot(item.getItem() != null && item.getType() == ItemSlotType.CHESTPLATE ? item.getItem() : inv.getChestplate(), ItemSlotType.CHESTPLATE);
		equipped[38] = new ItemSlot(item.getItem() != null && item.getType() == ItemSlotType.LEGGINGS ? item.getItem() : inv.getLeggings(), ItemSlotType.LEGGINGS);
		equipped[39] = new ItemSlot(item.getItem() != null && item.getType() == ItemSlotType.BOOTS ? item.getItem() : inv.getBoots(), ItemSlotType.BOOTS);
		equipped[40] = new ItemSlot(item.getItem() != null && item.getType() == ItemSlotType.OFF_HAND ? item.getItem() : inv.getItemInOffHand(), ItemSlotType.OFF_HAND);
		return equipped;
	}

	public List<ItemStack> getUnstableItems() {
		List<ItemStack> items = new ArrayList<ItemStack>();
		for(ItemStack item: getEquipped())
			if (EnchantmentUtils.hasEnchantment(item, RegisterEnchantments.CURSE_OF_INSTABILITY)) items.add(item);
		return items;
	}

	public ItemSlot getProjectile(EntityType[] entityTypes) {
		ItemSlot[] slots = getInventoryItemsAndType();
		ItemSlot offhand = slots[40];
		if (offhand != null && offhand.getItem() != null) for(EntityItemType type: EntityItemType.values())
			if (offhand.getItem().getType() == type.getMaterial() && Arrays.asList(entityTypes).contains(type.getEntityType())) return offhand;

		for(ItemSlot slot: slots)
			for(EntityItemType type: EntityItemType.values())
				if (slot != null && slot.getItem() != null && slot.getItem().getType() == type.getMaterial() && Arrays.asList(entityTypes).contains(type.getEntityType())) return slot;

		return null;
	}

	public void setItem(ItemSlotType type, ItemStack i) {
		if (isOnline()) if (type.getEquipmentSlot() != null) getOnlinePlayer().getInventory().setItem(type.getEquipmentSlot(), i);
		else
			getOnlinePlayer().getInventory().setItem(type.getSlot(), i);
		else {}
	}

	public long getCooldown(EnchantmentWrapper enchant) {
		return cooldowns.containsKey(enchant) ? cooldowns.get(enchant) : 0;
	}

	public boolean setCooldown(EnchantmentWrapper enchant, int ticks) {
		cooldowns.put(enchant, ServerUtils.getCurrentTick() + ticks);
		return cooldowns.containsKey(enchant);
	}

	public RPGPlayer getRPG() {
		return rpg;
	}

	public List<ItemStack> getSoulItems() {
		return soulItems;
	}

	public void setSoulItems(List<ItemStack> items) {
		soulItems = items;
	}

	public void removeSoulItems() {
		soulItems = new ArrayList<ItemStack>();
	}

	public boolean canBreakBlock() {
		long tick = ServerUtils.getCurrentTick();
		if (tick <= DELAY_TICKS) return false;
		if (GLOBAL_BLOCKS.containsKey(tick) && GLOBAL_BLOCKS.get(tick) >= ConfigString.ASYNC_BLOCKS_GLOBAL.getInt()) return false;
		if (blocksBroken.containsKey(tick) && blocksBroken.get(tick) >= ConfigString.ASYNC_BLOCKS_PLAYER.getInt()) return false;
		return true;
	}

	public void breakBlock() {
		long tick = ServerUtils.getCurrentTick();
		int blocks = 1;
		if (blocksBroken.containsKey(tick)) blocks += blocksBroken.get(tick);
		blocksBroken.put(tick, blocks);
		int globalBlocks = 1;
		if (GLOBAL_BLOCKS.containsKey(tick)) globalBlocks += GLOBAL_BLOCKS.get(tick);
		GLOBAL_BLOCKS.put(tick, globalBlocks);
	}

	public double getContagionChance() {
		double playerChance = 0;
		if (isOnline()) for(ItemStack item: getOnlinePlayer().getInventory().getContents())
			if (EnchantmentUtils.hasEnchantment(item, RegisterEnchantments.CURSE_OF_CONTAGION)) playerChance += CONTAGION_CHANCE;
		return playerChance;
	}

	public List<ItemStack> getCurseableItems() {
		List<ItemStack> items = new ArrayList<ItemStack>();
		if (isOnline()) {
			for(ItemStack item: getOnlinePlayer().getInventory().getContents())
				if (item != null && ItemType.hasEnchantMaterial(new ItemData(item)) && canAddCurse(item) && !hasAllCurses(item)) items.add(item);
			for(ItemStack item: getOnlinePlayer().getInventory().getExtraContents())
				if (item != null && ItemType.hasEnchantMaterial(new ItemData(item)) && canAddCurse(item) && !hasAllCurses(item)) items.add(item);
		}
		return items;
	}

	private boolean hasAllCurses(ItemStack item) {
		boolean noCurse = true;
		for(CustomEnchantment enchantment: RegisterEnchantments.getCurseEnchantments())
			if (enchantment.isCurse() && EnchantmentUtils.canAddEnchantment(enchantment, item) && !EnchantmentUtils.hasEnchantment(item, enchantment.getRelativeEnchantment())) {
				noCurse = false;
				break;
			}
		return noCurse;
	}

	private boolean canAddCurse(ItemStack item) {
		boolean addCurse = false;
		if (EnchantmentUtils.hasEnchantment(item, RegisterEnchantments.CURSE_OF_STAGNANCY)) return false;
		for(CustomEnchantment enchantment: RegisterEnchantments.getCurseEnchantments())
			if (enchantment.isCurse() && EnchantmentUtils.canAddEnchantment(enchantment, item)) {
				addCurse = true;
				break;
			}
		return addCurse;
	}

	public int getExhaustion() {
		if (!isOnline()) return 0;
		return AbilityUtils.getExhaustionCurse(getOnlinePlayer());
	}

	public void setCurrentExhaustion() {
		pastExhaustion = currentExhaustion;
		currentExhaustion = AbilityUtils.getExhaustion(getOnlinePlayer());
	}

	public void resetCurrentExhaustion() {
		pastExhaustion = 0;
		currentExhaustion = 0;
	}

	public float getPastExhaustion() {
		return pastExhaustion;
	}

	public float getCurrentExhaustion() {
		return currentExhaustion;
	}

	public void setFlightSpeed(double flight) {
		flyAttribute.removeModifier(UUID.fromString("ffffffff-ffff-ffff-ffff-000000001111"));
		flyAttribute.addModifier(new AttributeModifier(UUID.fromString("ffffffff-ffff-ffff-ffff-000000001111"), "frequentFlyerLevel", flight, Operation.ADD_NUMBER));
		player.getPlayer().setFlySpeed((float) flyAttribute.getValue());
	}

	public void setCanFly(int level) {
		if (!isOnline()) return;
		Player player = getOnlinePlayer();
		boolean permission = PermissionUtils.check(player, "enchantmentsolution.abilities.has-external-flight");
		boolean modifyCanFly = level > 0 || player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR;
		this.canFly = modifyCanFly;
		if (this.canFly || !permission) {
			player.setAllowFlight(this.canFly);
			if (player.isFlying() && !this.canFly) player.setFlying(false);
		}
		if (!this.canFly) resetFlightSpeed();
		else if (level > 0 && player.isFlying()) minus();
	}

	public void resetFlightSpeed() {
		player.getPlayer().setFlySpeed((float) flyAttribute.getDefaultValue());
	}

	public int getFlightDamage() {
		return flightDamage;
	}

	public void minus() {
		Player player = getOnlinePlayer();
		if (player.getLocation().getY() >= 12000) AdvancementUtils.awardCriteria(player, ESAdvancement.CRUISING_ALTITUDE, "elytra");
		flightDamage--;
		if (flightDamage <= 0) {
			if (elytra != null) DamageUtils.damageItem(player, elytra);
			flightDamage = flightDamageLimit;
		}
		setDidTick(true);
	}

	public void setDidTick(boolean b) {
		didTick = b;
	}

	public boolean didTick() {
		return didTick;
	}

	public double getForceFeedChance(int level) {
		return 0.0075 + level * 0.0075;
	}

	public List<ItemStack> getForceFeedItems() {
		List<ItemStack> items = new ArrayList<ItemStack>();
		for(ItemStack item: getEquipped())
			if (EnchantmentUtils.hasEnchantment(item, RegisterEnchantments.FORCE_FEED)) items.add(item);
		return items;
	}

	public boolean hasForceFeed() {
		return getForceFeedItems().size() > 0;
	}

	public void setIcarusDelay(int icarusDelay) {
		this.icarusDelay = icarusDelay;
	}

	public int getIcarusDelay() {
		return icarusDelay;
	}

	public void minusIcarusDelay() {
		if (icarusDelay > 0) icarusDelay--;
	}

	public List<OverkillDeath> getOverkillDeaths() {
		return overkillDeaths;
	}

	public void addOverkillDeath(OverkillDeath death) {
		overkillDeaths.add(death);
	}

	public List<PotionEffect> getEffects() {
		return effects;
	}

	public void addPotionEffect(PotionEffect effect) {
		effects.add(effect);
	}

	public void removePotionEffect(PotionEffect effect) {
		Iterator<PotionEffect> iter = effects.iterator();
		while (iter.hasNext()) {
			PotionEffect eff = iter.next();
			if (effect.getType() == eff.getType()) iter.remove();
		}
	}

	public void giveTelepathyItems() {
		Collection<ItemStack> newItems = new ArrayList<ItemStack>(telepathyItems);
		telepathyItems = new ArrayList<ItemStack>();
		telepathyTask = null;
		Player p = Bukkit.getPlayer(getOnlinePlayer().getUniqueId());
		ItemUtils.giveItemsToPlayer(p, newItems, p.getLocation(), true, EquipMethod.PICK_UP);
	}

	public void addTelepathyItems(Collection<ItemStack> items) {
		checkTelepathyTask();
		for(ItemStack item: items)
			addTelepathyItem(item);
		checkTelepathyTask();
	}

	private void checkTelepathyTask() {
		if (telepathyTask == null) {
			telepathyTask = () -> giveTelepathyItems();
			Bukkit.getScheduler().runTaskLater(EnchantmentSolution.getPlugin(), telepathyTask, 0l);
		}
	}

	private void addTelepathyItem(ItemStack item) {
		checkTelepathyTask();
		int amount = item.getAmount();
		for(ItemStack i: telepathyItems)
			if (i.isSimilar(item) && i.getMaxStackSize() > 1) if (i.getAmount() == i.getMaxStackSize()) continue;
			else if (i.getAmount() + amount > i.getMaxStackSize()) {
				amount -= i.getMaxStackSize();
				i.setAmount(i.getMaxStackSize());
				break;
			} else {
				i.setAmount(amount + i.getAmount());
				amount = 0;
				break;
			}
			else {}
		if (amount > 0) {
			item.setAmount(amount);
			telepathyItems.add(item);
		}
		checkTelepathyTask();
	}

	public void addTimedDisableEnchant(JavaPlugin plugin, EnchantmentWrapper enchant, int ticks) {
		long tick = ServerUtils.getCurrentTick() + ticks;
		if (!isTimedDisableEnchant(plugin, enchant)) timedDisable.add(new EnchantmentTimedDisable(plugin, enchant, tick));
	}

	public void addTimeToDisableEnchant(JavaPlugin plugin, EnchantmentWrapper enchant, int moreTicks) {
		if (isTimedDisableEnchant(plugin, enchant)) {
			EnchantmentTimedDisable disable = getTimedDisable(plugin, enchant);
			disable.addToEndTime(moreTicks);
		} else
			addTimedDisableEnchant(plugin, enchant, moreTicks);
	}

	private EnchantmentTimedDisable getTimedDisable(JavaPlugin plugin, EnchantmentWrapper enchant) {
		for(EnchantmentTimedDisable etd: timedDisable)
			if (etd.isSimilar(plugin, enchant)) return etd;
		return null;
	}

	public void setTimeDisableEnchant(JavaPlugin plugin, EnchantmentWrapper enchant, int ticks) {
		long tick = ServerUtils.getCurrentTick() + ticks;
		if (isTimedDisableEnchant(plugin, enchant)) {
			EnchantmentTimedDisable disable = getTimedDisable(plugin, enchant);
			disable.setEndTime(tick);
		} else
			addTimedDisableEnchant(plugin, enchant, ticks);
	}

	public void removeTimedDisableEnchant(JavaPlugin plugin, EnchantmentWrapper enchant) {
		Iterator<EnchantmentTimedDisable> iter = timedDisable.iterator();
		while (iter.hasNext()) {
			EnchantmentTimedDisable etd = iter.next();
			if (etd.isSimilar(plugin, enchant)) iter.remove();
		}
	}

	public void removeTimeFromDisableEnchant(JavaPlugin plugin, EnchantmentWrapper enchant, int lessTicks) {
		if (isTimedDisableEnchant(plugin, enchant)) {
			EnchantmentTimedDisable disable = getTimedDisable(plugin, enchant);
			disable.removeFromEndTime(lessTicks);
		}
	}

	public boolean isTimedDisableEnchant(JavaPlugin plugin, EnchantmentWrapper enchant) {
		for(EnchantmentTimedDisable etd: timedDisable)
			if (etd.isSimilar(plugin, enchant)) return true;
		return false;
	}

	public boolean hasTimedDisable(Player player, EnchantmentWrapper enchant) {
		for(EnchantmentTimedDisable etd: timedDisable)
			if (etd.getEnchantment() == enchant) return true;
		return false;
	}

	public void setDisabledEnchant(JavaPlugin plugin, EnchantmentWrapper enchant) {
		if (!isDisabledEnchant(plugin, enchant)) disable.add(new EnchantmentDisable(plugin, enchant));
	}

	public boolean isDisabledEnchant(JavaPlugin plugin, EnchantmentWrapper enchant) {
		for(EnchantmentDisable e: disable)
			if (e.isSimilar(plugin, enchant)) return true;
		return false;
	}

	public void removeDisabledEnchant(JavaPlugin plugin, EnchantmentWrapper enchant) {
		Iterator<EnchantmentDisable> iter = disable.iterator();
		while (iter.hasNext()) {
			EnchantmentDisable e = iter.next();
			if (e.isSimilar(plugin, enchant)) iter.remove();
		}
	}

	public boolean hasDisabled(Player player, EnchantmentWrapper enchant) {
		for(EnchantmentDisable e: disable)
			if (e.getEnchantment() == enchant) return true;
		return false;
	}

	public boolean getPlyometricsAdvancement() {
		return plyometricsAdvancement;
	}

	public void setPlyometricsAdvancement(boolean plyometricsAdvancement) {
		this.plyometricsAdvancement = plyometricsAdvancement;
	}

	public void addUnderwaterTick() {
		for(ItemStack item: getArmor())
			if (EnchantmentUtils.hasEnchantment(item, RegisterEnchantments.WATER_BREATHING)) {
				underwaterTicks++;
				if (underwaterTicks >= 13900) AdvancementUtils.awardCriteria(onlinePlayer, ESAdvancement.WORLD_RECORD, "record");
				return;
			}
		resetUnderwaterTick();
	}

	public void resetUnderwaterTick() {
		underwaterTicks = 0;
	}

	public void addToHWDController(ItemStack item, Block original, List<Location> allBlocks) {
		for(AsyncHWDController controller: hwdControllers)
			if (controller.getItem().equals(item)) {
				controller.addBlocks(original, allBlocks);
				return;
			}
		hwdControllers.add(new AsyncHWDController(onlinePlayer, item, original, allBlocks));
	}

	public void removeNullHWDControllers() {
		Iterator<AsyncHWDController> iter = hwdControllers.iterator();
		while (iter.hasNext()) {
			AsyncHWDController controller = iter.next();
			if (controller.getItem() == null) {
				controller.removeBlocks();
				iter.remove();
			}
		}
	}

	public void removeAllHWDControllers() {
		Iterator<AsyncHWDController> iter = hwdControllers.iterator();
		while (iter.hasNext()) {
			AsyncHWDController controller = iter.next();
			controller.removeBlocks();
			iter.remove();
		}
	}

	public void runHWD() {
		Iterator<AsyncHWDController> iter = hwdControllers.iterator();
		while (iter.hasNext()) {
			AsyncHWDController controller = iter.next();
			controller.breakingBlocks();
			if (controller.willRemove()) {
				controller.removeBlocks();
				iter.remove();
			}
		}
		removeNullHWDControllers();
	}

	public void addToGaiaController(ItemStack item, Block original, List<Location> allBlocks, GaiaTrees tree) {
		List<AsyncGaiaController> controllers = gaiaControllers.get(tree);
		if (controllers == null) controllers = new ArrayList<AsyncGaiaController>();
		for(AsyncGaiaController controller: controllers)
			if (item.equals(controller.getItem()) && !controller.willRemove()) {
				controller.addBlocks(original, allBlocks);
				return;
			}
		controllers.add(new AsyncGaiaController(onlinePlayer, item, original, allBlocks, tree));
		gaiaControllers.put(tree, controllers);
	}

	public void removeGaiaControllers(ItemStack item) {
		Iterator<Entry<GaiaTrees, List<AsyncGaiaController>>> iter = gaiaControllers.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<GaiaTrees, List<AsyncGaiaController>> entry = iter.next();
			Iterator<AsyncGaiaController> controllers = entry.getValue().iterator();
			while (controllers.hasNext()) {
				AsyncGaiaController controller = controllers.next();
				if (item.equals(controller.getItem())) controllers.remove();
			}
		}
	}

	public void removeGaiaController(ItemStack item, GaiaTrees tree) {
		Iterator<AsyncGaiaController> controllers = gaiaControllers.get(tree).iterator();
		while (controllers.hasNext()) {
			AsyncGaiaController controller = controllers.next();
			if (item.equals(controller.getItem())) controllers.remove();
		}
	}

	public void removeNullGaiaControllers() {
		Iterator<Entry<GaiaTrees, List<AsyncGaiaController>>> iter = gaiaControllers.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<GaiaTrees, List<AsyncGaiaController>> entry = iter.next();
			Iterator<AsyncGaiaController> controllers = entry.getValue().iterator();
			while (controllers.hasNext()) {
				AsyncGaiaController controller = controllers.next();
				if (controller.getItem() == null) controllers.remove();
			}
		}
	}

	public void removeAllGaiaControllers() {
		Iterator<Entry<GaiaTrees, List<AsyncGaiaController>>> iter = gaiaControllers.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<GaiaTrees, List<AsyncGaiaController>> entry = iter.next();
			Iterator<AsyncGaiaController> controllers = entry.getValue().iterator();
			while (controllers.hasNext()) {
				AsyncGaiaController controller = controllers.next();
				controller.removeBlocks();
			}
			iter.remove();
		}
	}

	public void runGaia() {
		Iterator<Entry<GaiaTrees, List<AsyncGaiaController>>> iter = gaiaControllers.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<GaiaTrees, List<AsyncGaiaController>> entry = iter.next();
			Iterator<AsyncGaiaController> controllers = entry.getValue().iterator();
			while (controllers.hasNext()) {
				AsyncGaiaController controller = controllers.next();
				controller.breakingBlocks();
				if (controller.willRemove()) {
					controller.removeBlocks();
					controllers.remove();
				}
			}
		}
		removeNullGaiaControllers();
	}

	public void createModel(Block start, Location low, Location high, ItemStack item) {
		NamespacedKey key = new NamespacedKey(EnchantmentSolution.getPlugin(), "hwd_" + player.getName() + "_" + modelKey);
		modelKey++;
		ItemMeta meta = item.getItemMeta();
		meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, 1);
		item.setItemMeta(meta);
		models.add(new HWDModel(start, low, high, player, key, item));
	}

	public void runHWDModel() {
		long currentTick = ServerUtils.getCurrentTick();
		Iterator<HWDModel> iter = models.iterator();
		while (iter.hasNext() && canBreakBlock()) {
			HWDModel model = iter.next();
			if (currentTick != ServerUtils.getCurrentTick()) {
				DELAY_TICKS = currentTick + ConfigString.ASYNC_BLOCK_DELAY.getInt();
				break;
			}
			while (canBreakBlock()) {
				if (model.getCurrent().size() <= 0) {
					ItemStack item = model.getItem();
					if (item != null) {
						ItemMeta meta = item.getItemMeta();
						meta.getPersistentDataContainer().remove(model.getKey());
						item.setItemMeta(meta);
					}
					iter.remove();
					break;
				}
				if (model.hasItem()) {
					ItemStack item = model.getItem();
					Block block = model.getCurrent().get(0);
					if (!canBreakBlock()) break;
					List<Material> pickBlocks = ItemBreakType.getDiamondPickaxeBlocks();
					if (!pickBlocks.contains(model.getStartingPointMaterial()) && pickBlocks.contains(block.getType())) {
						model.aroundBlock(block);
						model.setUsed(block);
						continue;
					}

					if (ItemBreakType.getType(item.getType()).getBreakTypes().contains(block.getType())) {
						BlockUtils.addMultiBlockBreak(block.getLocation(), RegisterEnchantments.HEIGHT_PLUS_PLUS);
						if (BlockUtils.multiBreakBlock(player.getPlayer(), item, block.getLocation(), RegisterEnchantments.HEIGHT_PLUS_PLUS)) breakBlock();
					}
					model.aroundBlock(block);
					model.setUsed(block);
				} else {
					iter.remove();
					break;
				}
			}
		}
	}

	public void removeHWDModels() {
		Iterator<HWDModel> iter = models.iterator();
		while (iter.hasNext()) {
			HWDModel model = iter.next();
			if (model.hasItem()) {
				ItemStack item = model.getItem();
				ItemMeta meta = item.getItemMeta();
				meta.getPersistentDataContainer().remove(model.getKey());
				item.setItemMeta(meta);
			}
			iter.remove();
		}
	}

	public void removeHWDModels(ItemStack item) {
		Iterator<HWDModel> iter = models.iterator();
		while (iter.hasNext()) {
			HWDModel model = iter.next();
			if (model.hasItem() && item.isSimilar(model.getItem())) {
				ItemMeta meta = item.getItemMeta();
				meta.getPersistentDataContainer().remove(model.getKey());
				item.setItemMeta(meta);
			}
			iter.remove();
		}
	}

	public void removeInvalidHWDModels() {
		Iterator<HWDModel> iter = models.iterator();
		while (iter.hasNext()) {
			HWDModel model = iter.next();
			if (!model.hasItem()) {
				ItemStack item = model.getPossibleItem();
				if (item != null) {
					ItemMeta meta = item.getItemMeta();
					meta.getPersistentDataContainer().remove(model.getKey());
					item.setItemMeta(meta);
				}
				iter.remove();
			}
		}
	}

	public void endCooldowns() {
		Iterator<Entry<EnchantmentWrapper, Long>> iter = cooldowns.entrySet().iterator();

		while (iter.hasNext()) {
			Entry<EnchantmentWrapper, Long> entry = iter.next();
			if (!(entry.getValue().longValue() > ServerUtils.getCurrentTick())) {
				if (entry.getKey() == RegisterEnchantments.ICARUS) {
					Player p = getOnlinePlayer();
					if (p == null) return;
					p.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, p.getLocation(), 250, 2, 2, 2);
					p.getWorld().playSound(p.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1, 1);
				}

				iter.remove();
			}
		}
	}

	public boolean hasCooldown(Player player, EnchantmentWrapper enchantment) {
		return cooldowns.containsKey(enchantment);
	}

	public long getEnchantmentSeed(ItemData data, int bookshelves) {
		Iterator<Seed> iter = enchantmentSeeds.iterator();
		while (iter.hasNext()) {
			Seed seed = iter.next();
			if (seed.getData().equals(data)) return seed.getFinalSeed(bookshelves);
		}
		Seed seed = new Seed(data, (int) (Math.random() * Integer.MAX_VALUE));
		enchantmentSeeds.add(seed);
		return seed.getFinalSeed(bookshelves);
	}

	public List<Seed> getEnchantmentSeeds() {
		return enchantmentSeeds;
	}

	public void setEnchantmentSeed(ItemData data, int num) {
		for (Seed s : enchantmentSeeds)
			if (s.getData().equals(data)) {
				s.setSeed(num);
				return;
			}
		Seed seed = new Seed(data, num);
		enchantmentSeeds.add(seed);
	}

	public void updateEnchantmentSeeds() {
		Iterator<Seed> iter = enchantmentSeeds.iterator();
		while (iter.hasNext()) {
			iter.next();
			iter.remove();
		}
		levelSeed = new Random().nextLong();
		canSeeEnchantments = willSeeEnchantments;
		willSeeEnchantments = false;
		currentEchoShards = nextEchoShards;
		nextEchoShards = 0;
	}

	public long getLevelSeed() {
		return levelSeed;
	}

	public boolean willSeeEnchantments() {
		return willSeeEnchantments;
	}

	public void setWillSeeEnchantments(boolean willSeeEnchantments) {
		this.willSeeEnchantments = willSeeEnchantments;
	}

	public boolean canSeeEnchantments() {
		return canSeeEnchantments;
	}

	public void setCanSeeEnchantments(boolean canSeeEnchantments) {
		this.canSeeEnchantments = canSeeEnchantments;
	}

	public int getNextEchoShards() {
		return nextEchoShards;
	}

	public void setNextEchoShards(int nextEchoShards) {
		this.nextEchoShards = nextEchoShards;
	}

	public void addNextEchoShards(int nextEchoShards) {
		this.nextEchoShards += nextEchoShards;
	}

	public int getCurrentEchoShards() {
		return currentEchoShards;
	}

	public void setCurrentEchoShards(int currentEchoShards) {
		this.currentEchoShards = currentEchoShards;
	}

	public void setEquipTimer() {
		equipItem.set(true, ServerUtils.getCurrentTick());
	}

	public void runEquipTimer() {
		Player player = getOnlinePlayer();
		if (equipItem.shouldCheck()) {
			double health = player.getHealth();
			equipItem.setCheck(false);
			Iterator<AttributeLevel> attr = getAttributes().iterator();
			while (attr.hasNext()) {
				AttributeLevel level = attr.next();
				AttributeInstance instance = player.getAttribute(level.getAttr());
				instance.removeModifier(level.getAttribute());
				if (level.getAttr() == Attribute.GENERIC_MAX_HEALTH) DamageNMS.updateHealth(player);
				attr.remove();
			}
			Iterator<PotionEffect> effects = getEffects().iterator();
			while (effects.hasNext()) {
				PotionEffect effect = effects.next();
				if (effect.isInfinite()) {
					player.removePotionEffect(effect.getType());
					effects.remove();
				}
			}
			for(ItemSlot slot: getEquippedAndType()) {
				ItemStack item = slot.getItem();
				if (item != null && EnchantmentUtils.getTotalEnchantments(item) > 0) {
					EquipEvent armorEquipEvent = new EquipEvent(player, EquipMethod.COMMAND, slot.getType(), item, item);
					Bukkit.getServer().getPluginManager().callEvent(armorEquipEvent);
				}
			}
			double finalHealth = player.getHealth();
			if (health != finalHealth && health <= player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()) {
				player.setHealth(health);
				DamageNMS.updateHealth(player);
			}
		}
	}

	public List<AttributeLevel> getAttributes() {
		return attributes;
	}

	public void addAttribute(AttributeLevel attribute) {
		attributes.add(attribute);
	}

	public void removeAttribute(AttributeModifier attribute) {
		Iterator<AttributeLevel> iter = attributes.iterator();
		while (iter.hasNext()) {
			AttributeLevel level = iter.next();
			if (level.getAttribute().getUniqueId().equals(attribute.getUniqueId())) iter.remove();
		}
	}

	public void setLevelSeed(long l) {
		this.levelSeed = l;
	}
}
