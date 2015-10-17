package com.c45y.CutePVP.alert;

import com.c45y.CutePVP.CutePVP;
import org.bukkit.ChatColor;

// ----------------------------------------------------------------------------
/**
 * An alert cycle to be broadcast globally.
 */
public class GlobalAlert extends Alert {

    // ------------------------------------------------------------------------
    /**
     * Constructor.
     *
     * @param plugin the owning plugin.
     */
    public GlobalAlert(CutePVP plugin) {
        super(plugin);
    }

    @Override
    protected void displayMessage(String message) {
        _plugin.getServer().broadcastMessage(message);
    }

    @Override
    protected String formatMessage(String message) {
        return ChatColor.LIGHT_PURPLE + "[Alert] " + ChatColor.WHITE + message;
    }

}
