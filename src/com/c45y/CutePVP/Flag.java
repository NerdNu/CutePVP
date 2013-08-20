package com.c45y.CutePVP;

import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.material.MaterialData;

import com.c45y.CutePVP.util.ConfigHelper;
import com.c45y.CutePVP.util.Util;

// ----------------------------------------------------------------------------
/**
 * Represents one of possibly many flags belonging to a team, whether at home or
 * carried by an opposition player.
 */
public class Flag {
	// ------------------------------------------------------------------------
	/**
	 * Load this flag from a subsection of the team's "flags" configuration
	 * section.
	 * 
	 * @param section the section under "flags" describing this flag. The name
	 *        of this section is the flag's ID.
	 * @param team the Team guarding this flag.
	 * @param logger used to log errors.
	 * @return the new Flag, or null on error.
	 */
	public static Flag load(ConfigurationSection section, Team team, Logger logger) {
		ConfigHelper config = new ConfigHelper(logger);
		try {
			Flag flag = new Flag(team);
			flag._id = section.getName();
			flag._homeLocation = config.loadLocation(section, "home");
			flag._dropLocation = (section.contains("current")) ? config.loadLocation(section, "current") : flag._homeLocation.clone();
			flag._name = section.getString("description", team.getName() + "'s flag");
			flag._dropTime = section.getLong("drop_time");
			return flag;
		} catch (Exception ex) {
			logger.severe("Error loading " + team.getId() + "'s flag " + section.getName());
		}
		return null;
	}

	// ------------------------------------------------------------------------
	/**
	 * Save the current and home locations of this flag.
	 * 
	 * @param section the configuration section of the flag.
	 * @param logger logs messages.
	 */
	public void save(ConfigurationSection section, Logger logger) {
		ConfigHelper helper = new ConfigHelper(logger);
		ConfigurationSection currentSection = section.getConfigurationSection("current");
		ConfigurationSection homeSection = section.getConfigurationSection("home");
		helper.saveBlockLocation(currentSection, _dropLocation);
		helper.saveBlockLocation(homeSection, _homeLocation);
		section.set("drop_time", _dropTime);
	}

	// ------------------------------------------------------------------------
	/**
	 * Signify that the flag has been stolen by a player from the specified
	 * team.
	 * 
	 * @param player the player touching the flag.
	 * @param team that player's team.
	 */
	public void stealBy(TeamPlayer teamPlayer) {
		if (!isCarried() && teamPlayer.getTeam() != _team) {
			getLocation().getBlock().setType(Material.AIR);
			_carrier = teamPlayer;
			_carrier.setCarriedFlag(this);
		}
	}

	// ------------------------------------------------------------------------
	/**
	 * Drop the flag on the ground.
	 * 
	 * The block above the ground is set to the flag material. This method is
	 * called when the flag carrier is killed or logs out, including restarts.
	 */
	public void drop() {
		if (isCarried()) {
			// Try dropping the flag at head height to avoid being trapped under
			// it.
			_dropLocation = _carrier.getPlayer().getLocation().getBlock().getRelative(BlockFace.UP).getLocation();
			_dropTime = _dropLocation.getWorld().getFullTime();

			_team.getPlugin().getLogger().info(
				_carrier.getPlayer().getName() + " dropped " + _team.getName() + "'s " +
				getName() + " flag at " + Messages.formatIntegerXYZ(_dropLocation) +
				" at time " + _dropTime + ".");

			MaterialData teamBlock = getTeam().getMaterialData();
			_dropLocation.getBlock().setTypeIdAndData(teamBlock.getItemTypeId(), teamBlock.getData(), false);
			_carrier.setCarriedFlag(null);
			_carrier = null;
		}
	}

	// ------------------------------------------------------------------------
	/**
	 * Return true if the flag was automatically returned after lying on the
	 * ground.
	 * 
	 * @param timeoutTicks the time in ticks that the flag can lie on the ground
	 *        before being automatically returned.
	 * @return true if the flag was returned.
	 */
	public boolean checkReturn(long timeoutTicks) {
		long worldTime = _dropLocation.getWorld().getFullTime();
		if (isDropped() && worldTime >= (_dropTime + timeoutTicks)) {
			Messages.broadcast(_team.getName() + "'s " + getName() + " flag returned automatically.");
			_team.getPlugin().getLogger().info(_team.getName() + "'s " + getName() +
												" flag returned automatically at time " + worldTime);
			doReturn();
			return true;
		} else {
			return false;
		}
	}

