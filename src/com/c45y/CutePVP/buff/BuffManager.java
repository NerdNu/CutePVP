package com.c45y.CutePVP.buff;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.potion.PotionEffect;

import com.c45y.CutePVP.CutePVP;
import com.c45y.CutePVP.Team;
import com.c45y.CutePVP.TeamPlayer;
import com.c45y.CutePVP.util.ConfigHelper;
import com.c45y.CutePVP.util.Util;

// ----------------------------------------------------------------------------
/**
 * Manages the application of potion effects (buffs) according to player contact
 * with special blocks.
 */
public class BuffManager implements Iterable<TeamBuff> {
	// ------------------------------------------------------------------------
	/**
	 * Constructor.
	 * 
	 * @param plugin the owning plugin.
	 */
	public BuffManager(CutePVP plugin) {
		_plugin = plugin;
	}

	// ------------------------------------------------------------------------
	/**
	 * Load all buffs.
	 */
	public void load() {
		_floorBuffs.clear();
		_teamBuffs.clear();

		// Load the team buffs.
		ConfigurationSection teamBuffsSection = _plugin.getConfig().getConfigurationSection("buffs.team");
		if (teamBuffsSection != null) {
			for (String id : teamBuffsSection.getKeys(false)) {
				ConfigurationSection section = teamBuffsSection.getConfigurationSection(id);
				if (section != null) {
					TeamBuff teamBuff = new TeamBuff();
					if (teamBuff.load(section, _plugin.getTeamManager(), _plugin.getLogger())) {
						_teamBuffs.add(teamBuff);
					}
				}
			}
		}

		// Load the potion effects for own team and enemy team blocks.
		ConfigHelper helper = new ConfigHelper(_plugin.getLogger());
		_friendPotions = helper.loadPotions(_plugin.getConfig().getConfigurationSection("buffs.friend"), "", true);
		_enemyPotions = helper.loadPotions(_plugin.getConfig().getConfigurationSection("buffs.enemy"), "", true);

		// Load the floor block buffs.
		ConfigurationSection floorBuffsSection = _plugin.getConfig().getConfigurationSection("buffs.block");
		if (floorBuffsSection != null) {
			for (String id : floorBuffsSection.getKeys(false)) {
				ConfigurationSection section = floorBuffsSection.getConfigurationSection(id);
				if (section != null) {
					FloorBuff floorBuff = new FloorBuff();
					if (floorBuff.load(section, _plugin.getLogger())) {
						_floorBuffs.put(floorBuff.getMaterialData().hashCode(), floorBuff);
					}
				}
			}
		}
	} // load

	// ------------------------------------------------------------------------
	/**
	 * Save updated buff locations.
	 * 
	 * Only the team buffs can be be modified in-game - by admin commands.
	 */
	public void save() {
		ConfigurationSection teamBuffsSection = _plugin.getConfig().getConfigurationSection("buffs.team");
		if (teamBuffsSection != null) {
			for (TeamBuff teamBuff : _teamBuffs) {
				ConfigurationSection section = teamBuffsSection.getConfigurationSection(teamBuff.getId());
				teamBuff.save(section, _plugin.getLogger());
			}
		}
	}

	// ------------------------------------------------------------------------
	/**
	 * Return an iterator over all {@link TeamBuff}s.
	 * 
	 * @return an iterator over all {@link TeamBuff}s.
	 */
	public Iterator<TeamBuff> iterator() {
		return _teamBuffs.iterator();
	}

	// --------------------------------------------------------------------------
	/**
	 * Return the {@link TeamBuff} with the specified programmatic ID, or null
	 * if not found.
	 * 
	 * @param buffId the requested ID.
	 * @return the {@link TeamBuff} with the specified programmatic ID, or null
	 *         if not found.
	 */
	public TeamBuff getTeamBuff(String buffId) {
		for (TeamBuff buff : _teamBuffs) {
			if (buff.getId().equals(buffId)) {
				return buff;
			}
		}
		return null;
	}

