package com.c45y.CutePVP;

import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Wool;

public class Team {
	private Server server;
	private FileConfiguration config;
	private String teamName;
	private ChatColor teamChatColor;
	private byte woolData;
	private HashMap<String, Integer> teamMembers = new HashMap<String, Integer>();
	public Player flagHolder;

	public Team(Server s1, FileConfiguration f1, String n1, ChatColor c2, byte w1) {
		server = s1;
		config = f1;
		teamName = n1;
		teamChatColor = c2;
		woolData = w1;
	}

	public String getTeamName() {
		return teamName;
	}

	public ChatColor getTeamChatColor() {
		return teamChatColor;
	}

	public Block getTeamBlock() {
		return (Block) new Wool(53,woolData);
	}

	public ItemStack getTeamItemStack() {
		return new ItemStack(35, 1, woolData);
	}
	
	public short getTeamWoolData() {
		return (short) woolData;
	}

	public String encodeTeamColor(String s1) {
		return getTeamChatColor() + s1 + ChatColor.WHITE;
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
	
	public boolean inTeamBase(Location l1) {
		int rad = config.getInt("base.protection.radius");
		return inTeamBase(l1,rad);
	}
	
	public boolean inTeamBase(Location l1,int rad) {
		Location playerLocation = l1;
		Location spawnLocation = getTeamSpawn();
		double dx = spawnLocation.getX() - playerLocation.getX();
		double dz = spawnLocation.getZ() - playerLocation.getZ();
		if ((dx<rad && dx>-rad) && (dz<rad && dz>-rad)) {
			return true;
		}
		return false;
	}

	/* Flag manipulation */

	public Location getTeamFlag() {
		return new Location(
				server.getWorlds().get(0),
				config.getDouble(teamName + "flag.x"),
				config.getDouble(teamName + "flag.y"),
				config.getDouble(teamName + "flag.z"));
	}
	
	public void dropTeamFlag(Location l1) {
		Block flag = server.getWorlds().get(0).getBlockAt(l1); //Get a handle for the
		flag.setTypeIdAndData(35, woolData, false);
		setTeamFlag(l1);
	}

	public void setTeamFlag(Location l1) {
		config.set(teamName + "flag.x", l1.getX());
		config.set(teamName + "flag.y", l1.getY());
		config.set(teamName + "flag.z", l1.getZ());
	}

	public Location getTeamFlagHome() {
		return new Location(
				server.getWorlds().get(0),
				config.getDouble(teamName + "flag.home.x"),
				config.getDouble(teamName + "flag.home.y"),
				config.getDouble(teamName + "flag.home.z"));
	}

	public boolean isTeamFlag(Location l1) {
		Location teamFlag = getTeamFlag();
		if (l1.getBlockX() == teamFlag.getBlockX() && l1.getBlockY() == teamFlag.getBlockY() &&	l1.getBlockZ() == teamFlag.getBlockZ()) {
			return true;
		}
		return false;
	}

	public void respawnTeamFlag() {
		server.getWorlds().get(0).getBlockAt(getTeamFlag()).setType(Material.AIR); //Remove the placed flag
		Block flag_home = server.getWorlds().get(0).getBlockAt(getTeamFlagHome()); //Get a handle for the
		flag_home.setTypeIdAndData(35, woolData, false);
		setTeamFlag(getTeamFlagHome());
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
	
	public Set<String> getTeamMembers() {
		return teamMembers.keySet();
	}
	
	public Set<String> getTeamMembersOnline() {
		List<String> online = new ArrayList<String>();
		for (String playerName : teamMembers.keySet()) {
			Player player = server.getPlayer(playerName);
			if (player != null)
				online.add(playerName);
		}
		return new HashSet<String>(online);
//		return teamMembers.keySet();
	}

	public void message(String m1) {
		for( String player: getTeamMembersOnline() ){
			server.getPlayer(player).sendMessage(m1);
		}
	}
	
	public void setHelmet(Player player) {
		player.getInventory().setHelmet(getTeamItemStack());
	}
	
	public void setCompassTarget() {
		for( String playerName: teamMembers.keySet() ){
			Player player = server.getPlayer(playerName);
			if (player != null)
				player.setCompassTarget(getTeamFlag());
		}
	}

	/* Team scoring methods */

	public void addTeamScore(int inc) {
		config.set(teamName + "score.total", getTeamScore() + inc);
	}
        
        public void addTeamKill() {
            config.set(teamName + "kills.total", getTeamKills() + 1);
        }
        
        public int getTeamKills() {
            return config.getInt(teamName + "kills.total");
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
}
