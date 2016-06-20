package com.c45y.CutePVP;

// ----------------------------------------------------------------------------

import com.c45y.CutePVP.buff.TeamBuff;
import com.c45y.CutePVP.util.Util;
import nu.nerd.nerdboard.NerdBoard;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages updates to the scoreboard.
 */
public class ScoreboardManager {

    // ------------------------------------------------------------------------
    /**
     * Constructor.
     *
     * @param plugin the owning plugin.
     */
    public ScoreboardManager(CutePVP plugin) {
        _plugin = plugin;
        _teams = new LinkedHashMap<Team, ScoreboardTeam>();
        _nerdBoard = (NerdBoard) _plugin.getServer().getPluginManager().getPlugin("NerdBoard");
        if (_nerdBoard == null) {
            _plugin.getLogger().severe("NerdBoard is required for Scoreboard functions. http://github.com/nerdnu/NerdBoard");
        }
    }

    /**
     * Enable the scoreboard feature.
     */
    public void enable() {
        if (!_enabled) {
            _enabled = true;
            _scoreboard = _nerdBoard.getScoreboard();
            _scoreObjective = _scoreboard.registerNewObjective("ctfscore", "dummy");
            _scoreObjective.setDisplaySlot(DisplaySlot.SIDEBAR);
            _scoreObjective.setDisplayName(ChatColor.BOLD + "Score");
            boolean first = true;
            for (Team team : _plugin.getTeamManager()) {
                _teams.put(team, new ScoreboardTeam(team, first));
                first = false;
            }
        }
        int line = 1;
        for (ScoreboardTeam team : _teams.values()) {
            line = team.renumber(line);
        }
    }

    /**
     * Sets the player's scoreboard to the CutePVP scoreboard.
     * @param player the player.
     */
    public void assignPlayer(Player player) {
        if (_enabled) {
            player.setScoreboard(_scoreboard);
        }
    }

    /**
     * Add the player to a Bukkit scoreboard team for colorization
     * @param player the player to ad
     */
    public void addPlayer(Player player, Team team) {
        if (_enabled) {
            ScoreboardTeam sTeam = _teams.get(team);
            if (sTeam != null) {
                sTeam.addPlayer(player);
            }
        }
    }

    /**
     * Remove the player from a Bukkit scoreboard
     * @param player the player to ad
     */
    public void removePlayer(Player player, Team team) {
        if (_enabled) {
            ScoreboardTeam sTeam = _teams.get(team);
            if (sTeam != null) {
                sTeam.removePlayer(player);
            }
        }
    }

    /**
     * Refresh the score of the given team.
     *
     * @param team the team.
     */
    public void refreshTeamScore(Team team) {
        if (_enabled) {
            ScoreboardTeam sTeam = _teams.get(team);
            if (sTeam != null) {
                sTeam.refreshScore();
            }
        }
    }

    /**
     * Increment the number of online players for the given team by 1.
     *
     * @param team the team.
     */
    public void incrementTeamPlayers(Team team) {
        if (_enabled) {
            ScoreboardTeam sTeam = _teams.get(team);
            if (sTeam != null) {
                sTeam.incrementPlayers();
            }
        }
    }

    /**
     * Decrement the number of online players for the given team by 1.
     *
     * @param team the team.
     */
    public void decrementTeamPlayers(Team team) {
        if (_enabled) {
            ScoreboardTeam sTeam = _teams.get(team);
            if (sTeam != null) {
                sTeam.decrementPlayers();
            }
        }
    }

    /**
     * Refresh the potion effects being applied to all teams by buffs.
     */
    public void refreshTeamEffects() {
        int line = 1;
        if (_enabled) {
            for (ScoreboardTeam team : _teams.values()) {
                if (team != null) {
                    team.refreshEffects();
                    line = team.renumber(line);
                }
            }
        }
    }

    /**
     * Refresh all the things!
     */
    public void refresh() {
        if (_enabled) {
            for (ScoreboardTeam t : _teams.values()) {
                t.refresh();
            }
        }
    }

    // ------------------------------------------------------------------------
    /**
     * The owning plugin.
     */
    private CutePVP _plugin;

    /**
     * Whether the scoreboard feature is enabled.
     */
    private boolean _enabled;

    /**
     * The underlying scoreboard.
     */
    private Scoreboard _scoreboard;

    /**
     * The scoreboard's score objective.
     */
    private Objective _scoreObjective;

    /**
     * A map from teams to the internal scoreboard team representation.
     */
    private Map<Team, ScoreboardTeam> _teams;

    /**
     * The NerdBoard instance, for compatibility with ModMode
     */
    private NerdBoard _nerdBoard;


    // -----------------------------------------------------------------------
    /**
     * A container for the team data displayed on the scoreboard.
     */
    private class ScoreboardTeam {

