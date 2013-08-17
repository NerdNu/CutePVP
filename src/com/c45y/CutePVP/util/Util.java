package com.c45y.CutePVP.util;

import org.bukkit.Location;

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
} // class Blocks