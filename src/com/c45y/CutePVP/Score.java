package com.c45y.CutePVP;

import org.bukkit.configuration.ConfigurationSection;

// ----------------------------------------------------------------------------
/**
 * Represents a score, comprising kills, steals, returns and captures.
 * 
 * A "steal" is the act of taking the enemy flag. A "capture" is carrying an
 * enemy flag into the scoring zone. A "return" is touching one's own team's
 * flag after it was stolen and dropped by an enemy, thus returning the flag to
 * its home position.
 */
public class Score {
	/**
	 * Number of enemies killed by this scorer.
	 */
	public Count kills = new Count("kills");

	/**
	 * Number of times this scorer has taken an enemy flag.
	 */
	public Count steals = new Count("steals");

	/**
	 * Number of times this scorer has returned an own team flag stolen by an
	 * enemy.
	 */
	public Count returns = new Count("returns");

	/**
	 * Number of times this scorer has carried an enemy flag to the scoring
	 * zone.
	 */
	public Count captures = new Count("captures");

	// ------------------------------------------------------------------------
	/**
	 * Represents one counted (integer) property as a public field of Score.
	 */
	public static class Count {
		/**
		 * Constructor.
		 * 
		 * @param name the persistent name of the property in the config file.
		 */
		public Count(String name) {
			_name = name;
		}

		/**
		 * Increment the count by one.
		 */
		public void increment() {
			++_count;
		}

		/**
		 * Return the current count.
		 * 
		 * @return the current count.
		 */
		public int get() {
			return _count;
		}

		/**
		 * Load the count value from the configuration.
		 * 
		 * @param section the section of the configuration under which the count
		 *        is stored.
		 */
		public void load(ConfigurationSection section) {
			_count = section.getInt(_name);
		}

		/**
		 * Save the count value to the configuration.
		 * 
		 * @param section the section of the configuration under which the count
		 *        is stored.
		 */
		public void save(ConfigurationSection section) {
			section.set(_name, _count);
		}

		/**
		 * The name of this count in the configuration file.
		 */
		private String _name;

		/**
		 * The integer value of the count (e.g. number of kills).
		 */
		private int _count;
	}; // inner class Count

	// ------------------------------------------------------------------------
	/**
	 * Load the score from the specified section.
	 * 
	 * @param section the section.
	 */
	public void load(ConfigurationSection section) {
		for (Count count : _counts) {
			count.load(section);
		}
	}

	// ------------------------------------------------------------------------
	/**
	 * Save the score to the specified section.
	 * 
	 * @param section the section.
	 */
	public void save(ConfigurationSection section) {
		for (Count count : _counts) {
			count.save(section);
		}
	}

	// ------------------------------------------------------------------------
	/**
	 * Format this score as a string for presentation to the player.
	 * 
	 * @return the formatted representation of this score.
	 */
	public String toString() {
		StringBuilder message = new StringBuilder();
		message.append(" Kills: ").append(kills.get());
		message.append(" Steals: ").append(steals.get());
		message.append(" Captures: ").append(captures.get());
		message.append(" Returns: ").append(returns.get());
		return message.toString();
	}
	
	// ------------------------------------------------------------------------
	/**
	 * A list of all Count fields, used to automate load() and save().
	 */
	private Count[] _counts = new Count[] { kills, steals, returns, captures };
} // class Score