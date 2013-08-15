package com.c45y.CutePVP.buff;

import java.util.logging.Logger;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.material.MaterialData;

// ----------------------------------------------------------------------------
/**
 * Potion effects applied to individual players when they walk on blocks of
 * distinguished types.
 */
public class FloorBuff extends Buff {
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
			Material material = Material.matchMaterial(section.getString("material", ""));
			byte data = (byte) (section.getInt("data") & 0xF);
			_materialData = new MaterialData(material, data);
			return true;
		} catch (Exception ex) {
			logger.severe("Unable to load material for floor buff " + section.getName());
			return false;
		}
	}

	// ------------------------------------------------------------------------
	/**
	 * Return the type of material that the player must touch in order to get
	 * the buff.
	 * 
	 * @return the type of material that the player must touch in order to get
	 *         the buff.
	 */
	public MaterialData getMaterialData() {
		return _materialData;
	}

	// ------------------------------------------------------------------------
	/**
	 * Material and data value that imparts the buff.
	 */
	private MaterialData _materialData;
} // class FloorBuff