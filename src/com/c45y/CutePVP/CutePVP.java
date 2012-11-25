package com.c45y.CutePVP;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import java.util.HashMap;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Level;

public class CutePVP extends JavaPlugin {
	private final CutePVPListener loglistener = new CutePVPListener(this);
	HashMap<String, String> fposSet = new HashMap<String, String>();
	TeamManager tm;
        WorldGuardPlugin wgPlugin = null;
        
        public WorldGuardPlugin getWorldGuard() {
            if (wgPlugin == null) {
                wgPlugin = (WorldGuardPlugin)getServer().getPluginManager().getPlugin("WorldGuard");
                if (wgPlugin != null) {
                    if (!wgPlugin.isEnabled()) {
                        getPluginLoader().enablePlugin(wgPlugin);
                    }
                }
                else {
                    getLogger().log(Level.INFO, "Could not load worldguard, disabling");
                    wgPlugin = null;
                }
            }
            return wgPlugin;
        }
        
	public void loadPlayers() {
		List<String> redPlayerNames = getConfig().getStringList("teams.red.players");
		for (String redPlayerName : redPlayerNames) {
			tm.redTeam.addPlayer(redPlayerName);
		}
		List<String> bluePlayerNames = getConfig().getStringList("teams.blue.players");
		for (String bluePlayerName : bluePlayerNames) {
			tm.blueTeam.addPlayer(bluePlayerName);
		}
		List<String> yellowPlayerNames = getConfig().getStringList("teams.yellow.players");
		for (String yellowPlayerName : yellowPlayerNames) {
			tm.yellowTeam.addPlayer(yellowPlayerName);
		}
		List<String> greenPlayerNames = getConfig().getStringList("teams.green.players");
		for (String greenPlayerName : greenPlayerNames) {
			tm.greenTeam.addPlayer(greenPlayerName);
		}
		getLogger().info("Red:" + redPlayerNames.size() + " Blue:" + bluePlayerNames.size() + " Yellow:" + yellowPlayerNames.size() + " Green:" + greenPlayerNames.size());
	}

	public void savePlayers() {
		getConfig().set("teams.red.players", new ArrayList<String>(tm.redTeam.getTeamMembers()));
		getConfig().set("teams.blue.players", new ArrayList<String>(tm.blueTeam.getTeamMembers()));
		getConfig().set("teams.yellow.players", new ArrayList<String>(tm.yellowTeam.getTeamMembers()));
		getConfig().set("teams.green.players", new ArrayList<String>(tm.greenTeam.getTeamMembers()));
/*
		getConfig().set("teams.red.online", new ArrayList<String>(tm.redTeam.getTeamMembersOnline()));
		getConfig().set("teams.blue.online", new ArrayList<String>(tm.blueTeam.getTeamMembersOnline()));
		getConfig().set("teams.yellow.online", new ArrayList<String>(tm.yellowTeam.getTeamMembersOnline()));
		getConfig().set("teams.green.online", new ArrayList<String>(tm.greenTeam.getTeamMembersOnline()));
*/
        saveConfig();
	}

