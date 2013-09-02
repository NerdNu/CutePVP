package com.c45y.CutePVP;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import com.c45y.CutePVP.util.WeightedSelection;

// ----------------------------------------------------------------------------
/**
 * Manages the allocation of players to teams.
 */
public class TeamManager implements Iterable<Team> {
	// ------------------------------------------------------------------------
	/**
	 * Constructor.
	 * 
	 * @param plugin the owning plugin.
	 */
	public TeamManager(CutePVP plugin) {
		_plugin = plugin;
	}

	// ------------------------------------------------------------------------
	/**
	 * Load the Teams and their membership from the configuration.
	 */
	public void load() {
		_teams.clear();
		_players.clear();

		// Load the teams.
		ConfigurationSection teams = _plugin.getConfig().getConfigurationSection("teams");
		for (String teamId : teams.getKeys(false)) {
			ConfigurationSection teamSection = teams.getConfigurationSection(teamId);
			Team team = new Team(_plugin);
			team.load(teamSection);
			_teams.put(teamId, team);
		}
	} // load

	// ------------------------------------------------------------------------
	/**
	 * Save the Teams and their membership to the configuration.
	 */
	public void save() {
		// Save the teams. This is modification of a pre-existing section.
		ConfigurationSection teams = _plugin.getConfig().getConfigurationSection("teams");
		for (String teamId : _teams.keySet()) {
			ConfigurationSection teamSection = teams.getConfigurationSection(teamId);
			Team team = getTeam(teamId);
			team.save(teamSection);
		}
	}

	// ------------------------------------------------------------------------
	/**
	 * Return an iterator over all Teams.
	 * 
	 * @return an iterator over all Teams.
	 */
	public Iterator<Team> iterator() {
		return _teams.values().iterator();
	}

	// ------------------------------------------------------------------------
	/**
	 * Return the Team with the specified ID.
	 * 
	 * @return the Team with the specified ID.
	 */
	public Team getTeam(String id) {
		return _teams.get(id);
	}

	// ------------------------------------------------------------------------
	/**
	 * Return the Team whose team block matches the material and data value of
	 * the specified Block, or null if no team matches.
	 * 
	 * @param block the Block.
	 * @return the Team whose team block matches the material and data value of
	 *         the specified Block, or null if no team matches.
	 */
	public Team getTeamFromBlock(Block block) {
		for (Team team : _teams.values()) {
			if (team.isTeamBlock(block)) {
				return team;
			}
		}
		return null;
	}

	// ------------------------------------------------------------------------
	/**
	 * Return the {@link TeamPlayer} representing the specified OfflinePlayer,
	 * or create a new instance if it does not exist.
	 * 
	 * @param offlinePlayer the player, whether online or offline.
	 * @param team the player's {@link Team}.
	 * @return the {@link TeamPlayer} representing the specified OfflinePlayer,
	 *         or create a new instance if it does not exist.
	 */
	public TeamPlayer createTeamPlayer(String playerName, Team team) {
		TeamPlayer teamPlayer = getTeamPlayer(playerName);
		if (teamPlayer == null) {
			teamPlayer = new TeamPlayer(playerName, team);
			_players.put(playerName, teamPlayer);
			team.addMember(playerName);
		}
		return teamPlayer;
	}

	// ------------------------------------------------------------------------
	/**
	 * Return the {@link TeamPlayer} corresponding to the specified Player, or
	 * null if the Player has no assigned {@link Team}.
	 * 
	 * @param player the name of the Player.
	 * @return the {@link TeamPlayer} corresponding to the specified Player, or
	 *         null if the Player has no assigned {@link Team}.
	 */
	public TeamPlayer getTeamPlayer(Player player) {
		return player != null ? _players.get(player.getName()) : null;
	}

	// ------------------------------------------------------------------------
	/**
	 * Return the {@link TeamPlayer} with the specified name, or null if the
	 * player has no assigned {@link Team}.
	 * 
	 * @param plaerName the name of the Player.
	 * @return the {@link TeamPlayer} with the specified name, or null if the
	 *         player has no assigned {@link Team}.
	 */
	public TeamPlayer getTeamPlayer(String playerName) {
		return _players.get(playerName);
	}

	// ------------------------------------------------------------------------
	/**
	 * Return true if the player is exempt from allocation to a team.
	 * 
	 * Staff (admins and moderators in ModMode) inherit this permission via the
	 * CutePVP.admin and CutePVP.mod permissions, respectively.
	 * 
	 * @return true if the player is exempt from allocation to a team.
	 */
	public boolean isExempted(Player player) {
		return player.hasPermission(Permissions.EXEMPT);
	}

	// ------------------------------------------------------------------------
	/**
	 * Allocate non-exempted players to a team.
	 * 
	 * @param player the joining player.
	 * @return the {@link TeamPlayer} instance representing the player, or null
	 *         if not assigned to a {@link Team}.
	 */
	public TeamPlayer assignTeam(Player player) {
		Team team = decideTeam(player);
		if (team != null) {
			TeamPlayer teamPlayer = createTeamPlayer(player.getName(), team);
			team.setTeamAttributes(player);
			player.teleport(team.getSpawn());
			player.sendMessage(team.getTeamChatColor() + "Welcome to " + team.getName() + "!");
			_plugin.getLogger().info(player.getName() + " was assigned to " + team.getName() + ".");
			return teamPlayer;
		}
		return null;
	}

