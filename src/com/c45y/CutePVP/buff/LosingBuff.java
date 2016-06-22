package com.c45y.CutePVP.buff;


import com.c45y.CutePVP.CutePVP;
import com.c45y.CutePVP.Messages;
import com.c45y.CutePVP.Team;
import com.c45y.CutePVP.TeamManager;
import com.c45y.CutePVP.util.ConfigHelper;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.logging.Logger;

/**
 * Potion effects applied to a team's players when they're significantly behind in points,
 * so they have more of a chance of catching up from a major setback.
 */
public class LosingBuff extends Buff {


    private CutePVP plugin;
    private Integer disparity;
    private Team lastLosingTeam = null;


    public LosingBuff(CutePVP plugin) {
        this.plugin = plugin;
    }


    /**
     * Load the buff from the specified configuration section
     * @param section the configuration section.
     * @param logger used to log messages.
     * @return true if successful
     */
    public boolean load(ConfigurationSection section, Logger logger) {
        try {
            ConfigHelper helper = new ConfigHelper(logger);
            if (super.load(section, logger)) {
                disparity = section.getInt("disparity", 10);
                return true;
            }
        } catch (Exception ex) {
            logger.severe("Unable to load losing team buff " + section.getName());
        }
        return false;
    }


    /**
     * Refresh the buff application on players who qualify
     */
    public void update() {
        TeamManager teamManager = plugin.getTeamManager();
        Team losingTeam = getLosingTeam(teamManager);
        if (losingTeam != null) {
            for (Player player : losingTeam.getOnlineMembers()) {
                apply(player);
            }
        }
        broadcast();
    }


    /**
     * Get the team that's behind in captures enough to trigger the buff, if applicable
     * @param teamManager the team manager
     * @return a Team or null
     */
    private Team getLosingTeam(TeamManager teamManager) {
        Team highest = null;
        Team lowest = null;
        Team losing = null;
        for (Team team : teamManager) {
            Integer caps = team.getScore().captures.get();
            if (highest == null || caps.compareTo(highest.getScore().captures.get()) > 0) {
                highest = team;
            }
            if (lowest == null || caps.compareTo(lowest.getScore().captures.get()) < 0) {
                lowest = team;
            }
        }
        if (highest != null && lowest != null) {
            if (highest.getScore().captures.get() - lowest.getScore().captures.get() >= disparity) {
                losing = lowest;
            }
        }
        return losing;
    }


    /**
     * Broadcast that the losing buff has been applied
     */
    private void broadcast() {
        TeamManager teamManager = plugin.getTeamManager();
        Team losingTeam = getLosingTeam(teamManager);
        if (losingTeam != null) {
            if (lastLosingTeam == null) {
                String team = String.format("%s%s", losingTeam.getTeamChatColor(), losingTeam.getName());
                Messages.broadcast(team + Messages.BROADCAST_COLOR + " is making a heroic comeback!");
                lastLosingTeam = losingTeam;
            }
        } else {
            lastLosingTeam = null;
        }
    }


}
