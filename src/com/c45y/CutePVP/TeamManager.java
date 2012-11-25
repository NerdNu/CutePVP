package com.c45y.CutePVP;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class TeamManager {
	private CutePVP cp;
	public Team staffTeam;
	public Team redTeam;
	public Team blueTeam;
	public Team yellowTeam;
	public Team greenTeam;

	public TeamManager(CutePVP cutePVP) {
		cp = cutePVP;
		staffTeam = new Team(cp,  cp.getServer(), cp.getConfig(), "staff", ChatColor.RED, (byte) 15);
		redTeam = new Team(cp,  cp.getServer(), cp.getConfig(), "red", ChatColor.RED, (byte) 14);
		blueTeam = new Team(cp,  cp.getServer(), cp.getConfig(), "blue", ChatColor.BLUE, (byte) 3);
		yellowTeam = new Team(cp, cp.getServer(), cp.getConfig(), "yellow", ChatColor.YELLOW, (byte) 4);
		greenTeam = new Team(cp, cp.getServer(), cp.getConfig(), "green", ChatColor.GREEN, (byte) 5);
	}

	public void onFirstJoin(String player) {
		/* Throw all staff into one team, makes it easy to track them */
		if( cp.getServer().getPlayer(player).hasPermission("CutePVP.mod")) {
			staffTeam.addPlayer(player);
			cp.getServer().getPlayer(player).sendMessage(staffTeam.encodeTeamColor("Welcome to the STAFF team!"));
			return;
		}

		/* If not a staff member decide what team to put them in */

		int value = decideTeam(player);
		if(value == 0) {
			redTeam.addPlayer(player);
			cp.getServer().getPlayer(player).sendMessage(redTeam.encodeTeamColor("Welcome to the RED team!"));
			return;
		}
		if(value == 1) {
			blueTeam.addPlayer(player);
			cp.getServer().getPlayer(player).sendMessage(blueTeam.encodeTeamColor("Welcome to the BLUE team!"));
			return;
		}
		if(value == 2) { 
			yellowTeam.addPlayer(player);
			cp.getServer().getPlayer(player).sendMessage(yellowTeam.encodeTeamColor("Welcome to the YELLOW team!"));
			return;
		}
		if(value == 3) { 
			greenTeam.addPlayer(player);
			cp.getServer().getPlayer(player).sendMessage(greenTeam.encodeTeamColor("Welcome to the GREEN team!"));
			return;
		}
	}

	public int decideTeam(String inpt) {
		int redSize = redTeam.getTeamMembersOnline().size();
		int blueSize = redTeam.getTeamMembersOnline().size();
		int yellowSize = redTeam.getTeamMembersOnline().size();
		int greenSize = redTeam.getTeamMembersOnline().size();
		int mostPlayers = Math.max(Math.max(redSize, blueSize), Math.max(yellowSize, greenSize));
		int sumDelta = mostPlayers - redSize + mostPlayers - blueSize + mostPlayers - yellowSize + mostPlayers - greenSize;
		int[] weights = {25, 25, 25, 25};
		if (sumDelta > 0) {
			weights[0] = (mostPlayers - redSize) / sumDelta * 100;
			weights[1] = (mostPlayers - blueSize) / sumDelta * 100;
			weights[2] = (mostPlayers - yellowSize) / sumDelta * 100;
			weights[3] = (mostPlayers - greenSize) / sumDelta * 100;
		}
		int random = 0 + (int)(Math.random() * ((100 - 0) + 1));

		int team = random / 25;
		if (0 <= random && random <= weights[0]) {
			team = 0;
		}
		if (weights[0] < random && random <= (weights[0]+weights[1])) {
			team = 1;
		}
		if ((weights[0]+weights[1]) < random && random <= (weights[0]+weights[1]+weights[2])) {
			team = 2;
		}
		if ((weights[0]+weights[1]+weights[2]) < random && random <= 100) {
			team = 3;
		}

		cp.getLogger().info("Player=" + inpt + " Team=" + team + " Random=" + random + " weights=" + weights[0] + "," + weights[1] + "," + weights[2] + "," + weights[3] + " Red=" + redSize + " Blue=" + blueSize + " Yellow=" + yellowSize + " Green=" + greenSize);
		return team;

/*
		int value = Character.getNumericValue(ChatColor.stripColor(inpt).charAt(inpt.length()-1));
		for(int i: new int[] { 10, 11, 23, 30, 33 } ) {
			if(value == i) {
				return 0;
			}
		}
		for(int i: new int[] { 14, 17, 28, 21, 29 } ) {
			if(value == i) {
				return 1;
			}
		}
		for(int i: new int[] { 12, 13, 15, 18, 25, 31, 32 } ) {
			if(value == i) {
				return 2;
			}
		}
		for(int i: new int[] { 0, 16, 19, 20, 22, 24, 27, 34, 35 } ) {
			if(value == i) {
				return 3;
			}
		}
		return 0;
*/
	}

	public void messageTeam(String s1, String m1) {
		/* Make sure staff receive all messages. */
		staffTeam.message(m1);
		if (redTeam.getTeamName().equals(s1)) {
			redTeam.message(m1);
			return;
		}
		if (blueTeam.getTeamName().equals(s1)) {
			blueTeam.message(m1);
			return;
		}
		if (yellowTeam.getTeamName().equals(s1)) {
			yellowTeam.message(m1);
			return;
		}
		if (greenTeam.getTeamName().equals(s1)) {
			greenTeam.message(m1);
			return;
		}
	}

	public Team getTeamMemberOf(String player){
		if (staffTeam.inTeam(player)) {
			return staffTeam;
		}
		if (redTeam.inTeam(player)) {
			return redTeam;
		}
		if (blueTeam.inTeam(player)) {
			return blueTeam;
		}
		if (yellowTeam.inTeam(player)) {
			return yellowTeam;
		}
		if (greenTeam.inTeam(player)) {
			return greenTeam;
		}
		return null;
	}
	
	public Team getTeamFromWool(byte data) {
		if (redTeam.getTeamWoolData() == data) {
			return redTeam;
		}
		if (blueTeam.getTeamWoolData() == data) {
			return blueTeam;
		}
		if (yellowTeam.getTeamWoolData() == data) {
			return yellowTeam;
		}
		if (greenTeam.getTeamWoolData() == data) {
			return greenTeam;
		}
		return null;
	}
        
        public boolean inEnemyTeamSpawn(Player player) {
                Location playerLocation = player.getLocation();
		Team playerTeam = getTeamMemberOf(player.getName());
		if(playerTeam != redTeam) {
			if(redTeam.inTeamSpawn(playerLocation)) {
				return true;
			}
		}
		if(playerTeam != blueTeam) {
			if(blueTeam.inTeamSpawn(playerLocation)) {
				return true;
			}
		}
		if(playerTeam != yellowTeam) {
			if(yellowTeam.inTeamSpawn(playerLocation)) {
				return true;
			}
		}
		if(playerTeam != greenTeam) {
			if(greenTeam.inTeamSpawn(playerLocation)) {
				return true;
			}
		}
		return false;
        }
	
	public boolean inRangeOfEnemyTeamSpawn(Player player) {
		Location playerLocation = player.getLocation();
		Team playerTeam = getTeamMemberOf(player.getName());
                if(redTeam.inTeamBase(playerLocation)) {
                        return playerTeam == redTeam;
                }
                if(blueTeam.inTeamBase(playerLocation)) {
                        return playerTeam == blueTeam;
                }
                if(yellowTeam.inTeamBase(playerLocation)) {
                        return playerTeam == yellowTeam;
                }
                if(greenTeam.inTeamBase(playerLocation)) {
                        return playerTeam == greenTeam;
                }
		return false;
	}
	
	public Team isFlagBearer(Player player) {
		if (redTeam.flagHolder == player) {
			return redTeam;
		}
		if (blueTeam.flagHolder == player) {
			return blueTeam;
		}
		if (yellowTeam.flagHolder == player) {
			return yellowTeam;
		}
		if (greenTeam.flagHolder == player) {
			return greenTeam;
		}
		return null;
	}
	
	public Team isFlagBlock(Location loc) {
		if (redTeam.getTeamFlag() == loc) {
			return redTeam;
		}
		if (blueTeam.getTeamFlag() == loc) {
			return blueTeam;
		}
		if (yellowTeam.getTeamFlag() == loc) {
			return yellowTeam;
		}
		if (greenTeam.getTeamFlag() == loc) {
			return greenTeam;
		}
		return null;
	}
	
	
	public boolean shouldTakeDamageFromBlock(Block block,String player) {
		if (block.getType() == Material.WOOL) {
			if (block.getData() == 14 || block.getData() == 3 || block.getData() == 4 || block.getData() == 5) {
				if (block.getData() != getTeamMemberOf(player).getTeamWoolData()) {
					return true;
				}
			}
		}
		return false;
	}
}
