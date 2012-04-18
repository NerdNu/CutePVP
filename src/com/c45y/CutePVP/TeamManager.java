package com.c45y.CutePVP;

import org.bukkit.ChatColor;

public class TeamManager {
	private CutePVP cp;
	public Team redTeam;
	public Team blueTeam;
	public Team yellowTeam;
	public Team greenTeam;
	
	public TeamManager(CutePVP cutePVP) {
		cp = cutePVP;
		redTeam = new Team( cp.getServer(), cp.getConfig(), "red", ChatColor.RED, (byte) 14);
		blueTeam = new Team( cp.getServer(), cp.getConfig(), "blue", ChatColor.BLUE, (byte) 3);
		yellowTeam = new Team( cp.getServer(), cp.getConfig(), "yellow", ChatColor.YELLOW, (byte) 4);
		greenTeam = new Team( cp.getServer(), cp.getConfig(), "green", ChatColor.GREEN, (byte) 5);
	}
}
