package com.c45y.CutePVP;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

// --------------------------------------------------------------------------
/**
 * Does uniform formatting of messages.
 */
public class Messages {
	/**
	 * The color of broadcast messages.
	 */
	public static final ChatColor BROADCAST_COLOR = ChatColor.LIGHT_PURPLE;

	/**
	 * The color of messages indicating successful completion.
	 */
	public static final ChatColor SUCCESS_COLOR = ChatColor.GOLD;

	/**
	 * The color of messages indicating unsuccessful completion.
	 */
	public static final ChatColor FAILURE_COLOR = ChatColor.DARK_RED;

	/**
	 * The prefix at the start of broadcast messages.
	 */
	public static final String PREFIX = "[CutePvP] ";

	// ------------------------------------------------------------------------
	/**
	 * Broadcast a message to all players.
	 */
	public static void broadcast(String message) {
		Bukkit.getServer().broadcastMessage(BROADCAST_COLOR + PREFIX + message);
	}

	// ------------------------------------------------------------------------
	/**
	 * Send a message to a player upon successful completion of an action.
	 * 
	 * @param recipient the recipient of the message.
	 * @param prefix if not null, additional text prefixed to the message.
	 * @param message the message.
	 */
	public static void success(CommandSender recipient, String prefix, String message) {
		String text = (prefix != null) ? prefix + message : message;
		recipient.sendMessage(SUCCESS_COLOR + text);
	}

	// ------------------------------------------------------------------------
	/**
	 * Send a message to a player upon failed completion of an action.
	 * 
	 * @param recipient the recipient of the message.
	 * @param prefix if not null, additional text prefixed to the message.
	 * @param message the message.
	 */
	public static void failure(CommandSender recipient, String prefix, String message) {
		String text = (prefix != null) ? prefix + message : message;
		recipient.sendMessage(FAILURE_COLOR + text);
	}

} // class Messages