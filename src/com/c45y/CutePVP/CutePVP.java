package com.c45y.CutePVP;

import java.util.List;
import java.util.logging.Level;

import com.c45y.CutePVP.alert.AlertManager;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.c45y.CutePVP.buff.BuffManager;
import com.c45y.CutePVP.buff.TeamBuff;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

import java.util.HashSet;

// ----------------------------------------------------------------------------
/**
 * Plugin class.
 */
public class CutePVP extends JavaPlugin {
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
	 * Return the {@link ScoreboardManager}.
	 *
	 * @return the {@link ScoreboardManager}.
	 */
	public ScoreboardManager getScoreboardManager() {
		return _scoreboardManager;
	}

	// ------------------------------------------------------------------------

	/**
	 * Return the {@link AlertManager}.
	 *
	 * @return the {@link AlertManager}.
	 */
	public AlertManager getAlertManager() {
		return _alertManager;
	}

	// ------------------------------------------------------------------------

	/**
	 * Return the {@link Configuration}.
	 * 
	 * @return the {@link Configuration}.
	 */
	public Configuration getConfiguration() {
		return _configuration;
	}

	// ------------------------------------------------------------------------
	/**
	 * Called when the plugin is enabled.
	 */
	@Override
	public void onEnable() {
		saveDefaultConfig();
		getConfiguration().load();

		getServer().getPluginManager().registerEvents(_listener, this);

		getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			public void run() {
				getBuffManager().applyTeamBuffs(getConfiguration().TEAM_BUFF_SECONDS);
				for (Team team : getTeamManager()) {
					for (Flag flag : team.getFlags()) {
						flag.checkReturnDropped(getConfiguration().FLAG_DROPPED_SECONDS);
					}
				}
			}
		}, 0, 30 * Constants.ONE_SECOND_TICKS);

		getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			public void run() {
				for (Team team : getTeamManager()) {
					team.updateCompasses();
				}

				for (Player player : Bukkit.getOnlinePlayers()) {
					TeamPlayer teamPlayer = getTeamManager().getTeamPlayer(player);
					if (teamPlayer != null) {
						// Handle inventory clearing in The End.
						if (getConfiguration().CHECK_HELMET) {
							teamPlayer.getTeam().setTeamAttributes(player);
						}

						// Only apply block buffs in the Overworld, not the
						// minigames area in The End.
						if (isInMatchArea(player)) {
							applyFloorBuffs(teamPlayer);
						}
					}
				}
			}
		}, 0, 5 * Constants.ONE_SECOND_TICKS);

		getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			public void run() {
				for (Team team : getTeamManager()) {
					for (Flag flag : team.getFlags()) {
						// Play flames at the flag location.
						Location loc = flag.getLocation();
						loc.getWorld().playEffect(loc, Effect.MOBSPAWNER_FLAMES, 1);

						// Check for attempts to piston the flag out of position
						// so that it can't be clicked.
						if (!flag.isCarried() && !flag.getTeam().isTeamBlock(loc.getBlock())) {
							flag.doReturn();
							Messages.broadcast(Messages.BROADCAST_COLOR + flag.getTeam().getName() + "'s stolen " +
												flag.getName() + " flag was destroyed and returned home.");
							if (getConfiguration().FLAG_RETURN_SOUND != null) {
								loc.getWorld().playSound(loc, getConfiguration().FLAG_RETURN_SOUND, Constants.SOUND_RANGE, 1);
							}
						}

						// Check for and do automatic return of flags that are
						// stolen but not captured after too long.
						if (!flag.isHome() && flag.checkReturnStolen()) {
							if (getConfiguration().FLAG_RETURN_SOUND != null) {
								loc.getWorld().playSound(loc, getConfiguration().FLAG_RETURN_SOUND, Constants.SOUND_RANGE, 1);
							}
						}
					}
				}
			}
		}, 0, getConfiguration().FLAG_FLAME_TICKS);

		getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			// Save the configuration, with backups at 10 minute intervals.
			// Log team membership and total scores.
			@Override
			public void run() {
				getConfiguration().save();
				for (Team team : getTeamManager()) {
					getLogger().info(team.getName() + " score: " + ChatColor.stripColor(team.getScore().toString()));
				}
				for (Team team : getTeamManager()) {
					int onlineSize = team.getOnlineMembers().size();
					int totalSize = team.getMembers().size();
					getLogger().info(team.getName() + " " + onlineSize + " of " + totalSize + " online: " + team.getOnlineList());
				}
			}
		}, 0, 10 * Constants.ONE_MINUTE_TICKS);

		if (getConfiguration().SCOREBOARD_ENABLE) {
			_scoreboardManager.enable();
		}
	} // onEnable

	// ------------------------------------------------------------------------
	/**
	 * Called when the plugin is disabled.
	 */
	@Override
	public void onDisable() {
		getConfiguration().save();
		_worldGuard = null;
	}

	// ------------------------------------------------------------------------
	/**
	 * Return true if the player is in the area where CutePvP match rules apply.
	 *
	 * @return true if the player is in the area where CutePvP match rules
	 *         apply.
	 */
	public boolean isInMatchArea(Player player) {
		return player.getWorld().getName().equals(getConfiguration().MATCH_WORLD);
	}

	// ------------------------------------------------------------------------
	/**
	 * Apply effects to the player state based on the block the player is
	 * standing on.
	 * 
	 * @param teamPlayer the non-null TeamPlayer who is affected.
	 */
	public void applyFloorBuffs(TeamPlayer teamPlayer) {
		// Player should NOT be null, but server gets confused by ModMode.
		Player player = teamPlayer.getPlayer();
		if (player != null) {
			Location loc = player.getLocation();

			// If the player is standing on certain low height blocks (carpets,
			// soul sand) then the block he is standing on is the one containing
			// his legs, rather than that below. For efficiency, we don't
			// support every case (e.g. not cake or slabs or enchanting tables).
			Block legBlock = loc.getBlock();
			if (legBlock.getType() == Material.SOUL_SAND || legBlock.getType() == Material.CARPET) {
				getBuffManager().applyFloorBuff(legBlock, teamPlayer);
			} else {
				Block floorBlock = legBlock.getRelative(BlockFace.DOWN);
				getBuffManager().applyFloorBuff(floorBlock, teamPlayer);
			}
		}
	}

	// ------------------------------------------------------------------------
	/**
	 * Commands:
	 * <ul>
	 * <li>/join - join a team and teleport to that team's base.</li>
	 * <li>/g &lt;message&gt; - global chat</li>
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
		if (command.getName().equalsIgnoreCase("join")) {
			join(player);
			return true;
		} else if (command.getName().equalsIgnoreCase("g")) {
			String message = ChatColor.RED + ">" + ChatColor.BLUE + ">" +
								ChatColor.GREEN + ">" + ChatColor.YELLOW + ">" + ChatColor.WHITE +
								" <" + player.getDisplayName() + "> " +
								getTeamManager().highlightTeamMemberNames(StringUtils.join(args, " "));
			for (Player recipient : getServer().getOnlinePlayers()) {
				recipient.sendMessage(message);
			}
			return true;
		} else if (command.getName().equalsIgnoreCase("teams")) {
			getTeamManager().sendTeamLists(player);
			return true;

		} else if (command.getName().equalsIgnoreCase("score")) {
			for (Team team : getTeamManager()) {
				sender.sendMessage(team.encodeTeamColor(team.getName()) + ":");
				sender.sendMessage(team.getScore().toString());
			}
			TeamPlayer teamPlayer = getTeamManager().getTeamPlayer(player);
			if (teamPlayer != null) {
				sender.sendMessage(player.getDisplayName() + ":");
				sender.sendMessage(teamPlayer.getScore().toString());
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
				Messages.success(player, null, "Flag dropped.");
				teamPlayer.getCarriedFlag().drop();
			} else {
				Messages.failure(player, null, "You're not carrying a flag.");
			}
			return true;
		} else if (command.getName().equalsIgnoreCase("testblock")) {
			TeamPlayer teamPlayer = getTeamManager().getTeamPlayer(player);
			if (teamPlayer != null) {
				teamPlayer.setTestingFloorBuffs(!teamPlayer.isTestingFloorBuffs());
				Messages.success(player, null, "Power block testing is now " + (teamPlayer.isTestingFloorBuffs() ? "enabled." : "disabled."));
			} else {
				Messages.failure(player, null, "You're not on a team.");
			}
			return true;
		} else if (command.getName().equals("cutepvp")) {
			if (!handleCutePvPCommand(sender, args)) {
				sender.sendMessage("CutePVP Admin Commands:");
				sender.sendMessage("/cutepvp save");
				sender.sendMessage("/cutepvp setspawn <team>");
				sender.sendMessage("/cutepvp flag list");
				sender.sendMessage("/cutepvp flag set <team> <id>");
				sender.sendMessage("/cutepvp buff list");
				sender.sendMessage("/cutepvp buff set <id>");
				sender.sendMessage("/cutepvp player add <player> <team>");
				sender.sendMessage("/cutepvp player remove <player>");
				sender.sendMessage("/cutepvp broadcast <team> <message>");
			}
			return true;
		}

		return false;
	} // onCommand

	// ------------------------------------------------------------------------
	/**
	 * Handle the /cutepvp command.
	 * 
	 * <ul>
	 * <li>/cutepvp save - save configuration</li>
	 * <li>/cutepvp setspawn &lt;team&gt; - set spawn of team</li>
	 * <li>/cutepvp flag list - list flag locations</li>
	 * <li>/cutepvp flag set &lt;team&gt; &lt;id&gt; - set the location of the
	 * flag with the specified ID.</li>
	 * <li>/cutepvp buff list - list team buff locations</li>
	 * <li>/cutepvp buff set &lt;id&gt; - set the location of the buff with the
	 * specified ID.</li>
	 * <li>/cutepvp player add &lt;player&gt; &lt;team&gt; - add the player to
	 * the specified team.</li>
	 * <li>/cutepvp player remove &lt;player&gt; - remove the player from their
	 * team.</li>
	 * <li>/cutepvp broadcast &lt;team&gt; &lt;message&gt; - broadcast a
	 * message to the specified team</li>
	 * </ul>
	 * 
	 * @param sender the CommandSender.
	 * @param args the command arguments.
	 * @return true if the command was handled.
	 */
	protected boolean handleCutePvPCommand(CommandSender sender, String[] args) {
		if (args.length == 0) {
			return false;
		}

		if (args[0].equalsIgnoreCase("save")) {
			if (args.length == 1) {
				getConfiguration().save();
				Messages.success(sender, Messages.PREFIX, "Configuration saved.");
			} else {
				Messages.failure(sender, Messages.PREFIX, "Usage: /cutepvp save");
			}
			return true;
		} else if (args[0].equalsIgnoreCase("setspawn")) {
			if (sender instanceof Player) {
				if (args.length == 2) {
					Player player = (Player) sender;
					String teamId = args[1];

					// Set the main first join and non team spawns?
					if (teamId.equals("firstjoin")) {
						Messages.success(sender, Messages.PREFIX, "First join spawn location set.");
						getConfiguration().FIRST_JOIN_SPAWN_LOCATION = player.getLocation();
						getConfiguration().save();
					} else if (teamId.equals("nonteam")) {
						Messages.success(sender, Messages.PREFIX, "Non-team respawn location set.");
						getConfiguration().NON_TEAM_RESPAWN_LOCATION = player.getLocation();
						getConfiguration().save();
					} else {
						Team team = getTeamManager().getTeam(teamId);
						if (team == null) {
							Messages.failure(sender, Messages.PREFIX, teamId + " is not a valid team ID.");
						} else {
							Messages.success(sender, Messages.PREFIX, team.getName() + " spawn set.");
							team.setSpawn(player.getLocation());
						}
					}
				} else {
					Messages.failure(sender, Messages.PREFIX, "Usage: /cutepvp setspawn <team>");
				}
			} else {
				Messages.failure(sender, Messages.PREFIX, "You need to be in game to set team spawns.");
			}
			return true;
		} else if (args[0].equalsIgnoreCase("flag")) {
			if (args.length > 1) {
				if (args[1].equalsIgnoreCase("list")) {
					if (args.length == 2) {
						for (Team team : getTeamManager()) {
							StringBuilder message = new StringBuilder();
							message.append(team.getName()).append(' ').append("flags: ");
							for (Flag flag : team.getFlags()) {
								message.append(" ").append(flag.getId()).append(" \"").append(flag.getName());
								message.append("\" @ ").append(Messages.formatIntegerXYZ(flag.getLocation()));
							}
							Messages.success(sender, Messages.PREFIX, message.toString());
						}
					} else {
						Messages.failure(sender, Messages.PREFIX, "Usage: /cutepvp flag list");
					}
					return true;
				} else if (args[1].equalsIgnoreCase("set")) {
					if (args.length == 4) {
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
									List<Block> inSight = player.getLastTwoTargetBlocks((HashSet<Byte>) null, 50);
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
					} else {
						Messages.failure(sender, Messages.PREFIX, "Usage: /cutepvp flag set <team> <id>");
					}
					return true;
				}
			}
		} else if (args[0].equalsIgnoreCase("buff")) {
			if (args.length > 1) {
				if (args[1].equalsIgnoreCase("list")) {
					if (args.length == 2) {
						StringBuilder message = new StringBuilder();
						message.append("Team buffs: ");
						for (TeamBuff teamBuff : getBuffManager()) {
							message.append(" ").append(teamBuff.getId()).append(" \"").append(teamBuff.getName());
							message.append("\" @ ").append(Messages.formatIntegerXYZ(teamBuff.getLocation()));
						}
						Messages.success(sender, Messages.PREFIX, message.toString());
					} else {
						Messages.failure(sender, Messages.PREFIX, "Usage: /cutepvp buff list");
					}
					return true;
				} else if (args[1].equalsIgnoreCase("set")) {
					if (args.length == 3) {
						String buffId = args[2];
						TeamBuff teamBuff = getBuffManager().getTeamBuff(buffId);
						if (teamBuff == null) {
							Messages.failure(sender, Messages.PREFIX, "There is no team buff with that ID.");
						} else {
							if (sender instanceof Player) {
								Player player = (Player) sender;
								List<Block> inSight = player.getLastTwoTargetBlocks((HashSet<Byte>) null, 50);
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
					} else {
						Messages.failure(sender, Messages.PREFIX, "Usage: /cutepvp buff set <id>");
					}
					return true;
				}
			}
		} else if (args[0].equalsIgnoreCase("player")) {
			if (args.length > 1) {
				if (args[1].equalsIgnoreCase("add")) {
					if (args.length == 4) {
						Player player = getServer().getPlayer(args[2]);
						if (player == null) {
							Messages.failure(sender, Messages.PREFIX, "That player is not online.");
						} else {
							TeamPlayer teamPlayer = getTeamManager().getTeamPlayer(player);
							if (teamPlayer == null) {
								Team team = getTeamManager().getTeam(args[3]);
								if (team == null) {
									Messages.failure(sender, Messages.PREFIX, "Invalid team. Available teams: "
											+ StringUtils.join(getTeamManager().iterator(), ", ") + ".");
								} else {
									if (getTeamManager().assignTeam(player, team) != null) {
										Messages.success(sender, Messages.PREFIX, player.getName() + " is now on "
												+ team.getName() + ".");
									}
								}
							} else {
								Messages.failure(sender, Messages.PREFIX, "That player is already on "
										+ teamPlayer.getTeam().getName() + ".");
							}
						}
					} else {
						Messages.failure(sender, Messages.PREFIX, "Usage: /cutepvp player add <player> <team>");
					}
					return true;
				} else if (args[1].equalsIgnoreCase("remove")) {
					if (args.length == 3) {
						TeamPlayer teamPlayer = getTeamManager().removeTeamPlayer(args[2]);
						if (teamPlayer == null) {
							Messages.failure(sender, Messages.PREFIX, "That player is not currently on a team.");
						} else {
							Messages.failure(sender, Messages.PREFIX, args[2] + " has been removed from "
									+ teamPlayer.getTeam().getName() + ".");
						}
					} else {
						Messages.failure(sender, Messages.PREFIX, "Usage: /cutepvp player remove <player>");
					}
					return true;
				}
			}
		} else if (args[0].equalsIgnoreCase("broadcast")) {
			if (args.length > 2) {
				Team team = getTeamManager().getTeam(args[1]);
				if (team == null) {
					Messages.failure(sender, Messages.PREFIX, args[1] + " is not a valid team ID.");
				} else {
					String msg = team.encodeTeamColor("[" + team.getName() + "] ")
							+ StringUtils.join(args, ' ', 2, args.length);
					team.message(msg, false);
					getLogger().info(msg);
				}
			} else {
				Messages.failure(sender, Messages.PREFIX, "Usage: /cutepvp broadcast <team> <message>");
			}
			return true;
		}
		return false;
	} // handleCutePvPCommand

	// ------------------------------------------------------------------------
	/**
	 * The implementation of the /join command.
	 * 
	 * <ul>
	 * <li>If the player is not on a team, he is allocated to a team unless
	 * exempted by the CutePVP.exempt permision.</li>
	 * <li>If the player is not in the Overworld, the team block is affixed as a
	 * helmet, his display name color is set and he is teleported to his team's
	 * spawn.</li>
	 * <li>The player's bed spawn is set to the team spawn. This also prevents
	 * him from dying back to the minigames area in The End.</li>
	 * </ul>
	 */
	public void join(Player player) {
		if (player.hasPermission(Permissions.EXEMPT)) {
			Messages.failure(player, Messages.PREFIX, "You are exempted from playing by the " +
														Permissions.EXEMPT + " permission. Talk to a techadmin.");
			return;
		}

		TeamPlayer teamPlayer = getTeamManager().getTeamPlayer(player);
		if (teamPlayer == null) {
			// Player has not joined a team before. Assign them now.
			getTeamManager().assignTeam(player);
		} else {
			// If the player is already on a team, simply remind them of that.
			Team team = teamPlayer.getTeam();
			player.sendMessage("You're on " + team.encodeTeamColor(team.getName()) + ".");

			// If they happen to be in the lobby/minigames area in The End,
			// spawn them back into their team base.
			if (!isInMatchArea(player)) {
				team.setTeamAttributes(player);
				player.teleport(team.getSpawn());
			}
		}
	}

	// ------------------------------------------------------------------------
	/**
	 * Reference to the WorldGuard plugin.
	 */
	private WorldGuardPlugin _worldGuard;

	/**
	 * Plugin configuration.
	 */
	private Configuration _configuration = new Configuration(this);

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
	 * The {@link ScoreboardManager}.
	 */
	private ScoreboardManager _scoreboardManager = new ScoreboardManager(this);

	/**
	 * The {@link AlertManager}.
	 */
	private AlertManager _alertManager = new AlertManager(this);

} // class CutePVP