package org.ctp.enchantmentsolution.events.equip;

import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Entity;
import org.ctp.enchantmentsolution.enchantments.CERegister;
import org.ctp.enchantmentsolution.enchantments.helper.EnchantmentLevel;

public class LifeAttributeEvent extends AttributeEvent {

	public LifeAttributeEvent(Entity who, int level, boolean equip, AttributeModifier modifier) {
		super(who, new EnchantmentLevel(CERegister.ARMORED, level), equip, modifier);
	}
}