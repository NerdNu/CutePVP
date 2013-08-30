package com.c45y.CutePVP;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

// ----------------------------------------------------------------------------
/**
 * Represents the association between a player (whether online or not), his
 * {@link Team}, the {@link Flag} he is carrying and his personal Score.
 * 
 * Only Players assigned to a team have corresponding TeamPlayer instances.
 * {@link TeamManager} manages the mapping from {@link OfflinePlayer} to
 * {@link TeamPlayer}.
 * 
 * Notes on getting the corresponding Player from TeamPlayer:
 * <ul>
 * <li>setHelmet() occasionally fails with a NullPointerException (after deaths,
 * relogs etc). I assumed OfflinePlayer.getPlayer() was the culprit, but perhaps
 * it's actually Player.getInventory().</li>
 * <li>CraftOfflinePlayer.getPlayer() does an expensive linear search through
 * the entire online player list of the server every time it is called.</li>
 * <li>CraftServer.getPlayerExact() first generates a temporary array of online
 * players, then does the expensive linear search (case insenstive string
 * comparisons on player names) through that.</li>
 * </ul>
 * 
 * There is a very slight efficiency benefit from using OfflinePlayer but at the
 * cost of using a method that sometimes fails for unexplained reasons. Probably
 * best just to use Server.getPlayerExect() and hope that it will be improved.
 */
public class TeamPlayer {
	// ------------------------------------------------------------------------
	/**
	 * Constructor.
	 * 
	 * @param playerName the Player's name.
	 * @param team the Team to which the player belongs.
	 */
	public TeamPlayer(String playerName, Team team) {
		_playerName = playerName;
		_team = team;
	}

	// ------------------------------------------------------------------------
	/**
	 * Return the corresponding OfflinePlayer.
	 * 
	 * @return the corresponding OfflinePlayer.
	 */
	public OfflinePlayer getOfflinePlayer() {
		return Bukkit.getOfflinePlayer(_playerName);
	}

	// ------------------------------------------------------------------------
	/**
	 * Return the corresponding online Player, or null if not online.
	 * 
	 * @return the corresponding online Player, or null if not online.
	 */
	public Player getPlayer() {
		return Bukkit.getPlayerExact(_playerName);
	}

	// ------------------------------------------------------------------------
	/**
	 * Return the non-null Team to which this player belongs.
	 * 
	 * @return the non-null Team to which this player belongs.
	 */
	public Team getTeam() {
		return _team;
	}

	// ------------------------------------------------------------------------
	/**
	 * Set the Flag that this player is carrying.
	 * 
	 * @param flag the Flag.
	 */
	public void setCarriedFlag(Flag flag) {
		_carriedFlag = flag;
	}

	// ------------------------------------------------------------------------
	/**
	 * Return the Flag that this player is carrying, or null if not carrying.
	 * 
	 * @return the Flag that this player is carrying, or null if not carrying.
	 */
	public Flag getCarriedFlag() {
		return _carriedFlag;
	}

	// ------------------------------------------------------------------------
	/**
	 * Return true if the player is carrying a flag.
	 * 
	 * @return true if the player is carrying a flag.
	 */
	public boolean isCarryingFlag() {
		return _carriedFlag != null;
	}

	// ------------------------------------------------------------------------
	/**
	 * Return a reference to this player's mutable Score instance.
	 * 
	 * @return a reference to this player's mutable Score instance.
	 */
	public Score getScore() {
		return _score;
	}

	// ------------------------------------------------------------------------
	/**
	 * Specify whether the player is testing the effects of floor buffs.
	 * 
	 * When testing is enabled, floor buffs ("power blocks") apply their effects
	 * to the player even if intended only for enemies.
	 * 
	 * @param testingFloorBuffs if true, testing is enabled.
	 */
	public void setTestingFloorBuffs(boolean testingFloorBuffs) {
		_testingFloorBuffs = testingFloorBuffs;
	}

	// ------------------------------------------------------------------------
	/**
	 * Return true if floor buffs ("power blocks") apply their effects to the
	 * player even if intended only for enemies.
	 * 
	 * @return true if floor buffs ("power blocks") apply their effects to the
	 *         player even if intended only for enemies.
	 */
	public boolean isTestingFloorBuffs() {
		return _testingFloorBuffs;
	}

	// ------------------------------------------------------------------------
	/**
	 * The Player's name.
	 */
	private String _playerName;

	/**
	 * The owning Team.
	 */
	private Team _team;

	/**
	 * The flag carried by an online Player.
	 */
	private Flag _carriedFlag;

	/**
	 * Player's personal score.
	 */
	private Score _score = new Score();

	/**
	 * If true, floor buffs ("power blocks") apply their effects to the player
	 * even if intended only for enemies.
	 */
	private boolean _testingFloorBuffs;
} // class TeamPlayer