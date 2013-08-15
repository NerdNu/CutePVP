package com.c45y.CutePVP;

// --------------------------------------------------------------------------
/**
 * Constants for supported permissions.
 */
public class Permissions {
	/**
	 * Admin with access to all CutePVP commands, not allocated to teams.
	 * A strict superset of the CutePVP.mod permission.
	 */
	public static final String ADMIN = "CutePVP.admin";
	
	/**
	 * Moderator, not allocated to teams. Can see all team chats.
	 */
	public static final String MOD = "CutePVP.mod";
	
	/**
	 * An ordinary player, allocated to a team.
	 */
	public static final String USER = "CutePVP.user";
	
	/**
	 * Any player with this permission is not allocated to a team.
	 */
	public static final String EXEMPT = "CutePVP.exempt";
} // class Permissions