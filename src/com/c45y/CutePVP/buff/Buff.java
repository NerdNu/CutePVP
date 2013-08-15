package com.c45y.CutePVP.buff;

import java.util.HashSet;
import java.util.logging.Logger;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import com.c45y.CutePVP.util.ConfigHelper;

// ----------------------------------------------------------------------------
/**
 * Represents a set of beneficial or harmful potion effects that are applied as
 * a result of a player's contact with distinguished blocks.
 * 
 * <ul>
 * <li>{@link FloorBuff}s are applied to <i>individual players</i> as a
 * consequence of walking on a particular kind of block.</li>
 * <li>{@link TeamBuff}s are applied to <i>whole teams</i> when one team member
 * captures a buff block by right clicking on it. (Unlike flags, the block does
 * not move.)</li>
 * </ul>
 */
public class Buff {
	// ------------------------------------------------------------------------
	/**
	 * Load this buff from the specified configuration section.
	 * 
	 * @param section the configuration section.
	 * @param logger used to log messages.
	 * @return true if successfully loaded.
	 */
	public boolean load(ConfigurationSection section, Logger logger) {
		try {
			ConfigHelper helper = new ConfigHelper(logger);
			_potions = helper.loadPotions(section, "potions", true);
			return true;

		} catch (Exception ex) {
			logger.severe("Unable to load potions for buff " + section.getName());
			return false;
		}
	}

	// ------------------------------------------------------------------------
	/**
	 * Apply all potion effects of this buff to the player.
	 * 
	 * TODO: investigate what happens when a player already has an active potion
	 * of the same kind and higher level. Also, if I don't force, does speed
	 * expire etc.?
	 * 
	 * @param player the affected player.
	 */
	public void apply(Player player) {
		for (PotionEffect potion : _potions) {
			player.addPotionEffect(potion);
		}
	}

	// ------------------------------------------------------------------------
	/**
	 * The potion effects of this buff.
	 */
	private HashSet<PotionEffect> _potions;
} // class Buff
