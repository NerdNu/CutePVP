package com.c45y.CutePVP.buff;

import java.util.Collection;
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
	 * If the player already has a potion buff of a higher level, retain that.
	 * I believe this is different from the "force" flag in 
	 * Player.addPotionEffect(), in that the latter forces the new potion 
	 * without regard to whether it is weaker or stronger in effect.
	 * 
	 * @param player the affected player.
	 */
	public void apply(Player player) {
		Collection<PotionEffect> activePotions = player.getActivePotionEffects();
		for (PotionEffect potion : _potions) {
			if (! player.hasPotionEffect(potion.getType())) {
				player.addPotionEffect(potion);
			} else {
				// Find current active effect of the same type.
				PotionEffect current = null;
				for (PotionEffect active : activePotions) {
					if (active.getType() == potion.getType()) {
						current = active;
						break;
					}
				}
				
				// Add the new potion if it is at least as strong.
				// If the same stength, this may just refresh the duration.
				if (potion.getAmplifier() >= current.getAmplifier()) {
					player.addPotionEffect(potion, true);
				}
			}
		}
	} // apply

	// ------------------------------------------------------------------------
	/**
	 * The potion effects of this buff.
	 */
	private HashSet<PotionEffect> _potions;
} // class Buff
