package com.c45y.CutePVP;

import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

import com.c45y.CutePVP.buff.TeamBuff;

// ----------------------------------------------------------------------------
/**
 * Event listener implementation.
 * 
 * The synchronous PlayerChatEvent is deprecated but it's just so much easier to
 * handle that way.
 */
@SuppressWarnings("deprecation")
public class CutePVPListener implements Listener {
	// ------------------------------------------------------------------------
	/**
	 * Constructor.
	 * 
	 * @param plugin the plugin.
	 */
	public CutePVPListener(CutePVP plugin) {
		_plugin = plugin;
	}

	// ------------------------------------------------------------------------
	/**
	 * Cancel attempts to take off the woolen helmet.
	 */
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onInventoryClick(InventoryClickEvent event) {
		Player player = (Player) event.getWhoClicked();
		if (event.getSlotType() == SlotType.ARMOR &&
			!_plugin.getTeamManager().isExempted(player) && event.getSlot() == 39) {
			event.setCancelled(true);
		}
	}

	// ------------------------------------------------------------------------
	/**
	 * Disable crafting of helmets, per configuration.
	 */
	@EventHandler(ignoreCancelled = true)
	public void onCraftItemEvent(CraftItemEvent event) {
		if (!_plugin.getConfiguration().ALLOW_HELMET_CRAFTING && event.getRecipe().getResult() != null) {
			Material result = event.getRecipe().getResult().getType();
			if (result == Material.DIAMOND_HELMET ||
				result == Material.IRON_HELMET ||
				result == Material.GOLD_HELMET ||
				result == Material.LEATHER_HELMET) {
				event.setCancelled(true);
			}
		}
	}

	// ------------------------------------------------------------------------
	/**
	 * Respawn players in their team spawn.
	 * 
	 * Note:
	 * "getPlayer getPlayerExact returns null when called during respawn event"
	 * https://bukkit.atlassian.net/browse/BUKKIT-4561
	 * 
	 * Since TeamPlayer.getPlayer() is now based on getPlayerExact(), setting
	 * the player's helmet on respawn requires use of the Player instance from
	 * the event.
	 */
	@EventHandler(ignoreCancelled = true)
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		Player player = event.getPlayer();
		TeamPlayer teamPlayer = _plugin.getTeamManager().getTeamPlayer(player);
		if (teamPlayer != null) {
			_plugin.getLogger().info(event.getPlayer().getName() + " respawned on " + teamPlayer.getTeam().getName() + ".");
			teamPlayer.getTeam().setTeamAttributes(player);
		}

		// If the player's bed (including minigame sleep signs in the End) is
		// obstructed, Player.getBedSpawnLocation() is cleared to null and the
		// player will respawn at vanilla spawn in the overworld. Instead,
		// restore the bed spawn to the default, non-team spawn in the End.
		if (player.getBedSpawnLocation() == null) {
			player.setBedSpawnLocation(_plugin.getConfiguration().NON_TEAM_RESPAWN_LOCATION, true);
		}

