package com.c45y.CutePVP.alert;

import com.c45y.CutePVP.CutePVP;
import com.c45y.CutePVP.Team;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;

// ----------------------------------------------------------------------------
/**
 * Periodically broadcasts alerts to each team.
 */
public class AlertManager {

    // ------------------------------------------------------------------------
    /**
     * Constructor.
     * @param plugin the owning plugin.
     */
    public AlertManager(CutePVP plugin) {
        _plugin = plugin;
        _globalAlert = new GlobalAlert(_plugin);
        _teamAlerts = new HashMap<Team, Alert>();
    }

    /**
     * Load all alert settings.
     */
    public void load() {
        _globalAlert.cancel();
        for (Alert alert : _teamAlerts.values()) {
            alert.cancel();
        }

        ConfigurationSection alertsSection = _plugin.getConfig().getConfigurationSection("alerts");
        if (alertsSection != null) {
            if (alertsSection.isConfigurationSection("global")) {
                _globalAlert.load(alertsSection.getConfigurationSection("global"));
            }
            for (Team team : _plugin.getTeamManager()) {
                if (!_teamAlerts.containsKey(team)) {
                    _teamAlerts.put(team, new TeamAlert(_plugin, team));
                }
            }
            for (Map.Entry<Team, Alert> entry : _teamAlerts.entrySet()) {
                if (alertsSection.isConfigurationSection(entry.getKey().getId())) {
                    entry.getValue().load(alertsSection.getConfigurationSection(entry.getKey().getId()));
                }
            }
        }
    }

    /**
     * Save all alert settings.
     */
    public void save() {
        _globalAlert.save();
        for (Alert alert: _teamAlerts.values()) {
            alert.save();
        }
    }

    // ------------------------------------------------------------------------
    /**
     * The owning plugin.
     */
    private CutePVP _plugin;

    /**
     * The global alert cycle.
     */
    private Alert _globalAlert;

    /**
     * A collection of team alert cycles.
     */
    private Map<Team, Alert> _teamAlerts;

}
