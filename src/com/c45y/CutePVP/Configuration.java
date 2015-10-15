package com.c45y.CutePVP;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.bukkit.Location;
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
	 * The duration in seconds that a flag can be away from from its home before
	 * it is automatically returned, whether carried or dropped. A team must
	 * therefore capture the flag within that time or be forced to steal it
	 * again.
	 */
	public int FLAG_CAPTURE_MINUTES;

	/**
	 * The time in minutes before a flag is automatically returned (see
	 * {@link FLAG_CAPTURE_MINUTES}) when a warning of the automatic return is
	 * announced.
	 */
	public int FLAG_WARNING_MINUTES;

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

	/**
	 * If true, floor buff blocks effects are limited to specific teams rather
	 * than affecting all teams equally.
	 * 
	 * For each floor buff block in the configuration, the "friend" flag enables
	 * the effect for the team who placed the block. The "enemy" flag enables it
	 * for any other team. The functionality is implemented by setting the
	 * damage value of the block when it is placed. Only floor buff blocks whose
	 * damage value is not otherwise used (e.g. to set dye colour) are modified
	 * in this way.
	 */
	public boolean TEAM_SPECIFIC_FLOOR_BUFFS;

	/**
	 * The location where the player spawns the first time they join the server,
	 * expected to be in The End, or at least not in the Overworld.
	 */
	public Location FIRST_JOIN_SPAWN_LOCATION;

	/**
	 * The respawn location on subsequent joins when not yet allocated to a
	 * team; expected to be in The End, or at least not in the Overworld.
	 */
	public Location NON_TEAM_RESPAWN_LOCATION;

	/**
	 * If enabled, team block helmets will be periodically reattached. Since
	 * some of the warp signs in the end will probably clear inventory, we'll
	 * need this.
	 */
	public boolean CHECK_HELMET;

	/**
	 * If true, helmets can be crafted. Since just about everybody will be
	 * wearing team blocks on their heads, helmets are a waste of materials.
	 * Defaults to false.
	 */
	public boolean ALLOW_HELMET_CRAFTING;

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

	/**
	 * If true, a scoreboard will be displayed to players, containing each
	 * team's score, player count, and buffs.
	 */
	public boolean SCOREBOARD_ENABLE;

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
		ConfigHelper helper = new ConfigHelper(_plugin.getLogger());

		FLAG_FLAME_TICKS = _plugin.getConfig().getInt("time.flag_flame_ticks", 7);
		FLAG_DROPPED_SECONDS = _plugin.getConfig().getInt("time.flag_dropped_seconds", 300);
		FLAG_CAPTURE_MINUTES = _plugin.getConfig().getInt("time.flag_capture_minutes", 30);
		FLAG_WARNING_MINUTES = _plugin.getConfig().getInt("time.flag_warning_minutes", 10);
		TEAM_BUFF_SECONDS = _plugin.getConfig().getInt("time.team_buff_seconds", 1800);

		CAN_ATTACK_IN_ENEMY_BASE = _plugin.getConfig().getBoolean("protections.can_attack_in_enemy_base", false);
		CAN_EDIT_ENEMY_BASE = _plugin.getConfig().getBoolean("protections.can_edit_enemy_base", false);
		CHECK_HELMET = _plugin.getConfig().getBoolean("misc.check_helmet", true);
		ALLOW_HELMET_CRAFTING = _plugin.getConfig().getBoolean("misc.allow_helmet_crafting", false);

		ConfigurationSection spawn = _plugin.getConfig().getConfigurationSection("spawn");
		FIRST_JOIN_SPAWN_LOCATION = helper.loadLocation(spawn, "first_join");
		NON_TEAM_RESPAWN_LOCATION = helper.loadLocation(spawn, "non_team");

		ConfigurationSection sounds = _plugin.getConfig().getConfigurationSection("sounds");
		FLAG_STEAL_SOUND = helper.loadSound(sounds, "steal", Sound.AMBIENCE_THUNDER, true);
		FLAG_RETURN_SOUND = helper.loadSound(sounds, "return", Sound.ORB_PICKUP, true);
		FLAG_CAPTURE_SOUND = helper.loadSound(sounds, "capture", Sound.LEVEL_UP, true);
		TEAM_BUFF_SOUND = helper.loadSound(sounds, "buff", Sound.WITHER_SPAWN, true);

		SCOREBOARD_ENABLE = _plugin.getConfig().getBoolean("scoreboard.enable", true);

		TEAM_SPECIFIC_FLOOR_BUFFS = _plugin.getConfig().getBoolean("buffs.team_specific_floor_buffs", true);
		_plugin.getTeamManager().load();
		_plugin.getBuffManager().load();
	} // load

	// ------------------------------------------------------------------------
	/**
	 * Save all configuration.
	 */
	public void save() {
		// Move aside the current configuration to a backup.
		File backupsDir = new File(_plugin.getDataFolder(), "backups");
		if (!backupsDir.isDirectory()) {
			backupsDir.mkdirs();
		}

		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");
		File backupFile = new File(backupsDir, "config.yml.backup-" + format.format(new Date()));
		File configFile = new File(_plugin.getDataFolder(), "config.yml");
		configFile.renameTo(backupFile);

		// Save the new configuration.
		ConfigHelper helper = new ConfigHelper(_plugin.getLogger());
		_plugin.getTeamManager().save();
		_plugin.getBuffManager().save();

		_plugin.getConfig().set("time.flag_flame_ticks", FLAG_FLAME_TICKS);
		_plugin.getConfig().set("time.flag_dropped_seconds", FLAG_DROPPED_SECONDS);
		_plugin.getConfig().set("time.flag_capture_minutes", FLAG_CAPTURE_MINUTES);
		_plugin.getConfig().set("time.flag_warning_minutes", FLAG_WARNING_MINUTES);
		_plugin.getConfig().set("time.team_buff_seconds", TEAM_BUFF_SECONDS);

		ConfigurationSection firstJoin = _plugin.getConfig().getConfigurationSection("spawn.first_join");
		ConfigurationSection nonTeam = _plugin.getConfig().getConfigurationSection("spawn.non_team");
		helper.saveLocation(firstJoin, FIRST_JOIN_SPAWN_LOCATION);
		helper.saveLocation(nonTeam, NON_TEAM_RESPAWN_LOCATION);
		_plugin.saveConfig();
	} // save

	// ------------------------------------------------------------------------
	/**
	 * The owning plugin.
	 */
	private CutePVP _plugin;
} // class Configuration