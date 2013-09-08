package com.c45y.CutePVP;

import org.bukkit.ChatColor;
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
	public Count kills = new Count("Kills");

	/**
	 * Number of times the scorer dies in the match area. "Natural" deaths count
	 * the same as PvP deaths because sometime people suicide to avoid PvP.
	 */
	public Count deaths = new Count("Deaths");

	/**
	 * Number of times this scorer has taken an enemy flag.
	 */
	public Count steals = new Count("Steals");

	/**
	 * Number of times this scorer has returned an own team flag stolen by an
	 * enemy.
	 */
	public Count returns = new Count("Returns");

	/**
	 * Number of times this scorer has carried an enemy flag to the scoring
	 * zone.
	 */
	public Count captures = new Count("Captures");

	/**
	 * Number of team buffs claimed by this scorer.
	 */
	public Count buffs = new Count("Buffs");

	// ------------------------------------------------------------------------
	/**
	 * Represents one counted (integer) property as a public field of Score.
	 */
	public static class Count {
		// --------------------------------------------------------------------
		/**
		 * Constructor.
		 * 
		 * @param id the persistent ID of the property in the config file.
		 * @param name the name of the property presented to players in score
		 *        messages.
		 */
		public Count(String name) {
			this(name, name.toLowerCase());
		}

		// --------------------------------------------------------------------
		/**
		 * Constructor.
		 * 
		 * @param name the name of the property presented to players in score
		 *        messages.
		 * @param id the persistent ID of the property in the config file.
		 */
		public Count(String name, String id) {
			_name = name;
			_id = id;
		}

		// --------------------------------------------------------------------
		/**
		 * Increment the count by one.
		 */
		public void increment() {
			++_count;
		}

		// --------------------------------------------------------------------
		/**
		 * Return the current count.
		 * 
		 * @return the current count.
		 */
		public int get() {
			return _count;
		}

		// --------------------------------------------------------------------
		/**
		 * Return the descriptive name of this count presented to the players.
		 * 
		 * @return the descriptive name of this count presented to the players.
		 */
		public String getName() {
			return _name;
		}

		// --------------------------------------------------------------------
		/**
		 * Load the count value from the configuration.
		 * 
		 * @param section the section of the configuration under which the count
		 *        is stored.
		 */
		public void load(ConfigurationSection section) {
			_count = section.getInt(_id);
		}

		// --------------------------------------------------------------------
		/**
		 * Save the count value to the configuration.
		 * 
		 * @param section the section of the configuration under which the count
		 *        is stored.
		 */
		public void save(ConfigurationSection section) {
			section.set(_id, _count);
		}

		// --------------------------------------------------------------------
		/**
		 * The programmatic ID of this count in the configuration file.
		 */
		private String _id;

		/**
		 * The descriptive name of this count presented to the players.
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
		for (Count count : _counts) {
			message.append(ChatColor.GOLD).append(" ").append(count.getName()).append(": ");
			message.append(ChatColor.WHITE).append(count.get());
		}
		return message.toString();
	}

	// ------------------------------------------------------------------------
	/**
	 * A list of all Count fields, used to automate load(), save() and
	 * toString().
	 */
	private Count[] _counts = new Count[] { kills, deaths, steals, captures, returns, buffs };
} // class Score