package com.c45y.CutePVP;

import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;

import com.c45y.CutePVP.util.ConfigHelper;

// ----------------------------------------------------------------------------
/**
 * Plugin configuration.
 */
public class Configuration {
	// ------------------------------------------------------------------------
	/**
	 * Number of ticks before a dropped flag is automatically returned.
	 */
	public int FLAG_DROPPED_TICKS;

	/**
	 * The number of ticks between flame effects displayed at each flag.
	 */
	public int FLAG_FLAME_TICKS;

	/**
	 * The total number of ticks from the time a team buff is claimed to when it
	 * expires.
	 */
	public int TEAM_BUFF_TICKS;

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
		FLAG_DROPPED_TICKS = _plugin.getConfig().getInt("misc.flag_dropped_ticks", 5 * Constants.ONE_MINUTE_TICKS);
		TEAM_BUFF_TICKS = _plugin.getConfig().getInt("misc.team_buff_ticks", 30 * Constants.ONE_MINUTE_TICKS);
		
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
		_plugin.getConfig().set("misc.flag_dropped_ticks", FLAG_DROPPED_TICKS);
		_plugin.getConfig().set("misc.team_buff_ticks", TEAM_BUFF_TICKS);
		_plugin.saveConfig();
	}

	// ------------------------------------------------------------------------
	/**
	 * The owning plugin.
	 */
	private CutePVP _plugin;
} // class Configuration