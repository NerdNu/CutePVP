package com.c45y.CutePVP;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Wool;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class Team {
	private String teamName;
	private ChatColor teamChatColor;
	private byte woolData;
	private HashMap<String, Integer> teamMembers = new HashMap<String, Integer>();
	public Player flagHolder;
	private CutePVP plugin;

	public Team(CutePVP plugin, String n1, ChatColor c2, byte w1) {
		this.plugin = plugin;
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
		return (Block) new Wool(53, woolData);
	}

	public ItemStack getTeamItemStack() {
		return new ItemStack(35, 1, woolData);
	}

	public short getTeamWoolData() {
		return woolData;
	}

	public String encodeTeamColor(String s1) {
		return getTeamChatColor() + s1 + ChatColor.WHITE;
	}

	/* Team location configuration */

	public Location getTeamSpawn() {
		FileConfiguration config = plugin.getConfig();
		World overworld = Bukkit.getWorlds().get(0);
		return new Location(overworld, config.getDouble(teamName + "spawn.x"), config.getDouble(teamName + "spawn.y"), config.getDouble(teamName
						+ "spawn.z"), (float) config.getDouble(teamName + "spawn.yaw"), (float) config.getDouble(teamName + "spawn.pitch"));
	}

	public void setTeamSpawn(Location l1) {
		FileConfiguration config = plugin.getConfig();
		config.set(teamName + "spawn.x", l1.getX());
		config.set(teamName + "spawn.y", l1.getY());
		config.set(teamName + "spawn.z", l1.getZ());
		config.set(teamName + "spawn.yaw", l1.getYaw());
		config.set(teamName + "spawn.pitch", l1.getPitch());
		plugin.saveConfig();
	}

	public boolean inTeamBase(Location l1) {
		RegionManager mgr = plugin.getWorldGuard().getGlobalRegionManager().get(l1.getWorld());
		Vector pt = new Vector(l1.getBlockX(), l1.getBlockY(), l1.getBlockZ());
		ApplicableRegionSet set = mgr.getApplicableRegions(pt);

		for (ProtectedRegion r : set) {
			if (r.getId().equalsIgnoreCase(teamName + "base"))
				return true;
		}

		return false;
	}

	public void setCarrier(Player player) {
		flagHolder = player;
		if (player != null)
			plugin.getConfig().set("carrier." + teamName + "flag", player.getName());
	}

	public void removeCarrier() {
		flagHolder = null;
		plugin.getConfig().set("carrier." + teamName + "flag", null);
		plugin.saveConfig();
	}

	public boolean isTeamFlagRegion(Location l1) {
		RegionManager mgr = plugin.getWorldGuard().getGlobalRegionManager().get(l1.getWorld());
		Vector pt = new Vector(l1.getBlockX(), l1.getBlockY(), l1.getBlockZ());
		ApplicableRegionSet set = mgr.getApplicableRegions(pt);

		for (ProtectedRegion r : set) {
			if (r.getId().equalsIgnoreCase(teamName + "_flag"))
				return true;
		}

		return false;
	}

	public boolean inTeamSpawn(Location l1) {
		RegionManager mgr = plugin.getWorldGuard().getGlobalRegionManager().get(l1.getWorld());
		Vector pt = new Vector(l1.getBlockX(), l1.getBlockY(), l1.getBlockZ());
		ApplicableRegionSet set = mgr.getApplicableRegions(pt);

		for (ProtectedRegion r : set) {
			if (r.getId().equalsIgnoreCase(teamName + "spawn"))
				return true;
		}

		return false;
	}

	/* Flag manipulation */

	public Location getTeamFlag() {
		FileConfiguration config = plugin.getConfig();
		return new Location(Bukkit.getWorlds().get(0), config.getDouble(teamName + "flag.x"), config.getDouble(teamName + "flag.y"),
						config.getDouble(teamName + "flag.z"));
	}

	public void dropTeamFlag(Location l1) {
		Block flag = Bukkit.getWorlds().get(0).getBlockAt(l1);
		flag.setTypeIdAndData(35, woolData, false);
		setTeamFlag(l1);
		removeCarrier();
	}

	public void setTeamFlag(Location l1) {
		FileConfiguration config = plugin.getConfig();
		config.set(teamName + "flag.x", l1.getX());
		config.set(teamName + "flag.y", l1.getY());
		config.set(teamName + "flag.z", l1.getZ());
		plugin.saveConfig();
	}

	public Location getTeamFlagHome() {
		FileConfiguration config = plugin.getConfig();
		return new Location(Bukkit.getWorlds().get(0), config.getDouble(teamName + "flag.home.x"), config.getDouble(teamName + "flag.home.y"),
						config.getDouble(teamName + "flag.home.z"));
	}

	public void setTeamFlagHome(Location l1) {
		FileConfiguration config = plugin.getConfig();
		config.set(teamName + "flag.home.x", l1.getX());
		config.set(teamName + "flag.home.y", l1.getY());
		config.set(teamName + "flag.home.z", l1.getZ());
		plugin.saveConfig();
	}

	public boolean isTeamFlag(Location l1) {
		Location teamFlag = getTeamFlag();
		if (l1.getBlockX() == teamFlag.getBlockX() && l1.getBlockY() == teamFlag.getBlockY() && l1.getBlockZ() == teamFlag.getBlockZ()) {
			return true;
		}
		return false;
	}

	public void respawnTeamFlag() {
		Bukkit.getWorlds().get(0).getBlockAt(getTeamFlag()).setType(Material.AIR);
		Block flag_home = Bukkit.getWorlds().get(0).getBlockAt(getTeamFlagHome());
		flag_home.setTypeIdAndData(35, woolData, false);
		setTeamFlag(getTeamFlagHome());
		removeCarrier();
	}

	/* Team player management */

	public void addPlayer(String playerName) {
		addExistingPlayer(playerName, 0);
	}

	public void addExistingPlayer(String playerName, int score) {
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
			Player player = Bukkit.getPlayer(playerName);
			if (player != null)
				online.add(playerName);
		}
		return new HashSet<String>(online);
		// return teamMembers.keySet();
	}

	public void message(String m1) {
		for (String player : getTeamMembersOnline()) {
			Bukkit.getPlayer(player).sendMessage(m1);
		}
	}

	public void setHelmet(Player player) {
		player.getInventory().setHelmet(getTeamItemStack());
	}

	public void setCompassTarget() {
		for (String playerName : teamMembers.keySet()) {
			Player player = Bukkit.getPlayer(playerName);
			if (player != null)
				player.setCompassTarget(getTeamFlag());
		}
	}

	/* Team scoring methods */

	public void addTeamScore(int inc) {
		plugin.getConfig().set(teamName + "score.total", getTeamScore() + inc);
		plugin.saveConfig();
	}

	public void addTeamKill() {
		plugin.getConfig().set(teamName + "kills.total", getTeamKills() + 1);
		plugin.saveConfig();
	}

	public int getTeamKills() {
		return plugin.getConfig().getInt(teamName + "kills.total");
	}

	public int getTeamScore() {
		return plugin.getConfig().getInt(teamName + "score.total");
	}

	public void addPlayerScore(String player, int inc) {
		int playerScore = getPlayerScore(player);
		teamMembers.put(player, playerScore + inc);
	}

	public int getPlayerScore(String player) {
		return teamMembers.get(player).intValue();
	}
}
