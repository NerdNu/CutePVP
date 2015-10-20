package com.c45y.CutePVP.alert;

import com.c45y.CutePVP.CutePVP;
import com.c45y.CutePVP.Team;
import org.bukkit.ChatColor;

// ----------------------------------------------------------------------------
/**
 * An alert cycle for a specific team.
 */
public class TeamAlert extends Alert {

    // ------------------------------------------------------------------------
    /**
     * Constructor.
     *
     * @param plugin the owning plugin.
     * @param team the team to receive alerts.
     */
    public TeamAlert(CutePVP plugin, Team team) {
        super(plugin);
        _team = team;
    }

    @Override
    protected void displayMessage(String message) {
        _team.message(message, false);
        _plugin.getLogger().info(message);
    }

    @Override
    protected String formatMessage(String message) {
        return _team.encodeTeamColor("[" + _team.getName() + "] ") + ChatColor.WHITE + message;
    }

    // ------------------------------------------------------------------------
    /**
     * The team to receive alerts.
     */
    private Team _team;

}
