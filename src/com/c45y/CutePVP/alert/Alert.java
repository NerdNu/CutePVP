package com.c45y.CutePVP.alert;

import com.c45y.CutePVP.CutePVP;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;

// ----------------------------------------------------------------------------
/**
 * An alert cycle for a specific audience.
 */
public abstract class Alert {

    // ------------------------------------------------------------------------
    /**
     * Constructor.
     *
     * @param plugin the owning plugin.
     */
    public Alert(CutePVP plugin) {
        _plugin = plugin;
    }

    /**
     * Load settings for this alert cycle.
     *
     * @param config the configuration section to load from.
     */
    public final void load(ConfigurationSection config) {
        _config = config;
        _index = _config.getInt("index", 0);
        _messages = _config.getStringList("messages");
        cancel();
        int period = _config.getInt("period", 6000);
        int offset = _config.getInt("offset", period);
        if (!_messages.isEmpty()) {
            _task = _plugin.getServer().getScheduler().scheduleSyncRepeatingTask(_plugin, new Runnable() {
                public void run() {
                    nextAlert();
                }
            }, offset, period);
        }
    }

    /**
     * Save settings for this alert cycle.
     */
    public final void save() {
        _config.set("index", _index);
    }

    /**
     * Cancel the task running this alert cycle.
     */
    public final void cancel() {
        if (_task >= 0) {
            _plugin.getServer().getScheduler().cancelTask(_task);
        }
        _task = -1;
    }

    /**
     * Display the next alert.
     */
    public final void nextAlert() {
        if (!_messages.isEmpty()) {
            displayMessage(formatMessage(_messages.get(_index)));
            _index = (_index + 1) % _messages.size();
        }
    }

    /**
     * Display the given message to the relevant audience.
     *
     * @param message the message.
     */
    protected abstract void displayMessage(String message);

    /**
     * Format the given message to be displayed as a braodcast.
     *
     * @param message the message.
     * @return the formatted message.
     */
    protected abstract String formatMessage(String message);

    // ------------------------------------------------------------------------
    /**
     * The owning plugin.
     */
    protected final CutePVP _plugin;

    /**
     * The configuration section for this team's alerts.
     */
    private ConfigurationSection _config;

    /**
     * The index of the current alert message.
     */
    private int _index;

    /**
     * The list of alert messages for this team.
     */
    private List<String> _messages;

    /**
     * The index of the task running this alert cycle.
     */
    private int _task = -1;

}
