package com.c45y.CutePVP.util;

import org.bukkit.Location;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

// --------------------------------------------------------------------------
/**
 * Miscellaneous utility methods.
 */
public class Util {
	/**
	 * Return true if Locations a and b refer to the same Block.
	 * 
	 * @param a a Location.
	 * @param b another Location.
	 * @return true if Locations a and b refer to the same Block.
	 */
	public static boolean isSameBlock(Location a, Location b) {
		return  a.getBlockX() == b.getBlockX() &&
				a.getBlockY() == b.getBlockY() &&
				a.getBlockZ() == b.getBlockZ();
	}

	/**
	 * Formats a {@link PotionEffect} as its proper name and level.
	 *
	 * @param effect the effect to format.
	 * @return the formatted effect string.
	 */
	public static String formatEffect(PotionEffect effect) {
		return effectMap.get(effect.getType()) + " " + toRoman(effect.getAmplifier() + 1);
	}

	/**
	 * Converts a given integer to roman numerals. n must be between 1 and 10,
	 * inclusively. Otherwise, the empty string is returned.
	 *
	 * @param n the integer to convert.
	 * @return the roman numeral for n.
	 */
	private static String toRoman(int n) {
		if (n <= 0 || n > 10) {
			return "";
		}
		return roman[n - 1];
	}

	/**
	 * An array of Roman numerals from 1 to 10, for use by {@link #toRoman(int)}.
	 */
	private static String[] roman = new String[]{"I", "II", "III", "IV", "V", "VI", "VII", "IIX", "IX", "X"};

	/**
	 * A map from {@link PotionEffectType}s to their proper names, for use by {@link #formatEffect(PotionEffect)}.
	 */
	private static Map<PotionEffectType, String> effectMap = new HashMap<PotionEffectType, String>();
	static {
		effectMap.put(PotionEffectType.ABSORPTION,        "Absorption");
		effectMap.put(PotionEffectType.BLINDNESS,         "Blindness");
		effectMap.put(PotionEffectType.CONFUSION,         "Nausea");
		effectMap.put(PotionEffectType.DAMAGE_RESISTANCE, "Resistance");
		effectMap.put(PotionEffectType.FAST_DIGGING,      "Haste");
		effectMap.put(PotionEffectType.FIRE_RESISTANCE,   "Fire Resistance");
		effectMap.put(PotionEffectType.HARM,              "Instant Damage");
		effectMap.put(PotionEffectType.HEAL,              "Instant Health");
		effectMap.put(PotionEffectType.HEALTH_BOOST,      "Health Boost");
		effectMap.put(PotionEffectType.HUNGER,            "Hunger");
		effectMap.put(PotionEffectType.INCREASE_DAMAGE,   "Strength");
		effectMap.put(PotionEffectType.INVISIBILITY,      "Invisibility");
		effectMap.put(PotionEffectType.JUMP,              "Jump Boost");
		effectMap.put(PotionEffectType.NIGHT_VISION,      "Night Vision");
		effectMap.put(PotionEffectType.POISON,            "Poison");
		effectMap.put(PotionEffectType.REGENERATION,      "Regeneration");
		effectMap.put(PotionEffectType.SATURATION,        "Saturation");
		effectMap.put(PotionEffectType.SLOW,              "Slowness");
		effectMap.put(PotionEffectType.SLOW_DIGGING,      "Mining Fatigue");
		effectMap.put(PotionEffectType.SPEED,             "Speed");
		effectMap.put(PotionEffectType.WATER_BREATHING,   "Water Breathing");
		effectMap.put(PotionEffectType.WEAKNESS,          "Weakness");
		effectMap.put(PotionEffectType.WITHER,            "Wither");
	}
} // class Blocks