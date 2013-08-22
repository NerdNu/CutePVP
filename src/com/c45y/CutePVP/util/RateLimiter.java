package com.c45y.CutePVP.util;

// ----------------------------------------------------------------------------
/**
 * Limit the rate at which something can happen.
 * 
 * Time is measured using System.currentTimeMillis() rather than server ticks.
 * This is considered to be more accurate because server ticks are affected by
 * high server load and the the world's full time has been observed to advance
 * unexpectedly after restart when using the ProperTime plugin.
 */
public class RateLimiter {
	// ------------------------------------------------------------------------
	/**
	 * Return true if the action can be performed in the specified world given
	 * the number of cool down milliseconds that must transpire.
	 * 
	 * If this method is true, the implementation assumes that the action is
	 * indeed performed.
	 * 
	 * @param minElapsedMillis the minimum elapsed time between actions in the
	 *        world, in milliseconds.
	 * @return true if the action can be performed again.
	 */
	public boolean canAct(long minElapsedMillis) {
		long now = System.currentTimeMillis();
		if (now - _lastTime >= minElapsedMillis) {
			_lastTime = now;
			return true;
		}
		return false;
	}

	// ------------------------------------------------------------------------
	/**
	 * The time at which action was last performed.
	 */
	protected long _lastTime;
} // class RateLimiter