package com.c45y.CutePVP;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

// ----------------------------------------------------------------------------
/**
 * Records the addresses used by players to connect to the server.
 */
public class ConnectionRecords {
	// ------------------------------------------------------------------------
	/**
	 * Save the records to the specified file.
	 * 
	 * @param file the file to save.
	 * @param logger used to log messages.
	 */
	public void save(File file, Logger logger) {
		YamlConfiguration config = new YamlConfiguration();
		ConfigurationSection players = config.createSection("players");
		for (String playerName : _playerToAddress.keySet()) {
			ArrayList<String> addresses = new ArrayList<String>(getAddresses(playerName));
			players.set(playerName, addresses);
		}

		ConfigurationSection addresses = config.createSection("addresses");
		for (String address : _addressToPlayer.keySet()) {
			ArrayList<String> playerNames = new ArrayList<String>(getPlayers(address));
			// Replace '.' characters in the address or they will be treated
			// as a hierarchy of sections.
			String addressKey = address.replace('.', '_');
			addresses.set(addressKey, playerNames);
		}

		try {
			config.save(file);
		} catch (IOException ex) {
			logger.severe(ex.getClass().getName() + ": " + ex.getMessage() + " saving connection records.");
		}
	} // save

	// ------------------------------------------------------------------------
	/**
	 * Load the records from the specified file.
	 * 
	 * @param file the file to load.
	 * @param logger used to log messages.
	 */
	public void load(File file, Logger logger) {
		YamlConfiguration config = new YamlConfiguration();
		try {
			config.load(file);
		} catch (Exception ex) {
			logger.severe(ex.getClass().getName() + ": " + ex.getMessage() + " saving connection records.");
			return;
		}

		ConfigurationSection playersSection = config.getConfigurationSection("players");
		if (playersSection != null) {
			for (String playerName : playersSection.getKeys(false)) {
				List<String> addresses = playersSection.getStringList(playerName);
				_playerToAddress.put(playerName, new HashSet<String>(addresses));
			}
		}

		ConfigurationSection addressesSection = config.getConfigurationSection("addresses");
		if (addressesSection != null) {
			for (String addressKey : addressesSection.getKeys(false)) {
				List<String> players = addressesSection.getStringList(addressKey);
				String address = addressKey.replace('_', '.');
				_addressToPlayer.put(address, new HashSet<String>(players));
			}
		}
	} // load

	// ------------------------------------------------------------------------
	/**
	 * Add a record of the address used by the specified Player.
	 * 
	 * @param player the player.
	 */
	public void addRecord(Player player) {
		InetAddress address = player.getAddress().getAddress();
		if (address != null) {
			String ip = address.toString().split("/")[1];
			getPlayers(ip).add(player.getName());
			getAddresses(player.getName()).add(ip);
		}
	}

	// ------------------------------------------------------------------------
	/**
	 * Return the set of all alternate accounts of the player, defined as all
	 * those accounts that used the same IP address(es) as the player.
	 * 
	 * @param playerName the player name.
	 * @return the non-null set of alts.
	 */
	public HashSet<String> getAlts(String playerName) {
		HashSet<String> alts = new HashSet<String>();
		for (String address : getAddresses(playerName)) {
			alts.addAll(getPlayers(address));
		}
		return alts;
	}

	// ------------------------------------------------------------------------
	/**
	 * Return the non-null set of players that have played using the specified
	 * address.
	 * 
	 * @param address the IP address.
	 * @return the non-null set of players that have played using the specified
	 *         address.
	 */
	public HashSet<String> getPlayers(String address) {
		HashSet<String> players = _addressToPlayer.get(address);
		if (players == null) {
			players = new HashSet<String>();
			_addressToPlayer.put(address, players);
		}
		return players;
	}

	// ------------------------------------------------------------------------
	/**
	 * Return the non-null set of addresses used by the player.
	 * 
	 * @param playerName the name of the player.
	 * @return the non-null set of addresses used by the player.
	 */
	public HashSet<String> getAddresses(String playerName) {
		HashSet<String> addresses = _playerToAddress.get(playerName);
		if (addresses == null) {
			addresses = new HashSet<String>();
			_playerToAddress.put(playerName, addresses);
		}
		return addresses;
	}

	// ------------------------------------------------------------------------
	/**
	 * A map from player name to the set of used IP addresses.
	 */
	private HashMap<String, HashSet<String>> _playerToAddress = new HashMap<String, HashSet<String>>();

	/**
	 * A map from IP addresses to the set of player names using those addresses.
	 */
	private HashMap<String, HashSet<String>> _addressToPlayer = new HashMap<String, HashSet<String>>();
} // class ConnectionRecords