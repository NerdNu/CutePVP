package com.c45y.CutePVP.buff;

import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import com.c45y.CutePVP.Messages;
import com.c45y.CutePVP.Team;
import com.c45y.CutePVP.TeamManager;
import com.c45y.CutePVP.TeamPlayer;
import com.c45y.CutePVP.util.ConfigHelper;
import com.c45y.CutePVP.util.RateLimiter;

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
				_startTicks = section.getLong("start_ticks");
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
			section.set("start_ticks", _startTicks);
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
		// Only announce if minimum broadcast separation has elapsed, or the
		// buff has changed hands, to prevent chat spam.
		if (_team != teamPlayer.getTeam() || _broadcastRateLimiter.canAct(_location.getWorld(), BUFF_BROADCAST_TICKS)) {
			Messages.broadcast(teamPlayer.getPlayer().getDisplayName() + Messages.BROADCAST_COLOR +
								" has claimed the " + _name + " buff for " + teamPlayer.getTeam().getName() + ".");

			// If the buff has changed hands, play a sound.
			if (_team != teamPlayer.getTeam()) {
				_location.getWorld().playSound(_location, Sound.WITHER_SPAWN, 1000.0f, 1);
			}
		}

		_team = teamPlayer.getTeam();
		_startTicks = _location.getWorld().getFullTime();
		for (Player player : _team.getOnlineMembers()) {
			apply(player);
		}
	}

	// ------------------------------------------------------------------------
	/**
	 * Update this buff by reapplying it to all members of the owning team, or
	 * expiring it if durationTicks ticks have elapsed since it was claimed.
	 * 
	 * @param durationTicks the duration in ticks that the buff is sustained
	 *        after it is claimed by a player.
	 */
	public void update(long durationTicks) {
		if (isClaimed()) {
			long worldTime = _location.getWorld().getFullTime();
			if (worldTime < _startTicks + durationTicks) {
				for (Player player : _team.getOnlineMembers()) {
					apply(player);
				}
				if (_broadcastRateLimiter.canAct(_location.getWorld(), BUFF_BROADCAST_TICKS)) {
					Messages.broadcast(_team.getName() + " has the " + _name + " buff.");
				}
			} else {
				Messages.broadcast(_team.getName() + Messages.BROADCAST_COLOR +
									"'s claim on the " + _name + " buff has expired.");
				_team = null;
			}
		}
	}

	// ------------------------------------------------------------------------
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
	 * The world time (ticks) when the buff block was claimed.
	 */
	private long _startTicks;

	/**
	 * Limit the rate at which buff claims are broadcast so players can't spam
	 * chat by right clicking on the buff.
	 */
	private RateLimiter _broadcastRateLimiter = new RateLimiter();

	/**
	 * Minimum duration in ticks between broadcast announcements about who owns
	 * team buffs.
	 */
	private int BUFF_BROADCAST_TICKS = 20 * 60 * 3;
} // class TeamBuff