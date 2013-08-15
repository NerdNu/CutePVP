package com.c45y.CutePVP.buff;

import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import com.c45y.CutePVP.Messages;
import com.c45y.CutePVP.Team;
import com.c45y.CutePVP.TeamManager;
import com.c45y.CutePVP.TeamPlayer;
import com.c45y.CutePVP.util.ConfigHelper;

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
			helper.saveBlockLocation(section, _location);
			section.set("start_ticks", _startTicks);
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
		_team = teamPlayer.getTeam();
		_startTicks = _location.getWorld().getFullTime();
		Messages.broadcast(teamPlayer.getPlayer().getDisplayName() +
							" has claimed the " + _name + " buff for " + _team.getName() + ".");
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
			if (_startTicks + durationTicks < _location.getWorld().getFullTime()) {
				Messages.broadcast(_team.getName() + " has the " + _name + " buff.");
				for (Player player : _team.getOnlineMembers()) {
					apply(player);
				}
			} else {
				Messages.broadcast(_team.getName() + "'s claim on the " + _name + " buff has expired.");
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
	 * The full time when the buff block was claimed.
	 */
	private long _startTicks;
} // class TeamBuff