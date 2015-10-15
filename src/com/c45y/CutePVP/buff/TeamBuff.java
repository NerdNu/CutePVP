package com.c45y.CutePVP.buff;

import com.c45y.CutePVP.*;
import com.c45y.CutePVP.util.ConfigHelper;
import com.c45y.CutePVP.util.RateLimiter;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.logging.Logger;

// ----------------------------------------------------------------------------
/**
 * Potion effects applied to entire teams when one team member clicks on a
 * distinguished block.
 * 
 * Team buff blocks never move and should not be editable by players.
 * 
 * Buff potion effects are not explicitly cleared when the buff expires.
 * Instead, the normal Minecraft potion expiry mechanism is allowed to occur.
 */
public class TeamBuff extends Buff {
	// ------------------------------------------------------------------------
	/**
	 * Constructor.
	 *
	 * @param plugin the CutePVP instance.
	 */
	public TeamBuff(CutePVP plugin) {
		_plugin = plugin;
	}

	// ------------------------------------------------------------------------
	/**
	 * Load this buff from the specified configuration section.
	 * 
	 * @param section the configuration section.
	 * @param logger used to log messages.
	 * @return true if successfully loaded.
	 */
	public boolean load(ConfigurationSection section, TeamManager tm, Logger logger) {
		try {
			ConfigHelper helper = new ConfigHelper(logger);
			if (super.load(section, logger)) {
				_id = section.getName();
				_name = section.getString("name", _id);
				_team = tm.getTeam(section.getString("team", ""));
				_location = helper.loadLocation(section, "location");
				if (_location == null) {
					logger.severe("Unable to load the " + _name + " buff location.");
					return false;
				}
				_startMillis = section.getLong("start_millis");
				return true;
			}
		} catch (Exception ex) {
			logger.severe("Unable to load team buff " + section.getName());
		}
		return false;
	}

	// ------------------------------------------------------------------------
	/**
	 * Save the state of this buff to the configuration.
	 * 
	 * @param section the section whose name is the ID of this buff.
	 * @param logger used to log messages.
	 */
	public void save(ConfigurationSection section, Logger logger) {
		try {
			ConfigHelper helper = new ConfigHelper(logger);
			section.set("team", _team != null ? _team.getId() : "");
			section.set("start_millis", _startMillis);
			ConfigurationSection locationSection = section.createSection("location");
			helper.saveBlockLocation(locationSection, _location);
		} catch (Exception ex) {
			logger.severe("Unable to save team buff " + section.getName());
		}
	}

	// ------------------------------------------------------------------------
	/**
	 * Return the programmatic ID of this team buff.
	 * 
	 * @return the programmatic ID of this team buff.
	 */
	public String getId() {
		return _id;
	}

	// ------------------------------------------------------------------------
	/**
	 * Return the name of this team buff.
	 * 
	 * @return the name of this team buff.
	 */
	public String getName() {
		return _name;
	}

	// --------------------------------------------------------------------------
	/**
	 * Set the location of this team buff.
	 * 
	 * @param location the location of the block.
	 */
	public void setLocation(Location location) {
		_location = location.clone();
	}

	// ------------------------------------------------------------------------
	/**
	 * Return the location of this team buff.
	 * 
	 * @return the location of this team buff.
	 */
	public Location getLocation() {
		return _location;
	}

	// ------------------------------------------------------------------------
	/**
	 * Return the actual Block which, when right clicked, claims the buff for a
	 * whole team.
	 * 
	 * @return the actual Block which, when right clicked, claims the buff for a
	 *         whole team.
	 */
	public Block getBlock() {
		return _location.getBlock();
	}

	// ------------------------------------------------------------------------
	/**
	 * Return the team that has claimed this buff.
	 *
	 * @return the team that has claimed this buff.
	 */
	public Team getTeam() {
		return _team;
	}

	// ------------------------------------------------------------------------
	/**
	 * Return true if this buff has been claimed by a team.
	 * 
	 * @eturn true if this buff has been claimed by a team.
	 */
	public boolean isClaimed() {
		return _team != null;
	}