	// ------------------------------------------------------------------------
	/**
	 * Return true if the Location is in an enemy team's base.
	 * 
	 * @param player the Player performing an action.
	 * @param location the Location.
	 * @return true if the Location is in an enemy team's base.
	 */
	public boolean inEnemyTeamBase(Player player, Location location) {
		TeamPlayer teamPlayer = getTeamPlayer(player);
		if (teamPlayer != null) {
			for (Team otherTeam : _teams.values()) {
				if (teamPlayer.getTeam() != otherTeam && otherTeam.inTeamBase(location)) {
					return true;
				}
			}
		}
		return false;
	}

	// ------------------------------------------------------------------------
	/**
	 * Return true if the player is in his own Team's base.
	 * 
	 * @param player the Player.
	 * @return true if the player is in his own Team's base.
	 */
	public boolean inOwnTeamBase(Player player) {
		TeamPlayer teamPlayer = getTeamPlayer(player);
		return teamPlayer != null && teamPlayer.getTeam().inTeamBase(player.getLocation());
	}

	// ------------------------------------------------------------------------
	/**
	 * Send a list of all members of each team to the player.
	 * 
	 * @param player the player issuing the list command.
	 */
	public void sendTeamLists(Player player) {
		for (Team team : _teams.values()) {
			StringBuilder message = new StringBuilder();
			message.append(team.encodeTeamColor(team.getName()));
			message.append(" [").append(team.getOnlineMembers().size());
			message.append(" of ").append(team.getMembers().size()).append("]: ");
			message.append(team.getOnlineList());
			player.sendMessage(message.toString());
		}

		// Return staff list in sorted order.
		ArrayList<Player> onlineStaff = getOnlineStaff();
		Player[] sortedStaff = new Player[onlineStaff.size()];
		Arrays.sort(onlineStaff.toArray(sortedStaff), new Comparator<Player>() {
			@Override
			public int compare(Player a, Player b) {
				return a.getName().compareTo(b.getName());
			}
		});
		StringBuilder staffMessage = new StringBuilder();
		staffMessage.append("Staff: ");
		for (Player staff : sortedStaff) {
			staffMessage.append(' ').append(staff.getName());
		}
		player.sendMessage(staffMessage.toString());
	}

	// ------------------------------------------------------------------------
	/**
	 * Handle staff (non-participants) joining the server by adding the player
	 * to the set of online staff.
	 * 
	 * @param player the staff member who joined.
	 */
	public void onStaffJoin(Player player) {
		Messages.success(player, Messages.PREFIX, "Welcome, staff member.");
	}

	// ------------------------------------------------------------------------
	/**
	 * Return the online staff.
	 * 
	 * This is computed dynamically to account for moderators joining and
	 * leaving staff using /modmode.
	 * 
	 * @return the online staff.
	 */
	public ArrayList<Player> getOnlineStaff() {
		ArrayList<Player> staff = new ArrayList<Player>();
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (player.hasPermission(Permissions.MOD)) {
				staff.add(player);
			}
		}
		return staff;
	}

	// ------------------------------------------------------------------------
	/**
	 * Return the Team to which the player should be assigned.
	 * 
	 * Consider both the total number of players in a team and the number
	 * currently online when assigning selection probabilities.
	 * 
	 * @param player the player to add.
	 * @return the Team to which the player should be assigned.
	 */
	protected Team decideTeam(Player player) {
		if (isExempted(player)) {
			return null;
		}
		HashMap<Team, Integer> weights = new HashMap<Team, Integer>();
		int totalWeight = 0;
		for (Team team : _teams.values()) {
			// Yes, I know that getMembers() includes the online ones. :)
			// The intent is that neither figure should dominate the decision.
			int teamWeight = team.getOnlineMembers().size() + team.getMembers().size();
			totalWeight += teamWeight;
			weights.put(team, teamWeight);
		}

		WeightedSelection<Team> chooser = new WeightedSelection<Team>();
		if (totalWeight == 0) {
			// Selection weights are 0 when there are no players.
			// Assign equal weights to all teams.
			for (Team team : _teams.values()) {
				chooser.addChoice(team, 1.0);
			}
		} else {
			for (Team team : weights.keySet()) {
				chooser.addChoice(team, totalWeight - weights.get(team));
			}
		}
		return chooser.choose();
	}

	// ------------------------------------------------------------------------
	/**
	 * The owning plugin.
	 */
	private CutePVP _plugin;

	/**
	 * Map from Team.getId() to Team instance.
	 */
	private LinkedHashMap<String, Team> _teams = new LinkedHashMap<String, Team>();

	/**
	 * Map from Player name to corresponding {@link TeamPlayer}.
	 */
	private HashMap<String, TeamPlayer> _players = new HashMap<String, TeamPlayer>();

} // class TeamManager
