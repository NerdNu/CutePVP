package com.c45y.CutePVP;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class CutePVPListener implements Listener{
	public final CutePVP plugin;

	public CutePVPListener(CutePVP instance) {
		plugin = instance;
	}

	/* Inventory crap, locking the wool in place */

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onInventoryClick(InventoryClickEvent event) {
		if ( plugin.tm.staffTeam.inTeam(event.getWhoClicked().getName())) {
			return;
		}
		if (event.getSlot() == 39 /* Helmet slot */) {
			event.setCancelled(true);
		};
	}

//	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
//	public void onInventoryClose(InventoryCloseEvent event) {
//		Player player = (Player) event.getPlayer();
//		if ( plugin.tm.staffTeam.inTeam(event.getPlayer().getName())) {
//			return;
//		}
//		Team team = plugin.tm.getTeamMemberOf(player.getName());
//		team.setHelmet(player);
//	}

	/* END - Inventory crap, locking the wool in place */

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		Team playerTeam = plugin.tm.getTeamMemberOf(event.getPlayer().getName());
		playerTeam.setHelmet(event.getPlayer());
		event.setRespawnLocation(playerTeam.getTeamSpawn());
	} //Reworked

	/* Event reworked */
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerJoin(PlayerJoinEvent event){
		Player player = event.getPlayer();
		Team playerTeam = plugin.tm.getTeamMemberOf(player.getName());
		if( playerTeam == null || !player.hasPlayedBefore()){
			plugin.tm.onFirstJoin(player.getName());
			playerTeam = plugin.tm.getTeamMemberOf(player.getName());
		}
		player.setDisplayName(playerTeam.encodeTeamColor(player.getName()));
		playerTeam.setHelmet(event.getPlayer());
		event.setJoinMessage(player.getDisplayName() + " joined the game.");
		plugin.savePlayers();
	}//Reworked

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerQuit(PlayerQuitEvent event){
		Player player = event.getPlayer();
		event.setQuitMessage(player.getDisplayName() + " left the game.");
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		Block block = player.getLocation().getBlock().getRelative(BlockFace.DOWN);
		if (plugin.tm.shouldTakeDamageFromBlock(block, player.getName())) {
			player.damage(1);
		}
	}//Reworked

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if (!(event.getEntity() instanceof Player)) {
			return;
		}
		if ((event.getDamager() instanceof Player)) {
			Player attacker = (Player) event.getDamager();
			Player player = (Player) event.getEntity();
			Team attackerTeam = plugin.tm.getTeamMemberOf(attacker.getName());
			if (attackerTeam.inTeam(player.getName())) {
				event.setCancelled(true);
			} else if(plugin.tm.inRangeOfEnemyTeamSpawn(attacker)){
				attacker.sendMessage(ChatColor.DARK_RED + "You cannot attack within another teams base");
				event.setCancelled(true);
			}
		}
		else if (event.getDamager() instanceof Arrow) {
			Arrow arrow = (Arrow) event.getDamager();
			if (arrow.getShooter() instanceof Player) {
				Player player = (Player) event.getEntity();
				Player shooter = (Player) arrow.getShooter();
				Team attackerTeam = plugin.tm.getTeamMemberOf(shooter.getName());
				if (attackerTeam.inTeam(player.getName())) {
					event.setCancelled(true);
				} else if(plugin.tm.inRangeOfEnemyTeamSpawn(shooter)){
					shooter.sendMessage(ChatColor.DARK_RED + "You cannot attack within another teams base");
					event.setCancelled(true);
				}
			}
		}
	}//Reworked

	@EventHandler(priority= EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerInteract(PlayerInteractEvent event) {
		if ( plugin.tm.staffTeam.inTeam(event.getPlayer().getName()) || event.getAction() != Action.RIGHT_CLICK_BLOCK) {
			return;
		}

		Player player = event.getPlayer();
		Block b = event.getClickedBlock();
		if (b.getType() != Material.WOOL) {
			return;
		}
		if (b.getData() != (byte) 14 && b.getData() != (byte) 3 && b.getData() != (byte) 4 && b.getData() != (byte) 5 ) {
			return;
		}

		Team woolTeam = plugin.tm.getTeamFromWool(b.getData());
		Team attacker = plugin.tm.getTeamMemberOf(event.getPlayer().getName());

		if(!woolTeam.isTeamFlag(b.getLocation())) { //The block is the flag block
			return;
		}
		//Own team
		if(attacker == woolTeam){ //Returning a dropped flag
			if( woolTeam.isTeamFlag(woolTeam.getTeamFlagHome())) { //Clicking a returned flag is pointless
				return;
			}
			plugin.getServer().broadcastMessage(player.getDisplayName() + " returned the " + woolTeam.getTeamName() + " flag.");
			woolTeam.respawnTeamFlag();
			b.setType(Material.AIR);
			return;
		}
		//Opposing team
		if(woolTeam.flagHolder != null) { //Someone currently has the flag, they must be placing it.
			woolTeam.setCarrier(player); //They have placed the flag, nobody is in posession.
			if(attacker.isTeamFlagRegion(b.getLocation())) { //Placing block in own base, flag cap
				attacker.addTeamScore(1); //Increment the team score
				attacker.addPlayerScore(player.getName(), 10);
				woolTeam.respawnTeamFlag();//Reset the team flag
				b.setType(Material.AIR);
				plugin.getServer().broadcastMessage(player.getDisplayName() + " captured the " + woolTeam.getTeamName() + " flag.");
			}
		} else {
			woolTeam.setCarrier(player);
			b.setType(Material.AIR);
			plugin.getServer().broadcastMessage(player.getDisplayName() + " has stolen the " + woolTeam.getTeamName() + " flag.");
		}
	}//Reworked

	@EventHandler(priority= EventPriority.HIGHEST, ignoreCancelled= true)
	public void onPlayerDisconnect(PlayerQuitEvent event) {
		Team flagOf = plugin.tm.isFlagBearer(event.getPlayer());
		if (flagOf != null) {
			flagOf.dropTeamFlag(event.getPlayer().getLocation());
			flagOf.setCarrier(null);
		}
	}

	@EventHandler(priority= EventPriority.HIGHEST, ignoreCancelled= true)
	public void onPlayerKick(PlayerKickEvent event) {
		Team flagOf = plugin.tm.isFlagBearer(event.getPlayer());
		if (flagOf != null) {
			flagOf.dropTeamFlag(event.getPlayer().getLocation());
			flagOf.setCarrier(null);
		}
	}

	@EventHandler(priority= EventPriority.HIGHEST, ignoreCancelled= true)
	public void onPlayerDeath(PlayerDeathEvent event) {
		Team flagOf = plugin.tm.isFlagBearer(event.getEntity());
		if (flagOf != null) {
			flagOf.dropTeamFlag(event.getEntity().getLocation());
			flagOf.setCarrier(null);
		}

		if (event.getEntity().getKiller() instanceof Player) {
			String killer = event.getEntity().getKiller().getName();
			plugin.tm.getTeamMemberOf(killer).addTeamKill();
			plugin.tm.getTeamMemberOf(killer).addPlayerScore(killer, 1);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerChat(PlayerChatEvent event) {
		Player player = event.getPlayer();
		Team playerTeam = plugin.tm.getTeamMemberOf(player.getName());
		plugin.getLogger().info(player.getName() + ": " + ChatColor.stripColor(event.getMessage()));
		event.setCancelled(true);
		playerTeam.message("<" + player.getDisplayName() + "> " + ChatColor.stripColor(event.getMessage()));
		if (playerTeam != plugin.tm.staffTeam) {
			plugin.tm.staffTeam.message("<" + player.getDisplayName() + "> " + ChatColor.stripColor(event.getMessage()));
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event) {
		if ( plugin.tm.staffTeam.inTeam(event.getPlayer().getName())) {
			return;
		}
		Team team = plugin.tm.isFlagBlock(event.getBlock().getLocation());
		if (team != null) {
			event.setCancelled(true);
		}
		if (plugin.tm.inRangeOfEnemyTeamSpawn(event.getPlayer())) {
			event.getPlayer().sendMessage(ChatColor.DARK_RED + "You cannot build in an enemy base");
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event) {
		if ( plugin.tm.staffTeam.inTeam(event.getPlayer().getName())) {
			return;
		}
		Team team = plugin.tm.isFlagBlock(event.getBlock().getLocation());
		if (team != null) {
			event.setCancelled(true);
		}
		//If they're in an enemy base...
		if (plugin.tm.inRangeOfEnemyTeamSpawn(event.getPlayer())) {
			event.getPlayer().sendMessage(ChatColor.DARK_RED + "You cannot build in an enemy base");
			event.setCancelled(true);
		}
	}
}
