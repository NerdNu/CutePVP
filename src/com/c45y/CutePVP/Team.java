package com.c45y.CutePVP;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.material.MaterialData;

import com.c45y.CutePVP.util.ConfigHelper;
import com.c45y.CutePVP.util.Util;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

// ----------------------------------------------------------------------------
/**
 * Represents one of the opposing teams.
 */
public class Team {
	// ------------------------------------------------------------------------
	/**
	 * Constructor.
	 * 
	 * @param plugin owning plugin.
	 */
	public Team(CutePVP plugin) {
		_plugin = plugin;
	}

	// ------------------------------------------------------------------------
	/**
	 * Load the description of this Team from the configuration.
	 * 
	 * @param teamSection the configuration section for this team under "teams".
	 */
	public void load(ConfigurationSection teamSection) {
		Logger logger = _plugin.getLogger();
		ConfigHelper config = new ConfigHelper(logger);
		_id = teamSection.getName();
		_name = teamSection.getString("name", "");
		_chatColor = ChatColor.valueOf(teamSection.getString("chat_color", "white").toUpperCase());
		try {
			Material material = Material.matchMaterial(teamSection.getString("material", ""));
			byte data = (byte) (teamSection.getInt("data") & 0xF);
			_materialData = new MaterialData(material, data);
		} catch (Exception ex) {
			// Set an unreasonable default. :)
			_materialData = new MaterialData(Material.GRASS);
			logger.severe("Team " + getId() + " has an invalid team block.");
		}

		_spawn = config.loadLocation(teamSection, "spawn");
		_chestRegion = teamSection.getString("chest_region", "");
		_regions = new HashSet<String>();
		if (teamSection.isList("regions")) {
			for (String region : teamSection.getStringList("regions")) {
				_regions.add(region.toLowerCase());
			}
		} else {
			logger.severe("Team " + getId() + " has no WorldGuard regions.");
		}
		_score = new Score();
		ConfigurationSection scoreSection = teamSection.getConfigurationSection("score");
		if (scoreSection != null) {
			_score.load(scoreSection);
		}

		// Load the flags.
		ConfigurationSection flagsSection = teamSection.getConfigurationSection("flags");
		if (flagsSection != null) {
			for (String id : flagsSection.getKeys(false)) {
				ConfigurationSection section = flagsSection.getConfigurationSection(id);
				Flag flag = Flag.load(section, this, _plugin.getLogger());
				if (flag != null) {
					_flags.add(flag);
				}
			}
		}

		// Load the team members.
		ConfigurationSection membersSection = teamSection.getConfigurationSection("members");
		if (membersSection != null) {
			for (String playerName : membersSection.getKeys(false)) {
				OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
				TeamPlayer teamPlayer = _plugin.getTeamManager().createTeamPlayer(player.getName(), this);
				// createTeamPlayer() also calls addMember().
				teamPlayer.getScore().load(membersSection.getConfigurationSection(playerName));
			}
		}

		// Force recomputation of the message highlighting regexp.
		_memberNamesPattern = null;
	} // load

	// --------------------------------------------------------------------------
	/**
	 * Save the team description.
	 * 
	 * @param teamSection the configuration section for this team under "teams".
	 */
	public void save(ConfigurationSection teamSection) {
		ConfigHelper config = new ConfigHelper(_plugin.getLogger());

		// Save the team's spawn.
		ConfigurationSection spawnSection = teamSection.getConfigurationSection("spawn");
		config.saveLocation(spawnSection, _spawn);

		// Save the score.
		ConfigurationSection scoreSection = teamSection.getConfigurationSection("score");
		if (scoreSection == null) {
			scoreSection = teamSection.createSection("score");
		}
		_score.save(scoreSection);

		// Save the flags.
		for (Flag flag : _flags) {
			ConfigurationSection flagSection = teamSection.getConfigurationSection("flags." + flag.getId());
			flag.save(flagSection, _plugin.getLogger());
		}

		// Save the team members.
		ConfigurationSection membersSection = teamSection.createSection("members");
		for (String playerName : _members) {
			TeamPlayer teamPlayer = _plugin.getTeamManager().getTeamPlayer(playerName);
			ConfigurationSection playerSection = membersSection.createSection(playerName);
			teamPlayer.getScore().save(playerSection);
		}
	} // save

	// ------------------------------------------------------------------------
	/**
	 * Return a reference to the owning plugin.
	 * 
	 * @return a reference to the owning plugin.
	 */
	public CutePVP getPlugin() {
		return _plugin;
	}

	// ------------------------------------------------------------------------
	/**
	 * Return the unique programmatic identifier of the Team.
	 * 
	 * @return the unique programmatic identifier of the Team.
	 */
	public String getId() {
		return _id;
	}