        // -------------------------------------------------------------------
        /**
         * Constructor.
         *
         * @param team the team.
         * @param first whether this is the first team being drawn.
         */
        public ScoreboardTeam(Team team, boolean first) {
            _team = team;
            _bukkitTeam = _scoreboard.registerNewTeam(_team.getId());
            _bukkitTeam.setDisplayName(_team.getName());
            _bukkitTeam.setPrefix(team.getTeamChatColor() + "");
            _teamScore = _scoreObjective.getScore(_team.getTeamChatColor().toString() + ChatColor.BOLD.toString()
                    + _team.getName());
            _scoreScore = null;
            _playersScore = null;
            _effectsScores = new ArrayList<Score>();
            _emptyScore = first ? null : _scoreObjective.getScore(_team.getTeamChatColor().toString());
            refreshScore();
            refreshPlayers();
            refreshEffects();
        }

        /**
         * Renumber each line, starting from the given line number.
         *
         * @param index the starting line number.
         * @return the next line number to display.
         */
        public int renumber(int index) {
            if (_emptyScore != null) {
                _emptyScore.setScore(index++);
            }
            for (int i = 0; i < _effectsScores.size(); i++) {
                _effectsScores.get(_effectsScores.size() - i - 1).setScore(index++);
            }
            _playersScore.setScore(index++);
            _scoreScore.setScore(index++);
            _teamScore.setScore(index++);
            return index;
        }

        /**
         * Refresh the team's overall score.
         */
        public void refreshScore() {
            _score = _team.getScore().score.get();
            int line = 0;
            if (_scoreScore != null) {
                line = _scoreScore.getScore();
                _scoreboard.resetScores(_scoreScore.getEntry());
            }
            _scoreScore = _scoreObjective.getScore(_team.getTeamChatColor().toString() + ChatColor.WHITE.toString()
                    + "Score: " + _score);
            _scoreScore.setScore(line);
        }

        /**
         * Set the team's displayed player count to the current value.
         */
        private void resetPlayersScore() {
            int line = 0;
            if (_playersScore != null) {
                line = _playersScore.getScore();
                _scoreboard.resetScores(_playersScore.getEntry());
            }
            _playersScore = _scoreObjective.getScore(_team.getTeamChatColor().toString() + ChatColor.WHITE.toString()
                    + "Players: " + _players);
            _playersScore.setScore(line);
        }

        /**
         * Refresh the number of online players the team has.
         */
        public void refreshPlayers() {
            _players = _team.getOnlineMembers().size();
            resetPlayersScore();
        }

        /**
         * Increment the number of players shown by 1.
         */
        public void incrementPlayers() {
            _players++;
            resetPlayersScore();
        }

        /**
         * Decrement the number of players shown by 1.
         */
        public void decrementPlayers() {
            _players--;
            resetPlayersScore();
        }

        /**
         * Refresh the potion effects being applied to the team by buffs.
         */
        public void refreshEffects() {
            for (Score score : _effectsScores) {
                _scoreboard.resetScores(score.getEntry());
            }
            _effectsScores = new ArrayList<Score>();
            for (TeamBuff buff : _plugin.getBuffManager()) {
                if (buff.getTeam() == _team) {
                    for (PotionEffect effect : buff) {
                        _effectsScores.add(_scoreObjective.getScore(_team.getTeamChatColor().toString()
                                + ChatColor.GREEN.toString() + Util.formatEffect(effect)));
                    }
                }
            }
        }

        /**
         * Refresh all team properties.
         */
        public void refresh() {
            refreshScore();
            refreshPlayers();
            refreshEffects();
        }

        public void addPlayer(Player player) {
            _nerdBoard.addPlayerToTeam(_bukkitTeam, player);
        }

        public void removePlayer(Player player) {
            _nerdBoard.removePlayerFromTeam(_bukkitTeam, player);
        }

        // -------------------------------------------------------------------
        /**
         * The associated {@link Team}.
         */
        private final Team _team;

        /**
         * Bukkit scoreboard team object for colorization
         */
        private org.bukkit.scoreboard.Team _bukkitTeam;

        /**
         * The Score displaying the team's name.
         */
        private Score _teamScore;

        /**
         * The team's overall score.
         */
        private int _score;

        /**
         * The Score representing the team's overall score.
         */
        private Score _scoreScore;

        /**
         * The number of online players the team has.
         */
        private int _players;

        /**
         * The Score representing the number of online players the team has.
         */
        private Score _playersScore;

        /**
         * The scores representing the potion effects currently being applied
         * to the team by buffs.
         */
        private List<Score> _effectsScores;

        /**
         * The Score displaying an empty line below this team's stats.
         */
        private Score _emptyScore;

    }

}