	@Override
	public void onEnable() {
		tm = new TeamManager(this);

		File config_file = new File(getDataFolder(), "config.yml");
		if (!config_file.exists()) {
			getConfig().options().copyDefaults(true);
			saveConfig();
		}

		loadPlayers();

		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(loglistener, this);
		System.out.println(this.toString() + " enabled");

		getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			public void run() {
				//getServer().broadcastMessage(ChatColor.DARK_PURPLE + "[NOTICE] Flags are respawning!");
				//respawnFlags();
				getLogger().info("Running buff");
				Location powerblock = new Location(
						getServer().getWorlds().get(0),
						getConfig().getDouble("block.buff.x"),
						getConfig().getDouble("block.buff.y"),
						getConfig().getDouble("block.buff.z"));
				if (getServer().getWorlds().get(0).getBlockAt(powerblock) != null) {
					Block gPowerBlock = getServer().getWorlds().get(0).getBlockAt(powerblock);
					if (gPowerBlock.getType() == Material.WOOL) {
						Team winTeam = tm.getTeamFromWool(gPowerBlock.getData());
						if (winTeam != null) {
							getServer().broadcastMessage(ChatColor.DARK_PURPLE + "[NOTICE] " + winTeam.getTeamName() + " gets buff!");
							for (Player playeri : getServer().getOnlinePlayers()) {
								if (winTeam.inTeam(playeri.getName())) {
									playeri.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 12000, 0));
									playeri.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 1200, 1));
								}
							}
						}
					}
				}
				getLogger().info("End running buff");
				if (tm.blueTeam.flagHolder == null) {
					tm.blueTeam.respawnTeamFlag();
				}
				if (tm.redTeam.flagHolder == null) {
					tm.redTeam.respawnTeamFlag();
				}
				if (tm.yellowTeam.flagHolder == null) {
					tm.yellowTeam.respawnTeamFlag();
				}
				if (tm.greenTeam.flagHolder == null) {
					tm.greenTeam.respawnTeamFlag();
				}
			}
		}, 1200, 12000);

		getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			public void run() {
				tm.blueTeam.setCompassTarget();
				tm.redTeam.setCompassTarget();
				tm.yellowTeam.setCompassTarget();
				tm.greenTeam.setCompassTarget();
			}
		}, 1200, 1200);
	}

	public void onDisable() {
		savePlayers();
		System.out.println(this.toString() + " disabled");
	}
	
	/* No longer needed
	public Location getTeamFlagLoc(String team) {
		try {
			return new Location(getServer().getWorlds().get(0),
					getConfig().getInt("ctf." + team + ".curr.x"),
					getConfig().getInt("ctf." + team + ".curr.y"),
					getConfig().getInt("ctf." + team + ".curr.z")
			);
		}
		catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	public void setTeamFlagSpawnLoc(String team, Location loc) {
		if (loc != null) {
			getConfig().set("ctf." + team + ".x", loc.getBlockX());
			getConfig().set("ctf." + team + ".y",loc.getBlockY());
			getConfig().set("ctf." + team + ".z", loc.getBlockZ());
		}
		else {
			getConfig().set("ctf." + team + ".x", null);
			getConfig().set("ctf." + team + ".y", null);
			getConfig().set("ctf." + team + ".z", null);
		}
		saveConfig();
	}

	public void setTeamFlagLoc(String team, Location loc) {
		if (loc != null) {
			getConfig().set("ctf." + team + ".curr.x", loc.getBlockX());
			getConfig().set("ctf." + team + ".curr.y",loc.getBlockY());
			getConfig().set("ctf." + team + ".curr.z", loc.getBlockZ());
		}
		else {
			getConfig().set("ctf." + team + ".curr.x", null);
			getConfig().set("ctf." + team + ".curr.y", null);
			getConfig().set("ctf." + team + ".curr.z", null);
		}
		saveConfig();
	}

	public Location getTeamFlagSpawnLoc(String team) {
		try {
			return new Location(getServer().getWorlds().get(0),
					getConfig().getInt("ctf." + team + ".x"),
					getConfig().getInt("ctf." + team + ".y"),
					getConfig().getInt("ctf." + team + ".z")
			);
		}
		catch (Exception ex) {
			return null;
		}
	}
	*/

	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("Sorry, this plugin cannot be used from console");
			return true;
		}
		Player player = (Player) sender;
		if (command.getName().equalsIgnoreCase("g")) {
			String str = StringUtils.join(args, " ");
			for (Player playeri : getServer().getOnlinePlayers()) {
				playeri.sendMessage(ChatColor.RED + ">" + ChatColor.BLUE + ">" + ChatColor.GREEN + ">" + ChatColor.YELLOW + ">" + ChatColor.WHITE + " <" + tm.getTeamMemberOf(sender.getName()).encodeTeamColor(sender.getName()) + "> " + str);
			}
			return true;
		}
	else if (command.getName().equalsIgnoreCase("cutepvp")) {
			if (args.length == 1 && new String("reload").equals(args[0])) {
//				loadPlayers();
				return true;
			}
			if (args.length == 1 && new String("save").equals(args[0])) {
				savePlayers();
				sender.sendMessage("[CutePVP] Saved");
				return true;
			}
                        if (args.length == 1 && new String("setbuff").equals(args[0])) {
                            Block block = player.getLocation().getBlock().getRelative(BlockFace.DOWN);
                            getConfig().set("block.buff.x", block.getLocation().getX());
                            getConfig().set("block.buff.y", block.getLocation().getY());
                            getConfig().set("block.buff.z", block.getLocation().getZ());
                        }
			if (args.length == 2 && new String("rmplayer").equals(args[0])) {
				String playerName = args[1];
				Team team = tm.getTeamMemberOf(playerName);
				team.removePlayer(playerName);
				sender.sendMessage("[CutePVP] Removed Player");
				return true;
			}
			if (args.length == 2 && new String("setspawn").equals(args[0])) {
				if (new String("red").equals(args[1])) { tm.redTeam.setTeamSpawn(player.getLocation()); }
				if (new String("blue").equals(args[1])) { tm.blueTeam.setTeamSpawn(player.getLocation()); }
				if (new String("yellow").equals(args[1])) { tm.yellowTeam.setTeamSpawn(player.getLocation()); }
				if (new String("green").equals(args[1])) { tm.greenTeam.setTeamSpawn(player.getLocation()); }
				sender.sendMessage("[CutePVP] Set Spawn");
				return true;
			}
			if (args.length == 2 && new String("setflag").equals(args[0])) {
				if (new String("red").equals(args[1])) { tm.redTeam.setTeamFlag(player.getTargetBlock(null, 50).getLocation()); }
				if (new String("blue").equals(args[1])) { tm.blueTeam.setTeamFlag(player.getTargetBlock(null, 50).getLocation()); }
				if (new String("yellow").equals(args[1])) { tm.yellowTeam.setTeamFlag(player.getTargetBlock(null, 50).getLocation()); }
				if (new String("green").equals(args[1])) { tm.greenTeam.setTeamFlag(player.getTargetBlock(null, 50).getLocation()); }
				sender.sendMessage("[CutePVP] Set flag loc");
				return true;
			}
			if (args.length == 2 && new String("setflaghome").equals(args[0])) {
				if (new String("red").equals(args[1])) { tm.redTeam.setTeamFlagHome(player.getTargetBlock(null, 50).getLocation()); }
				if (new String("blue").equals(args[1])) { tm.blueTeam.setTeamFlagHome(player.getTargetBlock(null, 50).getLocation()); }
				if (new String("yellow").equals(args[1])) { tm.yellowTeam.setTeamFlagHome(player.getTargetBlock(null, 50).getLocation()); }
				if (new String("green").equals(args[1])) { tm.greenTeam.setTeamFlagHome(player.getTargetBlock(null, 50).getLocation()); }
				sender.sendMessage("[CutePVP] Set flag home loc");
				return true;
			}
		}
		/*else if (command.getName().equalsIgnoreCase("fpos")) {
			if (args.length == 0) {
				if (fposSet.containsKey(sender.getName())) {
					fposSet.remove(sender.getName());
					return true;
				}
				return false;
			}
			if (woolColorByName(args[0]) == 0) {
				System.out.println("Shit doesn't work.");
				return false;
			}
			fposSet.put(sender.getName(), args[0]);
			sender.sendMessage(ChatColor.GREEN + "You are now setting the " + args[0] + " flag.");

			return true;
		}*/
		return false;
	}

	/* No longer needed
	public int getTeam(String inpt) {
		int value = Character.getNumericValue(ChatColor.stripColor(inpt).charAt(inpt.length()-1));
		for(int i: new int[] { 10, 11, 23, 30, 33 } ) {
			if(value == i) return 0;
		}
		for(int i: new int[] { 14, 17, 28, 21, 29 } ) {
			if(value == i) return 1;
		}
		for(int i: new int[] { 12, 13, 15, 18, 25, 31, 32 } ) {
			if(value == i) return 2;
		}
		for(int i: new int[] { 0, 16, 19, 20, 22, 24, 27, 34, 35 } ) {
			if(value == i) return 3;
		}
		return 0;
	}

	public Location getRespawnTeamLocation(String playerName) {
		String team = teamName(playerName);
		return getRespawnTeamLocationByTeam(team);
	}

	public String woolColorToTeamName(short metadata) {
		switch(metadata) {
		case 14:
			return "red";
		case 3:
			return "blue";
		case 4:
			return "yellow";
		case 5:
			return "green";
		}
		return null;
	}
	public int teamNameToWoolColor(String team) {
		if ("red".equals(team))    return 14;
		if ("blue".equals(team))   return 3;
		if ("yellow".equals(team)) return 4;
		if ("green".equals(team))  return 5;
		return 0;
	}

	public Location getRespawnTeamLocationByTeam(String teamName) {
		return new Location(
				getServer().getWorlds().get(0),
				getConfig().getInt("spawn." + teamName + ".x"),
				getConfig().getInt("spawn." + teamName + ".y"),
				getConfig().getInt("spawn." + teamName + ".z"));
	}

	public short woolColor(String inpt) {
		return (short)teamNameToWoolColor(teamName(inpt));
	}

	public byte woolColorByName(String inpt) {
		if (inpt.equalsIgnoreCase("red")) {
			return 14;
		} else if (inpt.equalsIgnoreCase("blue")) {
			return 3;
		} else if (inpt.equalsIgnoreCase("yellow")) {
			return 4;
		} else if (inpt.equalsIgnoreCase("green")) {
			return 5;
		}
		return 0;
	}

	public String teamName(String inpt) {
		int ret = getTeam(inpt);
		return teamNameFromInt(ret);
	}

	public String teamNameFromInt(int ret) {
		switch (ret) {
		case 0: return "red";
		case 1: return "blue";
		case 2: return "yellow";
		case 3: return "green";
		}
		return "";
	}

	public ItemStack returnWool(String inpt) {
		return new ItemStack(Material.WOOL.getId(), 1, woolColor(inpt));
	}

	public String colorName(String inpt) {
		int ret = getTeam(inpt);
		String retName = "";
		switch (ret) {
		case 0: retName += ChatColor.RED;
		break;
		case 1: retName += ChatColor.BLUE;
		break;
		case 2: retName += ChatColor.YELLOW;
		break;
		case 3: retName += ChatColor.GREEN;
		break;
		}
		retName += (inpt + ChatColor.WHITE);
		return retName;
	}

	public int isFlagBlock(int x, int y, int z) {
		for(int i=0; i<4; i++) {
			String teamName = teamNameFromInt(i);
			if( getConfig().getInt("ctf." + teamName + ".x") == x &&
					getConfig().getInt("ctf." + teamName + ".y") == y &&
					getConfig().getInt("ctf." + teamName + ".z") == z) {

				return i;

			}
		}
		return -1;
	}

	public void respawnFlags() {
		for(int i=0; i<4; i++) {
			String teamName = teamNameFromInt(i);
			Block flag_block = getServer().getWorlds().get(0).getBlockAt(
					getConfig().getInt("flag." + teamName + ".x"),
					getConfig().getInt("flag." + teamName + ".y"),
					getConfig().getInt("flag." + teamName + ".z"));
			flag_block.setTypeIdAndData(35, woolColorByName(teamName), false);
		}
	}

	String carrierFor(Player player) {
		if (player.getName().equalsIgnoreCase(getConfig().getString("ctf.red.carrier"))) {
			return "red";
		}
		if (player.getName().equalsIgnoreCase(getConfig().getString("ctf.blue.carrier"))) {
			return "blue";
		}
		if (player.getName().equalsIgnoreCase(getConfig().getString("ctf.yellow.carrier"))) {
			return "yellow";
		}
		if (player.getName().equalsIgnoreCase(getConfig().getString("ctf.green.carrier"))) {
			return "green";
		}
		return null;
	}

	void capForTeam(String team, String teamCap) {
		getConfig().set("count." + team, getConfig().getInt("count." + team) + 1);
		returnFlag(teamCap);
		setFlagCarrier(teamCap, null);
		saveConfig();
	}

	void killForTeam(String team) {
		getConfig().set("kills." + team, getConfig().getInt("kills." + team) + 1);
		saveConfig();
	}

	void messageCap(String capTeam, String cappedTeam) {
		getServer().broadcastMessage(String.format("%sTeam %s just capped the %s flag", ChatColor.GREEN, capTeam, cappedTeam));
	}

	void returnFlag(String woolTeamName) {
		Location flag = getTeamFlagLoc(woolTeamName);
		Location flagSpawn = getTeamFlagSpawnLoc(woolTeamName);

		if (flag != null) {
			getServer().getWorlds().get(0).getBlockAt(flag).setType(Material.AIR);
		}else
		{
			System.out.println("Flag doesn't exist?");
		}

		if (flagSpawn != null) {
			getServer().getWorlds().get(0).getBlockAt(flagSpawn).setTypeIdAndData(35, (byte)woolColorByName(woolTeamName), true);
			setTeamFlagLoc(woolTeamName, getTeamFlagSpawnLoc(woolTeamName));
		}
		else {
			System.out.println("Flag Spawn doesn't exist?");
		}

		setFlagCarrier(woolTeamName, null);
	}

	public void teamChat(String team, String message) {
		for (Player playeri : getServer().getOnlinePlayers()) {
			if (teamName(playeri.getName()).equalsIgnoreCase(team)) {
				playeri.sendMessage(message);
			}
		}
	}

	//Flag carrier of <team>'s flag
	public String getFlagCarrier(String team) {
		return getConfig().getString("ctf." + team.trim() + ".carrier");
	}
	public void setFlagCarrier(String team, String player) {
		getConfig().set("ctf." + team.trim() + ".carrier", player);
		saveConfig();
	}

	void takeFlag(String woolTeamName, Player player) {
		String c = carrierFor(player);

		if (c == null) {
			setFlagCarrier(woolTeamName, player.getName());
			Block b = getServer().getWorlds().get(0).getBlockAt(getTeamFlagLoc(woolTeamName));
			b.setType(Material.AIR);
			getServer().broadcastMessage(player.getDisplayName() + " has taken the " + woolTeamName + " flag.");

			if (dropTime.containsKey(woolTeamName)) {
				dropTime.remove(woolTeamName);
			}
		} else {
			player.sendMessage(ChatColor.RED + "You can only take one flag at a time!");
		}
	}

	void dropFlag(String carrierFor, Player player) {
		if (carrierFor != null) {
			setTeamFlagLoc(carrierFor, player.getLocation().add(0, 1, 0));
			getServer().getWorlds().get(0).getBlockAt(getTeamFlagLoc(carrierFor)).setTypeIdAndData(35, (byte)woolColorByName(carrierFor), true);
			setFlagCarrier(carrierFor, null);
			dropTime.put(carrierFor, System.currentTimeMillis());
			getServer().broadcastMessage(ChatColor.GREEN + player.getName() + " has droppped the " + carrierFor + " flag.");
		}
		else {
		}
	}
	*/

}