	// ------------------------------------------------------------------------
	/**
	 * Return the descriptive (presentation) name of the Team, used in chat
	 * messages.
	 * 
	 * @return the descriptive (presentation) name of the Team, used in chat
	 *         messages.
	 */
	public String getName() {
		return _name;
	}

	// ------------------------------------------------------------------------
	/**
	 * Return the ChatColor used to signify the team.
	 * 
	 * @return the ChatColor used to signify the team.
	 */
	public ChatColor getTeamChatColor() {
		return _chatColor;
	}

	// ------------------------------------------------------------------------
	/**
	 * Return the data value that is set on floor buff blocks when placed to
	 * mark them as owned by this team.
	 * 
	 * Currently this is derived from the data/color value of the wool helmets
	 * worn on players' heads, but the code could be adapted to get the value
	 * from another configuration setting if necessary.
	 * 
	 * @return the data/damage value of placed floor buff blocks.
	 */
	public byte getData() {
		return getMaterialData().getData();
	}

	// ------------------------------------------------------------------------
	/**
	 * Return the MaterialData of the team's block.
	 * 
	 * The team block is the type of block that players wear on their heads as a
	 * helmet. It also imparts negative potion effects to enemies who walk on
	 * it.
	 * 
	 * @return the MaterialData of the team's block.
	 */
	public MaterialData getMaterialData() {
		return _materialData;
	}

	// ------------------------------------------------------------------------
	/**
	 * Return an item stack containing a single team block.
	 * 
	 * @return an item stack containing a single team block.
	 */
	public ItemStack getTeamItemStack() {
		return _materialData.toItemStack(1);
	}

	// ------------------------------------------------------------------------
	/**
	 * Return true if the specified block's type and data matches the team
	 * block.
	 * 
	 * @return true if the specified block's type and data matches the team
	 *         block.
	 */
	public boolean isTeamBlock(Block block) {
		return block.getType() == _materialData.getItemType() &&
				block.getData() == _materialData.getData();
	}

	// ------------------------------------------------------------------------
	/**
	 * Return true if the block is the team block or carpet in the team's color.
	 * 
	 * @return true if the block is the team block or carpet in the team's
	 *         color.
	 */
	public boolean isTeamFloor(Block block) {
		return (block.getType() == _materialData.getItemType() || block.getType() == Material.CARPET) &&
				block.getData() == _materialData.getData();
	}

	// ------------------------------------------------------------------------
	/**
	 * Encode a chat message in the team's color.
	 * 
	 * Text after the message is reset back to white.
	 * 
	 * @param message the message.
	 */
	public String encodeTeamColor(String message) {
		return getTeamChatColor() + message + ChatColor.WHITE;
	}

	// ------------------------------------------------------------------------
	/**
	 * Highlight the names of all team members mentioned in the message using
	 * the team color, whether they are online or not.
	 * 
	 * Player names within words are not highlighted because they might refer to
	 * a different player. For example, if a player named Fred is on the team,
	 * then "hi fred" would be highlighted, but "hi alfred" would not.
	 * 
	 * Non-highlighted text is set to the same color as the end of the input
	 * message, defaulting to white if the message contains no color codes.
	 * 
	 * @param message the message text to highlight.
	 * @return the new message text with embedded highlight codes.
	 */
	public String highlightAllMembers(String message) {
		if (_memberNamesPattern == null) {
			// The regexp takes the form of a single group containing all
			// member names as alternatives, e.g. "\b(totemo|Notch)\b".
			StringBuilder pattern = new StringBuilder();
			pattern.append("\\b(");
			boolean first = true;
			for (String member : getMembers()) {
				if (first) {
					first = false;
				} else {
					pattern.append('|');
				}
				pattern.append(member);
			}
			pattern.append(")\\b");
			_memberNamesPattern = Pattern.compile(pattern.toString(), Pattern.CASE_INSENSITIVE);
		}

		// Work out the default color of the non-matching parts of the message.
		String resetColor = ChatColor.getLastColors(message);
		if (resetColor.length() == 0) {
			resetColor = ChatColor.WHITE.toString();
		}

		return _memberNamesPattern.matcher(message).replaceAll(_chatColor + "$1" + resetColor);
	}

