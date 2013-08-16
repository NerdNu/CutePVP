package com.c45y.CutePVP;

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
 */
public class TeamPlayer {
	// ------------------------------------------------------------------------
	/**
	 * Constructor.
	 * 
	 * @param player the OfflinePlayer.
	 * @param team the Team to which the player belongs.
	 */
	public TeamPlayer(OfflinePlayer player, Team team) {
		_offlinePlayer = player;
		_team = team;
	}

	// ------------------------------------------------------------------------
	/**
	 * Put on the team's helmet on the player.
	 */
	public void setHelmet() {
		getPlayer().getInventory().setHelmet(getTeam().getTeamItemStack());
	}

	// ------------------------------------------------------------------------
	/**
	 * Set the OfflinePlayer reference.
	 * 
	 * When a player logs in, his OfflinePlayer instance changes to an actual
	 * Player object. We need to update the stored OfflinePlayer instance.
	 * 
	 * @param offlinePlayer the new OfflinePlayer instance.
	 */
	public void setOfflinePlayer(OfflinePlayer offlinePlayer) {
		_offlinePlayer = offlinePlayer;
	}

	// ------------------------------------------------------------------------
	/**
	 * Return the corresponding OfflinePlayer.
	 * 
	 * @return the corresponding OfflinePlayer.
	 */
	public OfflinePlayer getOfflinePlayer() {
		return _offlinePlayer;
	}

	// ------------------------------------------------------------------------
	/**
	 * Return the corresponding online Player, or null if not online.
	 * 
	 * @return the corresponding online Player, or null if not online.
	 */
	public Player getPlayer() {
		return _offlinePlayer.getPlayer();
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
	 * The online Player.
	 */
	private OfflinePlayer _offlinePlayer;

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
} // class TeamPlayer