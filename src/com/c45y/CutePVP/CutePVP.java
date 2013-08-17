package com.c45y.CutePVP;

import java.util.List;
import java.util.logging.Level;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.c45y.CutePVP.buff.BuffManager;
import com.c45y.CutePVP.buff.TeamBuff;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

// ----------------------------------------------------------------------------
/**
 * Plugin class.
 */
public class CutePVP extends JavaPlugin {
	/**
	 * The number of ticks in one second.
	 */
	public static final int ONE_SECOND_TICKS = 20;

	/**
	 * The number of ticks in one minute.
	 */
	public static final int ONE_MINUTE_TICKS = 60 * ONE_SECOND_TICKS;

	// ------------------------------------------------------------------------
	/**
	 * Return a reference to the WorldGuard plugin and enable it if necessary.
	 * 
	 * @return a reference to the WorldGuard plugin and enable it if necessary.
	 */
	public WorldGuardPlugin getWorldGuard() {
		if (_worldGuard == null) {
			_worldGuard = (WorldGuardPlugin) getServer().getPluginManager().getPlugin("WorldGuard");
			if (_worldGuard != null) {
				if (!_worldGuard.isEnabled()) {
					getPluginLoader().enablePlugin(_worldGuard);
				}
			} else {
				getLogger().log(Level.INFO, "Could not load worldguard, disabling");
				_worldGuard = null;
			}
		}
		return _worldGuard;
	}

	// ------------------------------------------------------------------------
	/**
	 * Return the {@link TeamManager}.
	 * 
	 * @return the {@link TeamManager}.
	 */
	public TeamManager getTeamManager() {
		return _teamManager;
	}

	// ------------------------------------------------------------------------

	/**
	 * Return the {@link BuffManager}.
	 * 
	 * @return the {@link BuffManager}.
	 */
	public BuffManager getBuffManager() {
		return _buffManager;
	}