		// If the player is on a team and in the overworld, then spawn them in
		// their base.
		if (teamPlayer != null && _plugin.isInMatchArea(player)) {
			event.setRespawnLocation(teamPlayer.getTeam().getSpawn());
		} else {
			// Otherwise, spawn them at their (now valid) bed spawn location.
			event.setRespawnLocation(player.getBedSpawnLocation());
		}
	}

	// ------------------------------------------------------------------------
	/**
	 * On join, spawn new players at the first join spawn, in The End.
	 * 
	 * If they have already joined a team, tell everybody about it and set their
	 * helmet (just in case) and display name.
	 */
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		Configuration config = _plugin.getConfiguration();

		TeamManager tm = _plugin.getTeamManager();
		tm.onPlayerJoin(player);
		if (player.hasPermission(Permissions.MOD)) {
			tm.onStaffJoin(player);
			_plugin.getLogger().info(player.getName() + " has staff permissions.");
		}
		if (tm.isExempted(player)) {
			_plugin.getLogger().info(player.getName() + " is exempted from team assignment.");
		}

		TeamPlayer teamPlayer = tm.getTeamPlayer(player);
		if (teamPlayer != null) {
			Team team = teamPlayer.getTeam();
			team.setTeamAttributes(player);
			player.sendMessage("You're on " + team.encodeTeamColor(team.getName()) + ".");
			event.setJoinMessage(player.getDisplayName() + " joined the game.");
			_plugin.getLogger().info(player.getName() + " rejoined " + team.getName() + ".");
		}

		// If this is the first join, spawn in the designated location.
		// Also set respawn location in case they choose not to join a team.
		if (!player.hasPlayedBefore()) {
			player.teleport(config.FIRST_JOIN_SPAWN_LOCATION);

			// This overrides the spawn set by Team.setTeamAttributes(player).
			player.setBedSpawnLocation(config.NON_TEAM_RESPAWN_LOCATION, true);
		}
	} // onPlayerJoin

	// ------------------------------------------------------------------------
	/**
	 * When leaving, show the (colorful) display name in the leave message.
	 * 
	 * Drop the flag if the player is carrying it.
	 */
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		event.setQuitMessage(player.getDisplayName() + " left the game.");

		TeamManager tm = _plugin.getTeamManager();
		TeamPlayer teamPlayer = tm.getTeamPlayer(player);
		if (teamPlayer != null && teamPlayer.isCarryingFlag()) {
			teamPlayer.getCarriedFlag().drop();
		}
	}

	// ------------------------------------------------------------------------
	/**
	 * When the player steps on a block with special properties, apply the
	 * corresponding buff.
	 */
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		if (_plugin.isInMatchArea(player)) {
			TeamPlayer teamPlayer = _plugin.getTeamManager().getTeamPlayer(player);
			if (teamPlayer != null) {
				_plugin.applyFloorBuffs(teamPlayer);

				// Return flags carried into a player's own base.
				if (teamPlayer.isCarryingFlag() && teamPlayer.getTeam().inTeamBase(player.getLocation())) {
					Flag flag = teamPlayer.getCarriedFlag();
					flag.doReturn();
					Messages.broadcast(player.getDisplayName() + Messages.BROADCAST_COLOR +
										" tried to carry " + flag.getTeam().getName() + "'s " +
										flag.getName() + " flag into his base's safe area. It was returned.");
				}
			}
		}
	}

	// ------------------------------------------------------------------------
	/**
	 * Bar held and projectile weapon PvP damage in player's own team's base.
	 * 
	 * Also block damage from own team members, and between participants and
	 * non-participants (both cases).
	 */
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if (!(event.getEntity() instanceof Player)) {
			return;
		}

		// If the victim is not in the match area, don't change PvP mechanics.
		Player victim = (Player) event.getEntity();
		if (!_plugin.isInMatchArea(victim)) {
			return;
		}

		if (event.getDamager() instanceof Player) {
			Player attacker = (Player) event.getDamager();
			TeamPlayer teamAttacker = _plugin.getTeamManager().getTeamPlayer(attacker);
			TeamPlayer teamVictim = _plugin.getTeamManager().getTeamPlayer(victim);
			handlePvPDamage(event, teamAttacker, teamVictim);

		} else if (event.getDamager() instanceof Projectile) {
			Projectile projectile = (Projectile) event.getDamager();
			if (projectile.getShooter() instanceof Player) {
				TeamPlayer teamAttacker = _plugin.getTeamManager().getTeamPlayer((Player) projectile.getShooter());
				TeamPlayer teamVictim = _plugin.getTeamManager().getTeamPlayer(victim);

				// Allow players to hurt themselves throwing ender pearls.
				boolean pearlDamage = (teamAttacker == teamVictim && projectile instanceof EnderPearl);
				if (!pearlDamage) {
					handlePvPDamage(event, teamAttacker, teamVictim);
				}
			}
		}
	}

	// ------------------------------------------------------------------------
	/**
	 * Handle EntityDamageByEntityEvent by checking for PvP and disallowing PvP
	 * damage when:
	 * 
	 * <ul>
	 * <li>Between participants and non-participants, or</li>
	 * <li>Between participants on the same team, or</li>
	 * <li>When the victim is in his team's base.</li>
	 * </ul>
	 */
	protected void handlePvPDamage(EntityDamageByEntityEvent event, TeamPlayer attacker, TeamPlayer victim) {
		if (attacker == null || victim == null) {
			// One or other player is not a participant. No damage dealt.
			event.setCancelled(true);
		} else if (attacker.getTeam() == victim.getTeam()) {
			// Both players on the same team. No damage dealt.
			event.setCancelled(true);
		} else if (!_plugin.getConfiguration().CAN_ATTACK_IN_ENEMY_BASE &&
					victim.getTeam().inTeamBase(victim.getPlayer().getLocation())) {
			attacker.getPlayer().sendMessage(ChatColor.DARK_RED + "You cannot attack within another team's base.");
			event.setCancelled(true);
		}
	}

	// ------------------------------------------------------------------------
	/**
	 * Handle flag steals, captures (scoring) and returns, which all require
	 * that a player right clicks on a flag.
	 * 
	 * <ul>
	 * <li>To steal a flag, a player must right click on the opposing team's
	 * flag (whether dropped or at home).</li>
	 * <li>To capture a flag, a player must be carrying an opposing team's flag
	 * when he clicks on his own flag at home.</li>
	 * <li>To return a flag, a player must click on his own flag when it is
	 * dropped (not at home).</li>
	 * </ul>
	 * 
	 * @param event the event.
	 */
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		if (!_plugin.isInMatchArea(player)) {
			return;
		}

		Location loc = player.getLocation();
		TeamManager tm = _plugin.getTeamManager();
		TeamPlayer teamPlayer = tm.getTeamPlayer(player);

		// Treat left and right clicks the same to catch attempts to break flag.
		if (teamPlayer == null ||
			(event.getAction() != Action.RIGHT_CLICK_BLOCK &&
			event.getAction() != Action.LEFT_CLICK_BLOCK)) {
			return;
		}

		// Is the clicked block a team buff?
		Block clickedBlock = event.getClickedBlock();
		TeamBuff teamBuff = _plugin.getBuffManager().getTeamBuffFromBlock(clickedBlock);
		if (teamBuff != null) {
			teamBuff.claimBy(teamPlayer);

			// Cancel the interaction, so that we can use beacons as purely
			// decorative buff markers.
			event.setCancelled(true);
			return;
		}

		// Is it a team block and therefore possibly a flag?
		Team clickedBlockTeam = tm.getTeamFromBlock(clickedBlock);
		if (clickedBlockTeam != null) {
			// Block is of the same material as a team block. Is it a flag?
			Flag flag = clickedBlockTeam.getFlagFromBlock(clickedBlock);
			if (flag != null) {
				// Player's own team flag?
				if (clickedBlockTeam == teamPlayer.getTeam()) {
					// Returning a dropped flag?
					if (flag.isDropped()) {
						flag.doReturn();
						teamPlayer.getScore().returns.increment();
						teamPlayer.getTeam().getScore().returns.increment();
						Messages.broadcast(player.getDisplayName() + Messages.BROADCAST_COLOR + " returned " +
											teamPlayer.getTeam().getName() + "'s flag.");
						if (_plugin.getConfiguration().FLAG_RETURN_SOUND != null) {
							loc.getWorld().playSound(loc, _plugin.getConfiguration().FLAG_RETURN_SOUND, Constants.SOUND_RANGE, 1);
						}
					} else if (flag.isHome() && teamPlayer.isCarryingFlag()) {
						// Capturing an opposition team's flag.
						teamPlayer.getScore().captures.increment();
						teamPlayer.getTeam().getScore().captures.increment();

						Flag carriedFlag = teamPlayer.getCarriedFlag();
						carriedFlag.doReturn();
						Messages.broadcast(player.getDisplayName() + Messages.BROADCAST_COLOR + " captured " +
											carriedFlag.getTeam().getName() + "'s " + carriedFlag.getName() + " flag.");
						if (_plugin.getConfiguration().FLAG_CAPTURE_SOUND != null) {
							loc.getWorld().playSound(loc, _plugin.getConfiguration().FLAG_CAPTURE_SOUND, Constants.SOUND_RANGE, 1);
						}
					}
				} else {
					// An opposition team flag.
					if (teamPlayer.isCarryingFlag()) {
						player.sendMessage(ChatColor.DARK_RED + "You can only carry one flag at a time.");
					} else {
						// Only count towards the score if the flag was at home.
						if (flag.isHome()) {
							teamPlayer.getScore().steals.increment();
							teamPlayer.getTeam().getScore().steals.increment();
							Messages.broadcast(player.getDisplayName() + Messages.BROADCAST_COLOR +
												" has stolen " + clickedBlockTeam.getName() + "'s flag.");
							if (_plugin.getConfiguration().FLAG_STEAL_SOUND != null) {
								loc.getWorld().playSound(loc, _plugin.getConfiguration().FLAG_STEAL_SOUND, Constants.SOUND_RANGE, 1);
							}
						}
						flag.stealBy(teamPlayer);
					}
				}
			}
		}
	} // onPlayerInteract

	// ------------------------------------------------------------------------
	/**
	 * If a player attempts to carry a flag out of the world, it is
	 * automatically returned. If the player clicks a warp sign to go to the
	 * lobby, set his bed spawn back into the lobby area so that he will return
	 * there when dying in minigames. While in the lobby (The End) the /join
	 * command will return him to the Overworld and set the spawn back to the
	 * team spawn.
	 * 
	 * This event is called after the player has already changed worlds.
	 */
	@EventHandler(ignoreCancelled = true)
	public void onPlayerChangedWorldEvent(PlayerChangedWorldEvent event) {
		Player player = event.getPlayer();
		TeamPlayer teamPlayer = _plugin.getTeamManager().getTeamPlayer(player);
		if (teamPlayer != null && teamPlayer.isCarryingFlag()) {
			Flag flag = teamPlayer.getCarriedFlag();
			flag.doReturn();
			Messages.broadcast(player.getDisplayName() + Messages.BROADCAST_COLOR +
								" tried to carry " + flag.getTeam().getName() + "'s " +
								flag.getName() + " flag out of the world. It was returned.");
		}

		// When arriving in The End, set the bed spawn there.
		if (!_plugin.isInMatchArea(player)) {
			player.setBedSpawnLocation(_plugin.getConfiguration().NON_TEAM_RESPAWN_LOCATION, true);
		}
	}

	// ------------------------------------------------------------------------
	/**
	 * When a player dies:
	 * 
	 * <ul>
	 * <li>Drop the flag if he's carrying it, but return it home if he falls out
	 * of the world.</li>
	 * <li>Increment his and his team's kill scores if they are not on the same
	 * team. Players can't hurt their team mates, so the check is redundant, but
	 * cheap and future-proof.</li>
	 * </ul>
	 */
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerDeath(PlayerDeathEvent event) {
		Player player = event.getEntity();

		// Do some of this processing even if not in the match area.
		TeamPlayer teamPlayer = _plugin.getTeamManager().getTeamPlayer(player);
		if (teamPlayer != null) {
			if (_plugin.isInMatchArea(player)) {
				// Player deaths count towards the death score whether caused
				// by PvP, mobs or natural causes like falling and lava.
				// Players routinely try to suicide to avoid giving a kill.
				teamPlayer.getTeam().getScore().deaths.increment();
				teamPlayer.getScore().deaths.increment();

				// Flags cannot leave the world (match area).
				if (teamPlayer.isCarryingFlag()) {
					// Flags that fall out of the world are returned.
					Flag flag = teamPlayer.getCarriedFlag();
					Location loc = flag.getHomeLocation();
					if (player.getLocation().getY() < 0) {
						flag.doReturn();
						Messages.broadcast(player.getDisplayName() + Messages.BROADCAST_COLOR +
											" fell out of the world with " + flag.getTeam().getName() + "'s " +
											flag.getName() + " flag. It was returned.");
						if (_plugin.getConfiguration().FLAG_RETURN_SOUND != null) {
							loc.getWorld().playSound(loc, _plugin.getConfiguration().FLAG_RETURN_SOUND, Constants.SOUND_RANGE, 1);
						}
					} else {
						flag.drop();
					}
				}

				// Only count kills towards the score in the match area.
				Player killer = player.getKiller();
				TeamPlayer teamKiller = _plugin.getTeamManager().getTeamPlayer(killer);
				if (teamKiller != null && teamPlayer.getTeam() != teamKiller.getTeam()) {
					teamKiller.getTeam().getScore().kills.increment();
					teamKiller.getScore().kills.increment();
				}
			} // in the match area

			// Remove the helmet from the drops to prevent farming wool.
			// This applies in The End as well as the Overworld.
			ItemStack teamBlockStack = null;
			for (Iterator<ItemStack> it = event.getDrops().iterator(); it.hasNext();) {
				ItemStack stack = it.next();
				if (stack.getData().equals(teamPlayer.getTeam().getMaterialData())) {
					it.remove();
					teamBlockStack = stack;
					break;
				}
			}
			if (teamBlockStack != null) {
				teamBlockStack.setAmount(teamBlockStack.getAmount() - 1);
				if (teamBlockStack.getAmount() > 0) {
					event.getDrops().add(teamBlockStack);
				}
			}
		}
	} // onPlayerDeath

	// ------------------------------------------------------------------------
	/**
	 * Handle chat by cancelling the normal event and directly messaging players
	 * with team colors inserted.
	 */
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerChat(PlayerChatEvent event) {
		event.setCancelled(true);
		Player player = event.getPlayer();

		// Log chat in standard form so Mark2 triggers work.
		Bukkit.getLogger().info("<" + player.getName() + "> " + ChatColor.stripColor(event.getMessage()));
		String message = "<" + player.getDisplayName() + "> " + ChatColor.stripColor(event.getMessage());

		TeamPlayer teamPlayer = _plugin.getTeamManager().getTeamPlayer(player);
		if (teamPlayer != null) {
			// A match participant.
			teamPlayer.getTeam().message(message);
		} else {
			// If a player hasn't joined, send to all non-staff in the end.
			for (Player online : Bukkit.getOnlinePlayers()) {
				if (!_plugin.isInMatchArea(online) && !online.hasPermission(Permissions.MOD)) {
					online.sendMessage(message);
				}
			}
		}

		// Copy the message to staff.
		for (Player staff : _plugin.getTeamManager().getOnlineStaff()) {
			staff.sendMessage(message);
		}
	}

	// ------------------------------------------------------------------------
	/**
	 * Allow block placement per
	 * {@link CutePVPListener#allowBlockEdit(Player, Location)}.
	 */
	@EventHandler(ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event) {
		Player player = event.getPlayer();
		if (!_plugin.isInMatchArea(player)) {
			return;
		}

		if (!_plugin.getConfiguration().CAN_EDIT_ENEMY_BASE &&
			!allowBlockEdit(player, event.getBlock().getLocation())) {
			event.setCancelled(true);
			return;
		}

		TeamPlayer teamPlayer = _plugin.getTeamManager().getTeamPlayer(player);
		if (teamPlayer != null) {
			if (_plugin.getConfiguration().TEAM_SPECIFIC_FLOOR_BUFFS) {
				_plugin.getBuffManager().setFloorBuffTeam(event.getBlock(), teamPlayer.getTeam());
			}
		}
	}

	// ------------------------------------------------------------------------
	/**
	 * Allow block breaks per
	 * {@link CutePVPListener#allowBlockEdit(Player, Location)}.
	 */
	@EventHandler(ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event) {
		Player player = event.getPlayer();
		if (!_plugin.isInMatchArea(player)) {
			return;
		}

		if (!_plugin.getConfiguration().CAN_EDIT_ENEMY_BASE &&
			!allowBlockEdit(player, event.getBlock().getLocation())) {
			event.setCancelled(true);
			return;
		}
	}

	// ------------------------------------------------------------------------
	/**
	 * Return true if attempted edits should be allowed.
	 * 
	 * <ul>
	 * <li>Staff can edit blocks anywhere.</li>
	 * <li>Players in their own base regions can edit, except for the flag
	 * blocks.</li>
	 * <li>Players in the base regions of other teams cannot edit any blocks.</li>
	 * </ul>
	 * 
	 * @param player the player doing the edit.
	 * @param location the location of the edited block.
	 * @return true if attempted edits should be allowed.
	 */
	protected boolean allowBlockEdit(Player player, Location location) {
		TeamPlayer teamPlayer = _plugin.getTeamManager().getTeamPlayer(player);
		if (teamPlayer == null || player.hasPermission(Permissions.BUILD)) {
			// Staff member.
			return true;
		}

		if (teamPlayer.getTeam().inTeamBase(location)) {
			return !teamPlayer.getTeam().isFlagHomeLocation(location);
		} else if (_plugin.getTeamManager().inEnemyTeamBase(player, location)) {
			player.sendMessage(ChatColor.DARK_RED + "You cannot build in an enemy base.");
			return false;
		} else {
			return true;
		}
	}

	// ------------------------------------------------------------------------
	/**
	 * Owning plugin.
	 */
	private final CutePVP _plugin;
} // CutePVPListener