	// ------------------------------------------------------------------------
	/**
	 * Set the attributes of the player to those of this team; specifically:
	 * 
	 * <ul>
	 * <li>The player's helmet is set to the team block.</li>
	 * <li>The player's display name is set in the team color.</li>
	 * </ul>
	 * 
	 * Note:
	 * "getPlayer getPlayerExact returns null when called during respawn event"
	 * https://bukkit.atlassian.net/browse/BUKKIT-4561
	 * 
	 * @param player the affected player.
	 */
	public void setTeamAttributes(Player player) {
		// Helmet setting:
		// Occasionally, this had NullPointerExceptions when players were being
		// referenced by OfflinePlayer. Extra logging has been added to try to
		// diagnose.
		Logger logger = getPlugin().getLogger();
		if (player == null) {
			logger.severe("Player was unexpectedly null in setTeamAttributes().");
		} else {
			player.setDisplayName(encodeTeamColor(player.getName()));

			PlayerInventory inventory = player.getInventory();
			if (inventory == null) {
				logger.severe("Player " + player.getName() + "'s inventory was unexpectedly null in setTeamAttributes().");
			} else {
				inventory.setHelmet(getTeamItemStack());
			}
		}
	}

	// ------------------------------------------------------------------------
	/**
	 * Return a reference to the list of flags.
	 * 
	 * @return a reference to the list of flags.
	 */
	public ArrayList<Flag> getFlags() {
		return _flags;
	}

	// ------------------------------------------------------------------------
	/**
	 * Return the flag with the specified ID, or null if none matches.
	 * 
	 * @param flagId the programmatic ID of the flag.
	 * @return the flag with the specified ID, or null if none matches.
	 */
	public Flag getFlag(String flagId) {
		for (Flag flag : _flags) {
			if (flag.getId().equals(flagId)) {
				return flag;
			}
		}
		return null;
	}

	// ------------------------------------------------------------------------
	/**
	 * Return the flag at the specified, non-null block (if dropped or home), or
	 * null if the block is not a flag.
	 * 
	 * @param block the block to check.
	 * @return the flag at the specified, non-null block (if dropped or home),
	 *         or null if the block is not a flag.
	 */
	public Flag getFlagFromBlock(Block block) {
		Location loc = block.getLocation();
		for (Flag flag : _flags) {
			if (Util.isSameBlock(loc, flag.getLocation())) {
				return flag;
			}
		}
		return null;
	}

	// ------------------------------------------------------------------------
	/**
	 * Return true if the specified location is the home location of a Flag.
	 * 
	 * @param location the location.
	 * @return true if the specified location is the home location of a Flag.
	 */
	public boolean isFlagHomeLocation(Location location) {
		for (Flag flag : _flags) {
			if (Util.isSameBlock(flag.getHomeLocation(), location)) {
				return true;
			}
		}
		return false;
	}

	// ------------------------------------------------------------------------
	/**
	 * Return the mutable total score of the team as a whole.
	 * 
	 * @return the mutable total score of the team as a whole.
	 */
	public Score getScore() {
		return _score;
	}

	// ------------------------------------------------------------------------
	/**
	 * Return the team spawn point.
	 * 
	 * @return the team spawn point.
	 */
	public Location getSpawn() {
		return _spawn;
	}

	// ------------------------------------------------------------------------
	/**
	 * Set the team spawn point.
	 * 
	 * @param location the location.
	 */
	public void setSpawn(Location location) {
		_spawn = location.clone();
		FileConfiguration config = _plugin.getConfig();
		ConfigHelper helper = new ConfigHelper(_plugin.getLogger());
		ConfigurationSection section = config.createSection("teams." + getId() + ".spawn");
		helper.saveLocation(section, _spawn);
		_plugin.saveConfig();
	}

	// ------------------------------------------------------------------------
	/**
	 * Return true if the specified Location is in the team's base region(s).
	 * 
	 * @param location the location.
	 * @return true if the specified Location is in the team's base region(s).
	 */
	public boolean inTeamBase(Location location) {
		RegionManager mgr = _plugin.getWorldGuard().getGlobalRegionManager().get(location.getWorld());
		for (ProtectedRegion region : mgr.getApplicableRegions(location)) {
			if (_regions.contains(region.getId().toLowerCase())) {
				return true;
			}
		}
		return false;
	}

	// ------------------------------------------------------------------------
	/**
	 * Send the message to all online team members.
	 * 
	 * @param message the full text of the message.
	 */
	public void message(String message) {
		for (Player player : getOnlineMembers()) {
			// Don't send team messages to staff here, since they will get a
			// copy of them anyway.
			if (!player.hasPermission(Permissions.MOD)) {
				player.sendMessage(message);
			}
		}
	}

	// ------------------------------------------------------------------------
	/**
	 * Point team member compasses to point to the nearest stolen flag.
	 * 
	 * Only do this in the overworld.
	 */
	public void updateCompasses() {
		for (Player player : getOnlineMembers()) {
			if (_plugin.isInMatchArea(player)) {
				player.setCompassTarget(getNearestFlag(player).getLocation());
			}
		}
	}

	// ------------------------------------------------------------------------
	/**
	 * Return true if the specified player is a member of a team.
	 */
	public boolean hasMember(OfflinePlayer offlinePlayer) {
		return _members.contains(offlinePlayer);
	}

