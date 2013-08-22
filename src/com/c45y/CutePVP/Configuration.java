package com.c45y.CutePVP;

import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;

import com.c45y.CutePVP.util.ConfigHelper;

// ----------------------------------------------------------------------------
/**
 * Plugin configuration.
 * 
 * Seconds are used for timeouts rather than ticks. Only the periods of
 * repeating tasks added to the Bukkit scheduler are specified in ticks. The
 * reason for this is twofold:
 * <ul>
 * <li>Firstly, in all these cases, we're interested in elapsed real-world time,
 * rather than ticks, which dependent on the actual tick rate of the server,
 * which is rarely 20 under heavy load.</li>
 * <li>Secondly, using the ProperTime plugin to adjust the day cycle appears to
 * cause problems with World.getFullTime() advancing by large fractions of a day
 * after restarts, leading to buffs timing out unexpectedly when using ticks.</li>
 * </ul>
 * 
 * On the downside, this scheme will now take into account the time required to
 * restart the server.
 */
public class Configuration {
	// ------------------------------------------------------------------------
	/**
	 * Number of seconds before a dropped flag is automatically returned.
	 */
	public int FLAG_DROPPED_SECONDS;

	/**
	 * The number of ticks between flame effects displayed at each flag.
	 */
	public int FLAG_FLAME_TICKS;

	/**
	 * The total number of seconds from the time a team buff is claimed to when
	 * it expires.
	 */
	public int TEAM_BUFF_SECONDS;

	/**
	 * If true, players can be attacked by the enemy in their own base.
	 * 
	 * That is, Team A can be hurt by Team B in Team A's base.
	 */
	public boolean CAN_ATTACK_IN_ENEMY_BASE;

	/**
	 * If true, players can edit within an enemy team's base region.
	 */
	public boolean CAN_EDIT_ENEMY_BASE;

	// ------------------------------------------------------------------------
	/**
	 * Sound played when a player steals a flag.
	 */
	public Sound FLAG_STEAL_SOUND;

	/**
	 * Sound played when a player returns a flag.
	 */
	public Sound FLAG_RETURN_SOUND;

	/**
	 * Sound played when a player captures a flag.
	 */
	public Sound FLAG_CAPTURE_SOUND;

	/**
	 * Sound played when a player captures a team buff.
	 */
	public Sound TEAM_BUFF_SOUND;

	// ------------------------------------------------------------------------
	/**
	 * Constructor.
	 * 
	 * @param plugin the owning plugin.
	 */
	public Configuration(CutePVP plugin) {
		_plugin = plugin;
	}

	// ------------------------------------------------------------------------
	/**
	 * Load all configuration.
	 */
	public void load() {
		_plugin.getTeamManager().load();
		_plugin.getBuffManager().load();

		FLAG_FLAME_TICKS = _plugin.getConfig().getInt("misc.flag_flame_ticks", 7);
		FLAG_DROPPED_SECONDS = _plugin.getConfig().getInt("misc.flag_dropped_seconds", 300);
		TEAM_BUFF_SECONDS = _plugin.getConfig().getInt("misc.team_buff_seconds", 1800);
		CAN_ATTACK_IN_ENEMY_BASE = _plugin.getConfig().getBoolean("protections.can_attack_in_enemy_base", false);
		CAN_EDIT_ENEMY_BASE = _plugin.getConfig().getBoolean("protections.can_edit_enemy_base", false);

		ConfigHelper helper = new ConfigHelper(_plugin.getLogger());
		ConfigurationSection sounds = _plugin.getConfig().getConfigurationSection("sounds");
		FLAG_STEAL_SOUND = helper.loadSound(sounds, "steal", Sound.AMBIENCE_THUNDER, true);
		FLAG_RETURN_SOUND = helper.loadSound(sounds, "return", Sound.ORB_PICKUP, true);
		FLAG_CAPTURE_SOUND = helper.loadSound(sounds, "capture", Sound.LEVEL_UP, true);
		TEAM_BUFF_SOUND = helper.loadSound(sounds, "buff", Sound.WITHER_SPAWN, true);
	}

	// ------------------------------------------------------------------------
	/**
	 * Save all configuration.
	 */
	public void save() {
		_plugin.getTeamManager().save();
		_plugin.getBuffManager().save();

		_plugin.getConfig().set("misc.flag_flame_ticks", FLAG_FLAME_TICKS);
		_plugin.getConfig().set("misc.flag_dropped_seconds", FLAG_DROPPED_SECONDS);
		_plugin.getConfig().set("misc.team_buff_seconds", TEAM_BUFF_SECONDS);
		_plugin.saveConfig();
	}

	// ------------------------------------------------------------------------
	/**
	 * The owning plugin.
	 */
	private CutePVP _plugin;
} // class Configuration