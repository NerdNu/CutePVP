package com.c45y.CutePVP;

// ----------------------------------------------------------------------------
/**
 * Constants for supported permissions.
 */
public class Permissions {
	/**
	 * Admin with access to all CutePVP commands A strict superset of the
	 * CutePVP.mod permission.
	 */
	public static final String ADMIN = "CutePVP.admin";

	/**
	 * Moderator. Can see all team chats.
	 */
	public static final String MOD = "CutePVP.mod";

	/**
	 * Permission to build in an enemy team's base. Assigned to moderators.
	 */
	public static final String BUILD = "CutePVP.build";

	/**
	 * An ordinary player, allocated to a team.
	 */
	public static final String USER = "CutePVP.user";

	/**
	 * Any player with this permission is not allocated to a team.
	 */
	public static final String EXEMPT = "CutePVP.exempt";
} // class Permissions