	// ------------------------------------------------------------------------
	/**
	 * Return the set of all team member names, whether online or not.
	 * 
	 * @return the set of all team member names, whether online or not.
	 */
	public HashSet<String> getMembers() {
		return _members;
	}

	// ------------------------------------------------------------------------
	/**
	 * Return the set of all online team members.
	 * 
	 * @return the set of all online team members.
	 */
	public HashSet<Player> getOnlineMembers() {
		HashSet<Player> online = new HashSet<Player>();
		for (String playerName : getMembers()) {
			Player player = Bukkit.getPlayerExact(playerName);
			if (player != null) {
				online.add(player);
			}
		}
		return online;
	}

	// ------------------------------------------------------------------------
	/**
	 * Return the names of all online team members in case-insensitive sorted
	 * order as a string, with each player name preceded by a space.
	 * 
	 * @return the names of all online team members in case-insensitive sorted
	 *         order as a string.
	 */
	public String getOnlineList() {
		TreeSet<String> onlineNames = new TreeSet<String>();
		for (Player player : getOnlineMembers()) {
			onlineNames.add(player.getName());
		}

		StringBuilder list = new StringBuilder();
		for (String playerName : onlineNames) {
			list.append(' ');
			list.append(playerName);
		}
		return list.toString();
	}

	// ------------------------------------------------------------------------
	/**
	 * Add the specified player as a member of the team.
	 * 
	 * Called by the {@link TeamManager} only.
	 * 
	 * @param offlinePlayer the player.
	 */
	void addMember(String playerName) {
		_members.add(playerName);

		// Add the player to the team's "chest region" used as a group to
		// protect chests.
		World overworld = Bukkit.getWorlds().get(0);
		RegionManager mgr = _plugin.getWorldGuard().getGlobalRegionManager().get(overworld);
		ProtectedRegion region = mgr.getRegionExact(_chestRegion);
		if (region != null) {
			region.getMembers().addPlayer(playerName);
		}

		// Force recomputation of the message highlighting regexp.
		_memberNamesPattern = null;
	}

	// ------------------------------------------------------------------------
	/**
	 * Return the nearest stolen flag, or the nearest flag at home if none is
	 * stolen.
	 * 
	 * @param player the player whose location is compared to the flag location.
	 * @return the nearest stolen flag, or the nearest flag at home if none is
	 *         stolen.
	 */
	protected Flag getNearestFlag(Player player) {
		Location playerLoc = player.getLocation();

		// Find nearest stolen/dropped flag location first.
		double minDistSq = Double.MAX_VALUE;
		Flag nearest = null;
		for (Flag flag : _flags) {
			Location flagLoc = flag.getLocation();
			double distSq = flagLoc.distanceSquared(playerLoc);
			if (!flag.isHome() && distSq < minDistSq) {
				minDistSq = distSq;
				nearest = flag;
			}
		}

		// All the flags are home. Return nearest location.
		if (nearest == null) {
			for (Flag flag : _flags) {
				Location flagLoc = flag.getLocation();
				double distSq = flagLoc.distanceSquared(playerLoc);
				if (distSq < minDistSq) {
					minDistSq = distSq;
					nearest = flag;
				}
			}
		}
		return nearest;
	} // getNearestFlag

	// ------------------------------------------------------------------------
	/**
	 * The owning plugin.
	 */
	private CutePVP _plugin;

	/**
	 * Unique programmatic identifier of the Team.
	 */
	private String _id;

	/**
	 * Presentation name of the team in chat messages.
	 */
	private String _name;

	/**
	 * Color of chat messages.
	 */
	private ChatColor _chatColor;

	/**
	 * The Material and data value of the team's block.
	 */
	private MaterialData _materialData;

	/**
	 * Spawn location.
	 */
	private Location _spawn;

	/**
	 * The overall score of the team.
	 */
	private Score _score;

	/**
	 * The name of the protection region used as a predefined protection group
	 * for the team's chests.
	 */
	private String _chestRegion;

	/**
	 * Set of WorldGuard region names that are part of the base, all converted
	 * to lower case.
	 * 
	 * These regions should have the "build" flag set to "allow" regions. This
	 * plugin will restrict edits based on team membership.
	 */
	private HashSet<String> _regions = new HashSet<String>();

	/**
	 * The flags that this Team defends.
	 */
	private ArrayList<Flag> _flags = new ArrayList<Flag>();

	/**
	 * The set of names of players that are members of this team.
	 */
	private HashSet<String> _members = new HashSet<String>();

	/**
	 * A regexp that matches the name of any member of the team (whether online
	 * or not). Lazily updated when used in highlightAllMembers().
	 */
	private Pattern _memberNamesPattern;
} // class Team
