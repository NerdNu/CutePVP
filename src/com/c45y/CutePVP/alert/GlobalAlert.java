package com.c45y.CutePVP.alert;

import com.c45y.CutePVP.CutePVP;
import com.c45y.CutePVP.Messages;
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
        Messages.broadcast(message);
    }

    @Override
    protected String formatMessage(String message) {
        return message;
    }

}
