package com.c45y.CutePVP.util;

import java.util.HashMap;

import org.bukkit.World;

// ----------------------------------------------------------------------------
/**
 * Limit the rate at which something can happen in a given World.
 */
public class RateLimiter {
	// ------------------------------------------------------------------------
	/**
	 * Return true if the action can be performed in the specified world given
	 * the number of cool down ticks that must transpire.
	 * 
	 * If this method is true, the implementation assumes that the action is
	 * indeed performed.
	 * 
	 * @param world the world.
	 * @param minElapsedTicks the minimum elapsed time between actions in the
	 *        world, in ticks.
	 * @return true if the action can be performed again.
	 */
	public boolean canAct(World world, long minElapsedTicks) {
		Long lastTime = _lastTime.get(world.getName());
		if (lastTime == null || world.getFullTime() - lastTime >= minElapsedTicks) {
			_lastTime.put(world.getName(), world.getFullTime());
			return true;
		}
		return false;
	}

	// ------------------------------------------------------------------------
	/**
	 * Map from World name to corresponding full time at which action was last
	 * performed.
	 */
	protected HashMap<String, Long> _lastTime = new HashMap<String, Long>();
} // class RateLimiter