package com.c45y.CutePVP;

import org.bukkit.ChatColor;

public class TeamManager {
	private CutePVP cp;
	private Team staffTeam;
	public Team redTeam;
	public Team blueTeam;
	public Team yellowTeam;
	public Team greenTeam;

	public TeamManager(CutePVP cutePVP) {
		cp = cutePVP;
		staffTeam = new Team( cp.getServer(), cp.getConfig(), "staff", ChatColor.RED, (byte) 15);
		redTeam = new Team( cp.getServer(), cp.getConfig(), "red", ChatColor.RED, (byte) 14);
		blueTeam = new Team( cp.getServer(), cp.getConfig(), "blue", ChatColor.BLUE, (byte) 3);
		yellowTeam = new Team( cp.getServer(), cp.getConfig(), "yellow", ChatColor.YELLOW, (byte) 4);
		greenTeam = new Team( cp.getServer(), cp.getConfig(), "green", ChatColor.GREEN, (byte) 5);
	}

	public void onFirstJoin(String player) {
		/* Throw all staff into one team, makes it easy to track them */
		if( cp.getServer().getPlayer(player).hasPermission("CutePVP.mod")) {
			staffTeam.addPlayer(player);
			cp.getServer().getPlayer(player).sendMessage(staffTeam.encodeTeamColor("Welcome to the STAFF team!"));
			return;
		}

		/* If not a staff member decide what team to put them in */

		int value = decideTeam(player);
		if(value == 0) {
			redTeam.addPlayer(player);
			cp.getServer().getPlayer(player).sendMessage(redTeam.encodeTeamColor("Welcome to the RED team!"));
			return;
		}
		if(value == 1) {
			blueTeam.addPlayer(player);
			cp.getServer().getPlayer(player).sendMessage(redTeam.encodeTeamColor("Welcome to the BLUE team!"));
			return;
		}
		if(value == 2) { 
			yellowTeam.addPlayer(player);
			cp.getServer().getPlayer(player).sendMessage(redTeam.encodeTeamColor("Welcome to the YELLOW team!"));
			return;
		}
		if(value == 3) { 
			greenTeam.addPlayer(player);
			cp.getServer().getPlayer(player).sendMessage(redTeam.encodeTeamColor("Welcome to the GREEN team!"));
			return;
		}
	}

	public int decideTeam(String inpt) {
		int value = Character.getNumericValue(ChatColor.stripColor(inpt).charAt(inpt.length()-1));
		for(int i: new int[] { 10, 11, 23, 30, 33 } ) {
			if(value == i) {
				return 0;
			}
		}
		for(int i: new int[] { 14, 17, 28, 21, 29 } ) {
			if(value == i) {
				return 1;
			}
		}
		for(int i: new int[] { 12, 13, 15, 18, 25, 31, 32 } ) {
			if(value == i) {
				return 2;
			}
		}
		for(int i: new int[] { 0, 16, 19, 20, 22, 24, 27, 34, 35 } ) {
			if(value == i) {
				return 3;
			}
		}
		return 0;
	}

	public void messageTeam(String s1, String m1) {
		/* Make sure staff receive all messages. */
		staffTeam.message(m1);
		if (redTeam.getTeamName().equals(s1)) {
			redTeam.message(m1);
			return;
		}
		if (blueTeam.getTeamName().equals(s1)) {
			blueTeam.message(m1);
			return;
		}
		if (yellowTeam.getTeamName().equals(s1)) {
			yellowTeam.message(m1);
			return;
		}
		if (greenTeam.getTeamName().equals(s1)) {
			greenTeam.message(m1);
			return;
		}
	}

	public Team getTeamMemberOf(String player){
		if (staffTeam.inTeam(player)) {
			return staffTeam;
		}
		if (redTeam.inTeam(player)) {
			return redTeam;
		}
		if (blueTeam.inTeam(player)) {
			return blueTeam;
		}
		if (yellowTeam.inTeam(player)) {
			return yellowTeam;
		}
		if (greenTeam.inTeam(player)) {
			return greenTeam;
		}
		return null;
	}
}
