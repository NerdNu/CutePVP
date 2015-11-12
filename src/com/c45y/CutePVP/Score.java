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

	/**
	 * The team's overall score; primarily affected by captures.
	 */
	public Count score = new Count("Score");

	// ------------------------------------------------------------------------
	/**
	 * Represents one counted (integer) property as a public field of Score.
	 */
	public static class Count {
		// --------------------------------------------------------------------
		/**
		 * Constructor.
		 * 
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
		 * Increment the count by the given amount.
		 * @param amount the amount by which to increment the count.
		 */
		public void increment(int amount) {
			_count += amount;
		}

		// --------------------------------------------------------------------
		/**
		 * Set the count to the specified value.
		 *
		 * @param count the new count
		 */
		public void set(int count) {
			_count = count;
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

		@Override
		public String toString() {
			return ChatColor.GOLD + getName() + ": " + ChatColor.WHITE + get();
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
			message.append(' ').append(count);
		}
		return message.toString();
	}

	public String[] getLines() {
		return new String[]{
				" " + kills + " " + deaths + " " + buffs,
				" " + steals + " " + captures + " " + returns + " " + score
		};
	}

	// ------------------------------------------------------------------------
	/**
	 * A list of all Count fields, used to automate load(), save() and
	 * toString().
	 */
	private Count[] _counts = new Count[] { kills, deaths, steals, captures, returns, buffs, score };
} // class Score