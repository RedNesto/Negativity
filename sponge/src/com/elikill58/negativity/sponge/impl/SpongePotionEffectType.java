package com.elikill58.negativity.sponge.impl;

import org.spongepowered.api.effect.potion.PotionEffectType;
import org.spongepowered.api.effect.potion.PotionEffectTypes;

public class SpongePotionEffectType {
	
	public static PotionEffectType getEffect(com.elikill58.negativity.api.potion.PotionEffectType type) {
		switch (type) {
		case BLINDNESS:
			return PotionEffectTypes.BLINDNESS;
		case CONFUSION:
			return PotionEffectTypes.NAUSEA;
		case DOLPHINS_GRACE:
			return null;
		case FAST_DIGGING:
			return PotionEffectTypes.HASTE;
		case HUNGER:
			return PotionEffectTypes.HUNGER;
		case INSTANT_HEAL:
			return PotionEffectTypes.INSTANT_HEALTH;
		case JUMP:
			return PotionEffectTypes.JUMP_BOOST;
		case LEVITATION:
			return PotionEffectTypes.LEVITATION;
		case NIGHT_VISION:
			return PotionEffectTypes.NIGHT_VISION;
		case POISON:
			return PotionEffectTypes.POISON;
		case REGENERATION:
			return PotionEffectTypes.REGENERATION;
		case SLOW_DIGGING:
			return PotionEffectTypes.MINING_FATIGUE;
		case SPEED:
			return PotionEffectTypes.SPEED;
		case WEAKNESS:
			return PotionEffectTypes.WEAKNESS;
		case WITHER:
			return PotionEffectTypes.WITHER;
		default:
			return null;
		}
	}
}