	// ------------------------------------------------------------------------
	/**
	 * Put a flag back in its home location, irrespective of whether it is
	 * carried, dropped or already home.
	 */
	public void doReturn() {
		boolean restoreFlagBlock = false;
		if (isDropped()) {
			_dropLocation.getBlock().setType(Material.AIR);
			restoreFlagBlock = true;
		} else if (isCarried()) {
			_carrier.setCarriedFlag(null);
			_carrier = null;
			restoreFlagBlock = true;
		}

		if (restoreFlagBlock) {
			MaterialData teamBlock = getTeam().getMaterialData();
			_homeLocation.getBlock().setTypeIdAndData(teamBlock.getItemTypeId(), teamBlock.getData(), false);
			_dropLocation = _homeLocation.clone();
		}
	}

	// ------------------------------------------------------------------------
	/**
	 * Return the team that guards this flag.
	 * 
	 * @return the team that guards this flag.
	 */
	public Team getTeam() {
		return _team;
	}

	// ------------------------------------------------------------------------
	/**
	 * Return the programmatic identifier of this flag.
	 * 
	 * @return the programmatic identifier of this flag.
	 */
	public Object getId() {
		return _id;
	}

	// ------------------------------------------------------------------------
	/**
	 * Return the name of this flag used in messages.
	 * 
	 * @return the name of this flag used in messages.
	 */
	public String getName() {
		return _name;
	}

	// ------------------------------------------------------------------------
	/**
	 * Return true if the flag is at its home location.
	 * 
	 * @return true if the flag is at its home location.
	 */
	public boolean isHome() {
		return !isCarried() && Util.isSameBlock(_dropLocation, _homeLocation);
	}

	// ------------------------------------------------------------------------
	/**
	 * Return true if the flag is not at home and not being carried.
	 * 
	 * @return true if the flag is not at home and not being carried.
	 */
	public boolean isDropped() {
		return !isCarried() && !Util.isSameBlock(_dropLocation, _homeLocation);
	}

	// ------------------------------------------------------------------------
	/**
	 * Return true if the flag is currently being carried.
	 * 
	 * @return true if the flag is currently being carried.
	 */
	public boolean isCarried() {
		return getCarrier() != null;
	}

	// ------------------------------------------------------------------------
	/**
	 * Return the opposition player who is carrying this flag, or null if the
	 * flag is not being carried.
	 * 
	 * @return the opposition player who is carrying this flag, or null if the
	 *         flag is not being carried.
	 */
	public TeamPlayer getCarrier() {
		return _carrier;
	}

	// ------------------------------------------------------------------------
	/**
	 * Return the current location of the flag.
	 * 
	 * If the flag is currently being carried, it will be the location of the
	 * carrier.
	 * 
	 * @return the current location of the flag.
	 */
	public Location getLocation() {
		return isCarried() ? getCarrier().getPlayer().getLocation() : _dropLocation;
	}

	// --------------------------------------------------------------------------
	/**
	 * Set the home location of this flag.
	 * 
	 * The current carrier is cleared and the current location is set to the
	 * home location too.
	 * 
	 * @param location the new home location of the flag.
	 */
	public void setHomeLocation(Location location) {
		_homeLocation = _dropLocation = location.clone();
		_carrier = null;
		_dropTime = 0;
	}

	// ------------------------------------------------------------------------
	/**
	 * Return the starting location of the flag in its owning team's base.
	 * 
	 * @return the starting location of the flag in its owning team's base.
	 */
	public Location getHomeLocation() {
		return _homeLocation;
	}

	// ------------------------------------------------------------------------
	/**
	 * Return the Block at the current flag location, or null if the flag is
	 * carried by a player.
	 * 
	 * Blocks can be compared by identity (reference comparison) and therefore
	 * are a fast and accurate way to determine where a flag is.
	 * 
	 * @return the Block at the current flag location, or null if the flag is
	 *         carried by a player.
	 */
	public Block getBlock() {
		return isCarried() ? null : _dropLocation.getBlock();
	}

	// ------------------------------------------------------------------------
	/**
	 * Constructor.
	 * 
	 * @param team the team guarding this flag.
	 */
	protected Flag(Team team) {
		_team = team;
	}

	// ------------------------------------------------------------------------
	/**
	 * The Team that is guarding this flag.
	 */
	private Team _team;

	/**
	 * The unique identifier used as the section name for this flag in the
	 * configuration file.
	 */
	private String _id;

	/**
	 * The name of this flag used in messages.
	 */
	private String _name;

	/**
	 * The opposition team player who is currently carrying this flag, or null
	 * if the flag is not being carried. The flag is dropped whenever the player
	 * logs out, including during a restart.
	 */
	private TeamPlayer _carrier;

	/**
	 * Current location of the flag when dropped on the ground.
	 */
	private Location _dropLocation;

	/**
	 * Starting location of the flag in its owning team's base.
	 */
	private Location _homeLocation;

	/**
	 * The value of World.getFullTime() when the flag was dropped.
	 */
	private long _dropTime;
} // class Flag