	// --------------------------------------------------------------------------
	/**
	 * If the specified Block is a buff, return the corresponding
	 * {@link TeamBuff}.
	 * 
	 * @param block the block in question.
	 * @return the TeamBuff corresponding to block, or null if not a buff block.
	 */
	public TeamBuff getTeamBuffFromBlock(Block block) {
		Location loc = block.getLocation();
		for (TeamBuff teamBuff : _teamBuffs) {
			if (Util.isSameBlock(loc, teamBuff.getLocation())) {
				return teamBuff;
			}
		}
		return null;
	}

	// ------------------------------------------------------------------------
	/**
	 * Apply team buffs to teams that have them.
	 * 
	 * @param teamBuffSeconds the total number of seconds from the time a team buff
	 *        is claimed to when it expires.
	 */
	public void applyTeamBuffs(long teamBuffSeconds) {
		_plugin.getLogger().info("Running buff.");
		for (TeamBuff teamBuff : _teamBuffs) {
			teamBuff.update(teamBuffSeconds);
		}
	}

	// ------------------------------------------------------------------------
	/**
	 * Apply a buff to the player when they walk on a block.
	 * 
	 * <ul>
	 * <li>If the block is the enemy team's wool, apply getEnemyBlockBuff().</li>
	 * <li>If the block is the own team's wool, apply getFriendBlockBuff().</li>
	 * <li>If the block is one of those defined in the configuration, apply the
	 * corresponding buff.</li>
	 * 
	 * @param block the Block that the player is standing on.
	 * @param teamPlayer the player, who must belong to a team.
	 */
	public void applyFloorBuff(Block block, TeamPlayer teamPlayer) {
		// Check whether it is the player's own team block.
		if (teamPlayer.getTeam().isTeamBlock(block)) {
			for (PotionEffect potion : _friendPotions) {
				teamPlayer.getPlayer().addPotionEffect(potion, true);
			}
			return;
		} else {
			// Perhaps it is one of the non-team-specific buff blocks.
			// NOTE: this key must be the same as MaterialData.hashCode().
			int hash = (block.getTypeId() << 8) ^ block.getData();
			FloorBuff buff = _floorBuffs.get(hash);
			if (buff != null) {
				buff.apply(teamPlayer.getPlayer());
				return;
			}
		}

		// Check whether the block is that of an enemy team.
		for (Team otherTeam : _plugin.getTeamManager()) {
			if (teamPlayer.getTeam() != otherTeam && otherTeam.isTeamBlock(block)) {
				for (PotionEffect potion : _enemyPotions) {
					teamPlayer.getPlayer().addPotionEffect(potion, true);
				}
				return;
			}
		}
	} // applyFloorBuff

	// ------------------------------------------------------------------------
	/**
	 * The owning plugin.
	 */
	private CutePVP _plugin;

	/**
	 * The set of potion effects applied when a player walks on an own team
	 * block.
	 */
	private HashSet<PotionEffect> _friendPotions = new HashSet<PotionEffect>();

	/**
	 * The set of potion effects applied when a player walks on an enemy team
	 * block.
	 */
	private HashSet<PotionEffect> _enemyPotions = new HashSet<PotionEffect>();

	/**
	 * FloorBuff applied when walking on blocks with the specified MaterialData
	 * hash value.
	 * 
	 * Since this map only contains keys for actual blocks, the
	 * MaterialData.hash() value will always correspond exactly to an implied
	 * MaterialData instance, but without the GC overhead of creating one.
	 */
	private HashMap<Integer, FloorBuff> _floorBuffs = new HashMap<Integer, FloorBuff>();

	/**
	 * The {@link TeamBuff}s.
	 */
	private ArrayList<TeamBuff> _teamBuffs = new ArrayList<TeamBuff>();

} // class BuffManager