	// ------------------------------------------------------------------------
	/**
	 * Called when the plugin is enabled.
	 */
	@Override
	public void onEnable() {
		saveDefaultConfig();
		loadConfiguration();

		getServer().getPluginManager().registerEvents(_listener, this);

		getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			public void run() {
				getBuffManager().applyTeamBuffs(_teamBuffTicks);
				for (Team team : getTeamManager()) {
					for (Flag flag : team.getFlags()) {
						flag.checkReturn(_flagReturnTicks);
					}
				}
			}
		}, 0, 30 * ONE_SECOND_TICKS);

		getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			public void run() {
				for (Team team : getTeamManager()) {
					team.updateCompasses();
				}
				// saveConfig();

				for (Player player : Bukkit.getOnlinePlayers()) {
					TeamPlayer teamPlayer = getTeamManager().getTeamPlayer(player);
					if (teamPlayer != null) {
						updateTeamPlayerEffects(teamPlayer);
					}
				}
			}
		}, 0, 5 * ONE_SECOND_TICKS);
	}

	// ------------------------------------------------------------------------
	/**
	 * Called when the plugin is disabled.
	 */
	@Override
	public void onDisable() {
		saveConfiguration();
		_worldGuard = null;
	}

	// ------------------------------------------------------------------------
	/**
	 * Apply effects to the player state based on the block the player is
	 * standing on and whether they are carrying a flag.
	 * 
	 * @param teamPlayer the non-null TeamPlayer who is affected.
	 */
	public void updateTeamPlayerEffects(TeamPlayer teamPlayer) {
		Location loc = teamPlayer.getPlayer().getLocation();
		if (teamPlayer.isCarryingFlag()) {
			loc.getWorld().playEffect(loc, Effect.MOBSPAWNER_FLAMES, 1);
		}

		Block block = loc.getBlock().getRelative(BlockFace.DOWN);
		getBuffManager().applyFloorBuff(block, teamPlayer);
	}

	// ------------------------------------------------------------------------
	/**
	 * Commands:
	 * <ul>
	 * <li>/g <message> - global chat</li>
	 * <li>/teams - list all teams</li>
	 * <li>/score - show scores</li>
	 * <li>/flag - give coordinates of nearest flag</li>
	 * <li>/drop - drop flag if carrying</li>
	 * </ul>
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("Sorry, this plugin cannot be used from console");
			return true;
		}
		Player player = (Player) sender;
		if (command.getName().equalsIgnoreCase("g")) {
			String message = ChatColor.RED + ">" + ChatColor.BLUE + ">" +
								ChatColor.GREEN + ">" + ChatColor.YELLOW + ">" + ChatColor.WHITE
								+ " <" + player.getDisplayName() + "> " + StringUtils.join(args, " ");
			for (Player recipient : getServer().getOnlinePlayers()) {
				recipient.sendMessage(message);
			}
			return true;

		} else if (command.getName().equalsIgnoreCase("teams")) {
			getTeamManager().sendList(player);
			return true;

		} else if (command.getName().equalsIgnoreCase("score")) {
			for (Team team : getTeamManager()) {
				sender.sendMessage(team.encodeTeamColor(team.getName()) + " :-" + team.getScore());
			}
			TeamPlayer teamPlayer = getTeamManager().getTeamPlayer(player);
			if (teamPlayer != null) {
				sender.sendMessage(player.getDisplayName() + " :-" + teamPlayer.getScore());
			}
			return true;
		} else if (command.getName().equalsIgnoreCase("flag")) {
			TeamPlayer teamPlayer = getTeamManager().getTeamPlayer(player);
			if (teamPlayer != null) {
				Flag flag = teamPlayer.getTeam().getNearestFlag(player);
				if (flag.isHome()) {
					Messages.success(player, null, "Flag Location: Home");
				} else {
					sender.sendMessage("Flag Location: " + Messages.formatIntegerXYZ(flag.getLocation()));
				}
			}
			return true;
		} else if (command.getName().equalsIgnoreCase("drop")) {
			TeamPlayer teamPlayer = getTeamManager().getTeamPlayer(player);
			if (teamPlayer != null && teamPlayer.getCarriedFlag() != null) {
				Messages.failure(player, null, "Flag dropped.");
				teamPlayer.getCarriedFlag().drop();
			} else {
				Messages.failure(player, null, "You're not carrying a flag.");
			}
			return true;
		} else if (command.getName().equals("cutepvp")) {
			return handleCutePvPCommand(sender, args);
		}

		return false;
	} // onCommand

	// ------------------------------------------------------------------------
	/**
	 * Handle the /cutepvp command.
	 * 
	 * <ul>
	 * <li>/cutepvp reload|save - reload/save configuration</li>
	 * <li>/cutepvp setspawn &lt;team&gt; - set spawn of team</li>
	 * <li>/cutepvp flag list - list flag locations</li>
	 * <li>/cutepvp flag set &lt;team&gt; &lt;id&gt; - set the location of the
	 * flag with the specified ID.</li>
	 * <li>/cutepvp buff list - list team buff locations</li>
	 * <li>/cutepvp buff set &lt;id&gt; - set the location of the buff with the
	 * specified ID.</li>
	 * </ul>
	 * 
	 * @param sender the CommandSender.
	 * @param args the command arguments.
	 * @return true if the command was handled.
	 */
	protected boolean handleCutePvPCommand(CommandSender sender, String[] args) {
		if (args.length == 1) {
			if (args[0].equals("reload")) {
				loadConfiguration();
				Messages.success(sender, Messages.PREFIX, "Configuration reloaded.");
				return true;
			} else if (args[0].equals("save")) {
				saveConfiguration();
				Messages.success(sender, Messages.PREFIX, "Configuration saved.");
				return true;
			}
		} else if (args.length == 2) {
			if (args[0].equals("setspawn")) {
				String teamId = args[1];
				Team team = getTeamManager().getTeam(teamId);
				if (team == null) {
					Messages.failure(sender, Messages.PREFIX, teamId + " is not a valid team ID.");
				} else if (sender instanceof Player) {
					Messages.success(sender, Messages.PREFIX, team.getName() + " spawn set.");
					team.setSpawn(((Player) sender).getLocation());
				} else {
					Messages.failure(sender, Messages.PREFIX, " You need to be in game to set team spawns.");
				}
				return true;
			} else if (args[0].equals("flag") && args[1].equals("list")) {
				for (Team team : getTeamManager()) {
					StringBuilder message = new StringBuilder();
					message.append(team.getName()).append(' ').append("flags: ");
					for (Flag flag : team.getFlags()) {
						message.append(" ").append(flag.getId()).append(" \"").append(flag.getName());
						message.append("\" @ ").append(Messages.formatIntegerXYZ(flag.getLocation()));
					}
					Messages.success(sender, Messages.PREFIX, message.toString());
				}
				return true;
			} else if (args[0].equals("buff") && args[1].equals("list")) {
				StringBuilder message = new StringBuilder();
				message.append("Team buffs: ");
				for (TeamBuff teamBuff : getBuffManager()) {
					message.append(" ").append(teamBuff.getId()).append(" \"").append(teamBuff.getName());
					message.append("\" @ ").append(Messages.formatIntegerXYZ(teamBuff.getLocation()));
				}
				Messages.success(sender, Messages.PREFIX, message.toString());
				return true;
			}
		} else if (args.length == 3) {
			// /cutepvp buff set <buffId>
			if (args[0].equals("buff") && args[1].equals("set")) {
				String buffId = args[2];
				TeamBuff teamBuff = getBuffManager().getTeamBuff(buffId);
				if (teamBuff == null) {
					Messages.failure(sender, Messages.PREFIX, "There is no team buff with that ID.");
				} else {
					if (sender instanceof Player) {
						Player player = (Player) sender;
						List<Block> inSight = player.getLastTwoTargetBlocks(null, 50);
						Block target = inSight.get(1);
						teamBuff.setLocation(target.getLocation());
						Messages.success(sender, Messages.PREFIX,
							"Team buff " + teamBuff.getId() + " (\"" + teamBuff.getName() + "\") set to " +
								target.getType().name().toLowerCase() + " at " +
								Messages.formatIntegerXYZ(teamBuff.getLocation()));
					} else {
						Messages.failure(sender, Messages.PREFIX, "You need to be in game to set team buff locations.");
					}
				}
				return true;
			}
		} else if (args.length == 4) {
			// /cutepvp flag set <teamId> <flagId>
			if (args[0].equals("flag") && args[1].equals("set")) {
				String teamId = args[2];
				Team team = getTeamManager().getTeam(teamId);
				if (team == null) {
					Messages.failure(sender, Messages.PREFIX, teamId + " is not a valid team ID.");
				} else {
					String flagId = args[3];
					Flag flag = team.getFlag(flagId);
					if (flag == null) {
						Messages.failure(sender, Messages.PREFIX, team.getName() + " doesn't have a flag with the ID " + flagId + ".");
					} else {
						if (sender instanceof Player) {
							Player player = (Player) sender;
							List<Block> inSight = player.getLastTwoTargetBlocks(null, 50);
							Block target = inSight.get(1);
							if (team.isTeamBlock(target)) {
								flag.setHomeLocation(target.getLocation());
								Messages.success(sender, Messages.PREFIX,
									team.getName() + " flag " + flag.getId() + " (\"" + flag.getName() + "\") home set to " +
									Messages.formatIntegerXYZ(flag.getLocation()));

							} else {
								Messages.failure(sender, Messages.PREFIX, "The flag needs to be of the team's block type.");
							}
						} else {
							Messages.failure(sender, Messages.PREFIX, "You need to be in game to set flags.");
						}
					}
				}
				return true;
			} // handling /cutepvp flag set <teamId> <flagId>
		}
		return false;
	} // handleCutePvPCommand

	// ------------------------------------------------------------------------
	/**
	 * Load all configuration.
	 */
	protected void loadConfiguration() {
		getTeamManager().load();
		getBuffManager().load();

		_flagReturnTicks = getConfig().getInt("misc.flag_return_ticks", 5 * ONE_MINUTE_TICKS);
		_teamBuffTicks = getConfig().getInt("misc.team_buff_ticks", 30 * ONE_MINUTE_TICKS);
	}

	// ------------------------------------------------------------------------
	/**
	 * Save all configuration.
	 */
	protected void saveConfiguration() {
		getTeamManager().save();
		getBuffManager().save();

		getConfig().set("misc.flag_return_ticks", _flagReturnTicks);
		getConfig().set("misc.team_buff_ticks", _teamBuffTicks);
		saveConfig();
	}

	// ------------------------------------------------------------------------
	/**
	 * Reference to the WorldGuard plugin.
	 */
	private WorldGuardPlugin _worldGuard = null;

	/**
	 * Event listener.
	 */
	private CutePVPListener _listener = new CutePVPListener(this);

	/**
	 * The {@link TeamManager}.
	 */
	private TeamManager _teamManager = new TeamManager(this);

	/**
	 * The {@link BuffManager}.
	 */
	private BuffManager _buffManager = new BuffManager(this);

	/**
	 * Number of ticks before a dropped flag is automatically returned.
	 */
	int _flagReturnTicks;

	/**
	 * The total number of ticks from the time a team buff is claimed to when it
	 * expires.
	 */
	int _teamBuffTicks;
} // class CutePVP