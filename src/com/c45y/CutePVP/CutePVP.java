package com.c45y.CutePVP;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

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

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

public class CutePVP extends JavaPlugin {
	private final CutePVPListener loglistener = new CutePVPListener(this);
	HashMap<String, String> fposSet = new HashMap<String, String>();
	TeamManager tm;
	WorldGuardPlugin wgPlugin = null;

	public WorldGuardPlugin getWorldGuard() {
		if (wgPlugin == null) {
			wgPlugin = (WorldGuardPlugin) getServer().getPluginManager().getPlugin("WorldGuard");
			if (wgPlugin != null) {
				if (!wgPlugin.isEnabled()) {
					getPluginLoader().enablePlugin(wgPlugin);
				}
			} else {
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
		getLogger().info(
			"Red:" + redPlayerNames.size() + " Blue:" + bluePlayerNames.size() + " Yellow:" + yellowPlayerNames.size() + " Green:"
							+ greenPlayerNames.size());
	}

	public void savePlayers() {
		getConfig().set("teams.red.players", new ArrayList<String>(tm.redTeam.getTeamMembers()));
		getConfig().set("teams.blue.players", new ArrayList<String>(tm.blueTeam.getTeamMembers()));
		getConfig().set("teams.yellow.players", new ArrayList<String>(tm.yellowTeam.getTeamMembers()));
		getConfig().set("teams.green.players", new ArrayList<String>(tm.greenTeam.getTeamMembers()));
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
				getLogger().info("Running buff");
				Location powerblock = new Location(getServer().getWorlds().get(0), getConfig().getDouble("block.buff.x"), getConfig().getDouble(
					"block.buff.y"), getConfig().getDouble("block.buff.z"));
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
				if (tm.redTeam.flagHolder != null)
					tm.redTeam.setTeamFlag(tm.redTeam.flagHolder.getLocation());

				if (tm.blueTeam.flagHolder != null)
					tm.blueTeam.setTeamFlag(tm.blueTeam.flagHolder.getLocation());

				if (tm.greenTeam.flagHolder != null)
					tm.greenTeam.setTeamFlag(tm.greenTeam.flagHolder.getLocation());

				if (tm.yellowTeam.flagHolder != null)
					tm.yellowTeam.setTeamFlag(tm.yellowTeam.flagHolder.getLocation());

				tm.blueTeam.setCompassTarget();
				tm.redTeam.setCompassTarget();
				tm.yellowTeam.setCompassTarget();
				tm.greenTeam.setCompassTarget();

				saveConfig();
			}
		}, 5 * 20, 5 * 20);
	}

	public void onDisable() {
		savePlayers();
		System.out.println(this.toString() + " disabled");
	}

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
				playeri.sendMessage(ChatColor.RED + ">" + ChatColor.BLUE + ">" + ChatColor.GREEN + ">" + ChatColor.YELLOW + ">" + ChatColor.WHITE
								+ " <" + tm.getTeamMemberOf(sender.getName()).encodeTeamColor(sender.getName()) + "> " + str);
			}
			return true;
		} else if (command.getName().equalsIgnoreCase("cutepvp")) {
			if (args.length == 1 && new String("reload").equals(args[0])) {
				// loadPlayers();
				return true;
			}
			if (args.length == 1 && new String("save").equals(args[0])) {
				savePlayers();
				sender.sendMessage("[CutePVP] Saved");
				return true;
			}
			if (args.length == 1 && args[0].equalsIgnoreCase("setbuff")) {
				Block block = player.getLocation().getBlock().getRelative(BlockFace.DOWN);
				player.sendMessage("Set to: " + block.getType().name());
				getConfig().set("block.buff.x", block.getLocation().getX());
				getConfig().set("block.buff.y", block.getLocation().getY());
				getConfig().set("block.buff.z", block.getLocation().getZ());
				saveConfig();
			}
			if (args.length == 2 && new String("rmplayer").equals(args[0])) {
				String playerName = args[1];
				Team team = tm.getTeamMemberOf(playerName);
				team.removePlayer(playerName);
				sender.sendMessage("[CutePVP] Removed Player");
				return true;
			}
			if (args.length == 2 && new String("setspawn").equals(args[0])) {
				if (new String("red").equals(args[1])) {
					tm.redTeam.setTeamSpawn(player.getLocation());
				}
				if (new String("blue").equals(args[1])) {
					tm.blueTeam.setTeamSpawn(player.getLocation());
				}
				if (new String("yellow").equals(args[1])) {
					tm.yellowTeam.setTeamSpawn(player.getLocation());
				}
				if (new String("green").equals(args[1])) {
					tm.greenTeam.setTeamSpawn(player.getLocation());
				}
				sender.sendMessage("[CutePVP] Set Spawn");
				return true;
			}
			if (args.length == 2 && new String("setflag").equals(args[0])) {
				if (new String("red").equals(args[1])) {
					tm.redTeam.setTeamFlag(player.getTargetBlock(null, 50).getLocation());
				}
				if (new String("blue").equals(args[1])) {
					tm.blueTeam.setTeamFlag(player.getTargetBlock(null, 50).getLocation());
				}
				if (new String("yellow").equals(args[1])) {
					tm.yellowTeam.setTeamFlag(player.getTargetBlock(null, 50).getLocation());
				}
				if (new String("green").equals(args[1])) {
					tm.greenTeam.setTeamFlag(player.getTargetBlock(null, 50).getLocation());
				}
				sender.sendMessage("[CutePVP] Set flag loc");
				return true;
			}
			if (args.length == 2 && new String("setflaghome").equals(args[0])) {
				if (new String("red").equals(args[1])) {
					tm.redTeam.setTeamFlagHome(player.getTargetBlock(null, 50).getLocation());
				}
				if (new String("blue").equals(args[1])) {
					tm.blueTeam.setTeamFlagHome(player.getTargetBlock(null, 50).getLocation());
				}
				if (new String("yellow").equals(args[1])) {
					tm.yellowTeam.setTeamFlagHome(player.getTargetBlock(null, 50).getLocation());
				}
				if (new String("green").equals(args[1])) {
					tm.greenTeam.setTeamFlagHome(player.getTargetBlock(null, 50).getLocation());
				}
				sender.sendMessage("[CutePVP] Set flag home loc");
				return true;
			}
			if (args.length == 1 && new String("teams").equals(args[0])) {
				sender.sendMessage("[CutePVP] Online: Red=" + tm.redTeam.getTeamMembersOnline().size() + " Blue="
								+ tm.blueTeam.getTeamMembersOnline().size() + " Yellow=" + tm.yellowTeam.getTeamMembersOnline().size() + " Green="
								+ tm.greenTeam.getTeamMembersOnline().size());
				sender.sendMessage("[CutePVP] Total: Red=" + tm.redTeam.getTeamMembers().size() + " Blue=" + tm.blueTeam.getTeamMembers().size()
								+ " Yellow=" + tm.yellowTeam.getTeamMembers().size() + " Green=" + tm.greenTeam.getTeamMembers().size());
				return true;
			}
		} else if (command.getName().equalsIgnoreCase("list")) {
			String message = "Red Team:";
			for (String playerName : tm.redTeam.getTeamMembersOnline()) {
				message += " " + playerName;
			}
			sender.sendMessage(message);
			message = "Blue Team:";
			for (String playerName : tm.blueTeam.getTeamMembersOnline()) {
				message += " " + playerName;
			}
			sender.sendMessage(message);
			message = "Yellow Team:";
			for (String playerName : tm.yellowTeam.getTeamMembersOnline()) {
				message += " " + playerName;
			}
			sender.sendMessage(message);
			message = "Green Team:";
			for (String playerName : tm.greenTeam.getTeamMembersOnline()) {
				message += " " + playerName;
			}
			sender.sendMessage(message);
			return true;
		} else if (command.getName().equalsIgnoreCase("score")) {
			sender.sendMessage("Score: " + tm.redTeam.encodeTeamColor("Red=" + tm.redTeam.getTeamScore())
							+ tm.blueTeam.encodeTeamColor(" Blue=" + tm.blueTeam.getTeamScore())
							+ tm.yellowTeam.encodeTeamColor(" Yellow=" + tm.yellowTeam.getTeamScore())
							+ tm.greenTeam.encodeTeamColor(" Green=" + tm.greenTeam.getTeamScore()));
			return true;
		} else if (command.getName().equalsIgnoreCase("drop")) {
			Team flagOf = tm.isFlagBearer(player);
			if (flagOf != null) {
				flagOf.dropTeamFlag(player.getLocation().getBlock().getLocation());
			}
			return true;
		} else if (command.getName().equalsIgnoreCase("flag")) {
			Team team = tm.getTeamMemberOf(player.getName());
			Location flagLoc = team.getTeamFlag();
			if (flagLoc.equals(team.getTeamFlagHome()))
				sender.sendMessage("Flag Location: Home");
			else
				sender.sendMessage("Flag Location: " + (int) flagLoc.getX() + ", " + (int) flagLoc.getY() + ", " + (int) flagLoc.getZ());
			return true;
		}

		return false;
	}
}