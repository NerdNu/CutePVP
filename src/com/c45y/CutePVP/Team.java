package com.c45y.CutePVP;

import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class Team {
	private Server server;
	private FileConfiguration config;
	private String teamName;
	private ChatColor teamChatColor;
	private HashMap<String, Integer> teamMembers = new HashMap<String, Integer>();

	public Team(Server s1, FileConfiguration f1, String n1,ChatColor c2) {
		server = s1;
		config = f1;
		teamName = n1;
		teamChatColor = c2;
	}
	
	public String getTeamName() {
		return teamName;
	}
	
	public ChatColor getTeamChatColor() {
		return teamChatColor;
	}
	
	public Location getTeamSpawn() {
		return new Location(
				server.getWorlds().get(0),
				config.getDouble("team." + teamName + ".x"),
				config.getDouble("team." + teamName + ".y"),
				config.getDouble("team." + teamName + ".z"),
				(float)config.getDouble("team." + teamName + ".yaw"),
				(float)config.getDouble("team." + teamName + ".pitch"));
	}
	
	public void setTeamSpawn(Location l1) {
		config.set("team." + teamName + ".x", l1.getX());
		config.set("team." + teamName + ".y", l1.getY());
		config.set("team." + teamName + ".z", l1.getZ());
		config.set("team." + teamName + ".yaw", l1.getYaw());
		config.set("team." + teamName + ".pitch", l1.getPitch());
	}
	
	public void addPlayer(String playerName) {
		addExistingPlayer(playerName, 0);
	}
	
	public void addExistingPlayer(String playerName,int score) {
		teamMembers.put(playerName, score);
	}
	
	public void addExistingPlayers(HashMap<String, Integer> teamMembersToAdd) {
		teamMembersToAdd.putAll(teamMembers);
	}
	
	public int removePlayer(String playerName) {
		int score = teamMembers.get(playerName);
		teamMembers.remove(playerName);
		return score;
	}
	
	public boolean inTeam(String playerName) {
		if (teamMembers.containsKey(playerName)) { 
			return true;
		}
		return false;
	}
	
	// Debugging method
	private void print(String p1) {
		System.out.println(p1);
	}
	
}