	// ------------------------------------------------------------------------
	/**
	 * Claim this buff on behalf of the specified player's team.
	 * 
	 * @param teamPlayer the player who claimed the buff.
	 */
	public void claimBy(TeamPlayer teamPlayer) {
		if (_team != teamPlayer.getTeam()) {
			_team = teamPlayer.getTeam();

			// Only score if ownership has changed.
			teamPlayer.getTeam().getScore().buffs.increment();
			teamPlayer.getScore().buffs.increment();

			_plugin.getScoreboardManager().refreshTeamEffects();

			// Prevent chat and audio spam.
			if (_claimRateLimiter.canAct(BUFF_CLAIM_MILLIS)) {
				Messages.broadcast(teamPlayer.getPlayer().getDisplayName() + Messages.BROADCAST_COLOR +
									" has claimed the " + _name + " buff for " + teamPlayer.getTeam().getName() + ".");
				Configuration configuration = teamPlayer.getTeam().getPlugin().getConfiguration();
				if (configuration.TEAM_BUFF_SOUND != null) {
					_location.getWorld().playSound(_location, configuration.TEAM_BUFF_SOUND, Constants.SOUND_RANGE, 1);
				}
			} else {
				Messages.success(teamPlayer.getPlayer(), null,
					"You've reclaimed the buff. An announcement will be broadcast shortly.");
			}
		} else {
			Messages.success(teamPlayer.getPlayer(), null,
				"Your team already owns that buff.");
		}

		// Restart the claim timer.
		_startMillis = System.currentTimeMillis();
		for (Player player : _team.getOnlineMembers()) {
			apply(player);
		}
	} // claimBy

	// ------------------------------------------------------------------------
	/**
	 * Update this buff by reapplying it to all members of the owning team, or
	 * expiring it if the timeout has elapsed since it was claimed.
	 * 
	 * @param durationSeconds the duration in seconds that the buff is sustained
	 *        after it is claimed by a player.
	 */
	public void update(long durationSeconds) {
		if (isClaimed()) {
			long now = System.currentTimeMillis();
			if (now < _startMillis + 1000 * durationSeconds) {
				for (Player player : _team.getOnlineMembers()) {
					// Only apply buffs in the overworld; not The End.
					if (_team.getPlugin().isInMatchArea(player)) {
						apply(player);
					}
				}
				if (_broadcastRateLimiter.canAct(BUFF_BROADCAST_MILLIS)) {
					Messages.broadcast(_team.getName() + " has the " + _name + " buff.");
				}
			} else {
				Messages.broadcast(_team.getName() + Messages.BROADCAST_COLOR +
									"'s claim on the " + _name + " buff has expired.");
				_team = null;
				_plugin.getScoreboardManager().refreshTeamEffects();
			}
		}
	}

	// ------------------------------------------------------------------------
	/**
	 * The owning plugin.
	 */
	private CutePVP _plugin;

	/**
	 * The identifier of this buff in the configuration, used when saving.
	 */
	private String _id;

	/**
	 * The name of this buff, used in broadcast messages.
	 */
	private String _name;

	/**
	 * The team that currently owns this buff.
	 */
	private Team _team;

	/**
	 * The non-null location of the buff block.
	 */
	private Location _location;

	/**
	 * The value of System.currentTimeMillis() when the buff block was claimed.
	 */
	private long _startMillis;

	/**
	 * Limit the rate at which ongoing buff claims are broadcast so that players
	 * can't spam chat and audio by right clicking on the buff.
	 */
	private RateLimiter _broadcastRateLimiter = new RateLimiter();

	/**
	 * Limit the rate at which changes to buff ownership trigger broadcasts and
	 * sounds so that players on opposing teams cannot spam chat or audio by
	 * conspiring to claim the buff alternately.
	 */
	private RateLimiter _claimRateLimiter = new RateLimiter();

	/**
	 * Minimum duration in milliseconds between broadcast announcements about
	 * who owns team buffs.
	 */
	private int BUFF_BROADCAST_MILLIS = 1000 * 60 * 3;

	/**
	 * Minimum duration in milliseconds between sounds playing when a different
	 * team claims team buffs.
	 */
	private int BUFF_CLAIM_MILLIS = 1000 * 60;
} // class TeamBuff