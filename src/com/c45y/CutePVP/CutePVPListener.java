package com.c45y.CutePVP;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
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
import org.bukkit.inventory.ItemStack;

public class CutePVPListener implements Listener{
	public final CutePVP plugin;

	public CutePVPListener(CutePVP instance) {
		plugin = instance;
	}

	/* Inventory crap, locking the wool in place */

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onInventoryClick(InventoryClickEvent event) {
		Player player = (Player) event.getWhoClicked();
		if ( player.getGameMode() == GameMode.CREATIVE ){
			return;
		}
		if (player.getInventory().getHelmet() == null) {
			event.setCancelled(true);
			return;
		}
		if (player.getInventory().getHelmet().getType() != Material.WOOL) {
			event.setCancelled(true);
			return;
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onInventoryClose(InventoryCloseEvent event) {
		Player player = (Player) event.getPlayer();
		if ( player.getGameMode() == GameMode.CREATIVE ){
			return;
		}
		if (player.getInventory().getHelmet() == null) {
			player.getInventory().setHelmet(plugin.returnWool(player.getName()));
			return;
		}
		if (player.getInventory().getHelmet().getType() != Material.WOOL) {
			player.getInventory().setHelmet(plugin.returnWool(player.getName()));
			return;
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		Player player = (Player) event.getPlayer();
		if(player.isDead()) {
			return;
		}
		if ( player.getGameMode() == GameMode.CREATIVE ){
			return;
		}
		if (event.getItemDrop().getItemStack().getType() == Material.WOOL) {
			event.getItemDrop().remove();
			return;
		}
	}

	/* END - Inventory crap, locking the wool in place */

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		event.getPlayer().getInventory().setHelmet(plugin.returnWool(event.getPlayer().getName()));
		event.setRespawnLocation(plugin.getRespawnTeamLocation(event.getPlayer().getName()));
	}

	/* Event reworked */
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerJoin(PlayerJoinEvent event){
		Player player = event.getPlayer();
		Team playerTeam = plugin.tm.getTeamMemberOf(player.getName());
		if( playerTeam == null || player.hasPlayedBefore()){
			plugin.tm.onFirstJoin(player.getName());
			playerTeam = plugin.tm.getTeamMemberOf(player.getName());
		}
		player.setDisplayName(playerTeam.encodeTeamColor(player.getName()));
		player.getInventory().setHelmet(playerTeam.getTeamItemStack());
		event.setJoinMessage(player.getDisplayName() + " joined the game.");
	}
	
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerQuit(PlayerJoinEvent event){
		Player player = event.getPlayer();
		event.setJoinMessage(player.getDisplayName() + " left the game.");
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		Block block = player.getLocation().getBlock().getRelative(BlockFace.DOWN);
		if (block.getType() == Material.WOOL) {
			if (block.getData() == 14 || block.getData() == 3 || block.getData() == 4 || block.getData() == 5) {
				if (block.getData() != plugin.woolColor(player.getName())) {
					player.damage(1);
				}
			}	
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if (!(event.getEntity() instanceof Player)) {
			return;
		}
		if ((event.getDamager() instanceof Player)) {
			Player attacker = (Player) event.getDamager();
			Player player = (Player) event.getEntity();
            if (plugin.getTeam(attacker.getName()) == plugin.getTeam(player.getName())) {
                    event.setCancelled(true);
            } else if(plugin.getInRangeOfEnemyTeamSpawn(attacker)){
            		attacker.sendMessage(ChatColor.DARK_RED + "You cannot attack within another teams base");
                    event.setCancelled(true);
            }
		}
		else if (event.getDamager() instanceof Arrow) {
			Arrow arrow = (Arrow) event.getDamager();
			if (arrow.getShooter() instanceof Player) {
				Player player = (Player) event.getEntity();
				Player shooter = (Player) arrow.getShooter();
	            if (plugin.getTeam(shooter.getName()) == plugin.getTeam(player.getName())) {
	                    event.setCancelled(true);
	            } else if(plugin.getInRangeOfEnemyTeamSpawn(shooter)){
	            		shooter.sendMessage(ChatColor.DARK_RED + "You cannot attack within another teams base");
	                    event.setCancelled(true);
	            }
			}
		}
	}
        
        @EventHandler(priority= EventPriority.HIGHEST, ignoreCancelled = true)
        public void onPlayerInteract(PlayerInteractEvent event) {
            if (event.getPlayer().getGameMode() == GameMode.CREATIVE || event.getAction() != Action.RIGHT_CLICK_BLOCK) {
                return;
            }
            
            Player player = event.getPlayer();
            Block b = event.getClickedBlock();
            if (b.getType() != Material.WOOL) {
                return;
            }
            
            String woolTeamName = plugin.woolColorToTeamName((short)b.getData());
            Location blockLoc = plugin.getTeamFlagLoc(woolTeamName);
            
            if (plugin.fposSet.containsKey(player.getName())) {
                plugin.setTeamFlagSpawnLoc(plugin.fposSet.get(player.getName()), b.getLocation());
                plugin.setTeamFlagLoc(plugin.fposSet.get(player.getName()), b.getLocation());
                plugin.fposSet.remove(player.getName());
            }
            else if (b.getLocation().getBlockX() == blockLoc.getBlockX() &&
                    b.getLocation().getBlockY() == blockLoc.getBlockY() &&
                    b.getLocation().getBlockZ() == blockLoc.getBlockZ()) {
                String carrierTeamName = plugin.teamName(event.getPlayer().getName());
                
                if (carrierTeamName.equalsIgnoreCase(woolTeamName)) {
                    
                    Location flagSpawnLoc = plugin.getTeamFlagSpawnLoc(woolTeamName);
                    
                    if (b.getLocation().getBlockX() == flagSpawnLoc.getBlockX() &&
                        b.getLocation().getBlockY() == flagSpawnLoc.getBlockY() &&
                        b.getLocation().getBlockZ() == flagSpawnLoc.getBlockZ()) {
                    
                        String carryFor = plugin.carrierFor(player);

                        if (carryFor != null) {
                            if (!carryFor.equalsIgnoreCase(carrierTeamName)) {
                                plugin.capForTeam(carrierTeamName, carryFor);
                                plugin.messageCap(carrierTeamName, carryFor);
                            }
                        }
                    }
                    else {
                        plugin.returnFlag(woolTeamName);
                        if (plugin.dropTime.containsKey(woolTeamName)) {
                            plugin.dropTime.remove(woolTeamName);
                        }
                        plugin.getServer().broadcastMessage(player.getDisplayName() + " returned the " + woolTeamName + " flag.");
                    }
                }
                else {
                    plugin.takeFlag(woolTeamName, player);
                }
            }
        }
        
        @EventHandler(priority= EventPriority.HIGHEST, ignoreCancelled= true)
        public void onPlayerDisconnect(PlayerQuitEvent event) {
            String carrierFor = plugin.carrierFor(event.getPlayer());
            plugin.dropFlag(carrierFor, event.getPlayer());
        }
        
        @EventHandler(priority= EventPriority.HIGHEST, ignoreCancelled= true)
        public void onPlayerKick(PlayerKickEvent event) {
            String carrierFor = plugin.carrierFor(event.getPlayer());
            plugin.dropFlag(carrierFor, event.getPlayer());
        }
        
        @EventHandler(priority= EventPriority.HIGHEST, ignoreCancelled= true)
        public void onPlayerDeath(PlayerDeathEvent event) {
            String carrierFor = plugin.carrierFor(event.getEntity());
            plugin.dropFlag(carrierFor, event.getEntity());
            
            if (event.getEntity().getKiller() != null) {
                String team = plugin.teamName(event.getEntity().getKiller().getName());
                plugin.killForTeam(team);
            }
        }
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerChat(PlayerChatEvent event) {
		if ( event.getPlayer().getGameMode() == GameMode.CREATIVE ){
			return;
		}
		Player player = event.getPlayer();
		plugin.getLogger().info(player.getName() + ": " + ChatColor.stripColor(event.getMessage()));
		event.setCancelled(true);
		for (Player playeri : plugin.getServer().getOnlinePlayers()) {
			if (plugin.getTeam(player.getName()) == plugin.getTeam(playeri.getName()) || playeri.hasPermission("CutePVP.mod")) {
				playeri.sendMessage("<" + player.getDisplayName() + "> " + ChatColor.stripColor(event.getMessage()));
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event) {
		if ( event.getPlayer().getGameMode() == GameMode.CREATIVE ) {
			return;
		}
		int team = plugin.isFlagBlock(event.getBlock().getX(), event.getBlock().getY(), event.getBlock().getZ());
		if (team != -1) {
			event.setCancelled(true);
		}
		if (plugin.getInRangeOfEnemyTeamSpawn(event.getPlayer())) {
			event.getPlayer().sendMessage(ChatColor.DARK_RED + "You cannot build in an enemy base");
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event) {
		if ( event.getPlayer().getGameMode() == GameMode.CREATIVE ) {
			return;
		}
		Player player = event.getPlayer();
                
                int team = plugin.isFlagBlock(event.getBlock().getX(), event.getBlock().getY(), event.getBlock().getZ());
		if (team > 0) {
                    event.setCancelled(true);
                }
                
		//Check if it's a flag
		/*int team = plugin.isFlagBlock(event.getBlock().getX(), event.getBlock().getY(), event.getBlock().getZ());
		if (team != -1) {
			System.out.println("Destroyed a flag block!");
			
			String woolTeamName = plugin.teamNameFromInt(team);
			String carrierTeamName = plugin.teamName(event.getPlayer().getName());
			
			//Enemy player...
			if(woolTeamName != carrierTeamName) {
				plugin.getServer().broadcastMessage(player.getDisplayName() + " has the " + woolTeamName + " team flag!");
				plugin.setFlagCarrier(woolTeamName, player.getName());
				//event.getBlock().setTypeIdAndData(35, event.getBlock().getData(), true);
				ItemStack stack = new ItemStack(35, 1, (short)event.getBlock().getData());
				Inventory inv = event.getPlayer().getInventory();
				inv.addItem(stack);
				event.getBlock().setTypeId(0);
				event.setCancelled(true);
			} else {
				event.setCancelled(true);
			}
		} else {*/
			//If they're in an enemy base...
			if (plugin.getInRangeOfEnemyTeamSpawn(event.getPlayer())) {
				event.getPlayer().sendMessage(ChatColor.DARK_RED + "You cannot build in an enemy base");
				event.setCancelled(true);
			}
		//}
	}
}
