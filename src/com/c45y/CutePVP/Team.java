package com.c45y.CutePVP;

import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.material.Wool;

public class Team {
	private Server server;
	private FileConfiguration config;
	private String teamName;
	private ChatColor teamChatColor;
	private byte woolData;
	private HashMap<String, Integer> teamMembers = new HashMap<String, Integer>();
	private Location teamFlagHomePosition;

	public Team(Server s1, FileConfiguration f1, String n1, ChatColor c2, byte w1) {
		server = s1;
		config = f1;
		teamName = n1;
		teamChatColor = c2;
		teamFlagHomePosition = getTeamFlag();
		woolData = w1;
	}
	
	public String getTeamName() {
		return teamName;
	}
	
	public ChatColor getTeamChatColor() {
		return teamChatColor;
	}
	
	public Block getTeamMaterial() {
		return (Block) new Wool(53,woolData);
	}
	
	/* Team location configuration */
	
	public Location getTeamSpawn() {
		return new Location(
			server.getWorlds().get(0),
			config.getDouble(teamName + "spawn.x"),
			config.getDouble(teamName + "spawn.y"),
			config.getDouble(teamName + "spawn.z"),
			(float)config.getDouble(teamName + "spawn.yaw"),
			(float)config.getDouble(teamName + "spawn.pitch"));
	}
	
	public void setTeamSpawn(Location l1) {
		config.set(teamName + "spawn.x", l1.getX());
		config.set(teamName + "spawn.y", l1.getY());
		config.set(teamName + "spawn.z", l1.getZ());
		config.set(teamName + "spawn.yaw", l1.getYaw());
		config.set(teamName + "spawn.pitch", l1.getPitch());
	}
	
	/* Flag manipulation */
	
	public Location getTeamFlag() {
		return new Location(
			server.getWorlds().get(0),
			config.getDouble(teamName + "flag.x"),
			config.getDouble(teamName + "flag.y"),
			config.getDouble(teamName + "flag.z"),
			(float)config.getDouble(teamName + "flag.yaw"),
			(float)config.getDouble(teamName + "flag.pitch"));
	}
	
	public void setTeamFlag(Location l1) {
		config.set(teamName + "flag.x", l1.getX());
		config.set(teamName + "flag.y", l1.getY());
		config.set(teamName + "flag.z", l1.getZ());
		config.set(teamName + "flag.yaw", l1.getYaw());
		config.set(teamName + "flag.pitch", l1.getPitch());
	}
	
	public boolean isTeamFlag(Location l1) {
		Location teamFlag = getTeamFlag();
		if (l1.getBlockX() == teamFlag.getBlockX() && l1.getBlockY() == teamFlag.getBlockY() &&	l1.getBlockZ() == teamFlag.getBlockZ()) {
			return true;
		}
		return false;
	}
	
	public void respawnTeamFlag() {
		server.getWorlds().get(0).getBlockAt(getTeamFlag()).setType(Material.AIR);
		Block flag_home = server.getWorlds().get(0).getBlockAt(teamFlagHomePosition);
		flag_home.setTypeIdAndData(35, woolData, false);
		setTeamFlag(teamFlagHomePosition);
		
	}
	
	/* Team player management */
	
	public void addPlayer(String playerName) {
		addExistingPlayer(playerName, 0);
	}
	
	public void addExistingPlayer(String playerName,int score) {
		teamMembers.put(playerName, score);
	}
	
	public void addExistingPlayers(HashMap<String, Integer> teamMembersToAdd) {
		teamMembers.putAll(teamMembersToAdd);
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
	
	/* Team scoring methods */
	
	public void addTeamScore(int inc) {
		config.set(teamName + "score.total", getTeamScore() + inc);
	}
	
	public int getTeamScore() {
		return config.getInt(teamName + "score.total");
	}
	
	public void addPlayerScore(String player, int inc) {
		int playerScore = getPlayerScore(player);
		teamMembers.put(player, playerScore + inc);
	}
	
	public int getPlayerScore(String player) {
		return teamMembers.get(player).intValue();
	}
	
	/* Debugging method */
	
	private void print(String p1) {
		System.out.println(p1);
	}
	
}
