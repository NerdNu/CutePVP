package com.c45y.CutePVP.buff;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.bukkit.Location;
import org.bukkit.Material;
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
					TeamBuff teamBuff = new TeamBuff(_plugin);
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
						// If team-specific floor buffs are enabled, index the
						// floor buff according to specificity settings.
						// Some Materials can never be team-specific, however.
						Material material = floorBuff.getMaterialData().getItemType();
						if (_plugin.getConfiguration().TEAM_SPECIFIC_FLOOR_BUFFS &&
							!_immutableFloorBuffMaterials.contains(material)) {
							_mutableFloorBuffMaterials.add(material);

							// Add an entry for every possible team data value.
							for (Team team : _plugin.getTeamManager()) {
								if (floorBuff.affectsPlacingTeam() || floorBuff.affectsEnemyTeam()) {
									_floorBuffs.put(getMaterialDataHash(material.getId(), team.getData()), floorBuff);
								}
							}
						} else {
							// Add an entry just for the data value in config.
							_floorBuffs.put(floorBuff.getMaterialData().hashCode(), floorBuff);
						}
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
	 * @param teamBuffSeconds the total number of seconds from the time a team
	 *        buff is claimed to when it expires.
	 */
	public void applyTeamBuffs(long teamBuffSeconds) {
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
		// Check whether it is the player's own team block or carpet.
		if (teamPlayer.getTeam().isTeamFloor(block)) {
			for (PotionEffect potion : _friendPotions) {
				teamPlayer.getPlayer().addPotionEffect(potion, true);
			}
			return;
		} else {
			// Perhaps it is one of the general floor buff blocks.
			int hash = getMaterialDataHash(block.getTypeId(), block.getData());
			FloorBuff buff = _floorBuffs.get(hash);
			if (buff != null) {
				int ownTeamData = teamPlayer.getTeam().getData();
				if ((buff.affectsPlacingTeam() && block.getData() == ownTeamData) ||
					(buff.affectsEnemyTeam() && (block.getData() != ownTeamData || teamPlayer.isTestingFloorBuffs()))) {
					buff.apply(teamPlayer.getPlayer());
				}
				return;
			}
		}

		// Check whether the block is that of an enemy team.
		for (Team otherTeam : _plugin.getTeamManager()) {
			if (teamPlayer.getTeam() != otherTeam && otherTeam.isTeamFloor(block)) {
				for (PotionEffect potion : _enemyPotions) {
					teamPlayer.getPlayer().addPotionEffect(potion, true);
				}
				return;
			}
		}
	} // applyFloorBuff

	// ------------------------------------------------------------------------
	/**
	 * Record ownership of a floor buff block by setting its data value to that
	 * of the Team placing it.
	 * 
	 * Floor buffs with a data value modified in this way are team-specific;
	 * they apply positive effects to the placing team and/or negative effects
	 * to enemy teams, as enabled in the configuration by the "friend" and
	 * "enemy" flags. Effectively, the block itself stores ownership information
	 * in the data value.
	 * 
	 * This method is only called if Configuration.TEAM_SPECIFIC_FLOOR_BUFFS is
	 * true.
	 * 
	 * @param block the block that was placed.
	 * @param team the Team that will own the placed block.
	 */
	public void setFloorBuffTeam(Block block, Team team) {
		if (_mutableFloorBuffMaterials.contains(block.getType())) {
			block.setData(team.getData());
		}
	}

	// ------------------------------------------------------------------------
	/**
	 * Compute the same hash value as MaterialData.hashCode() for a given block
	 * type ID and data/damage byte.
	 * 
	 * @param materialId the block type ID.
	 * @param data the data/damage byte.
	 * @return the hash code, exactly as it would be computed by MaterialData.
	 */
	public static int getMaterialDataHash(int materialId, byte data) {
		return (materialId << 8) ^ data;
	}

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

	/**
	 * The set of all Materials used in floor buffs whose data value can be set
	 * to that of the Team placing it.
	 * 
	 * This set is computed as an asymmetric set difference: the set of floor
	 * buff materials loaded from the configuration minus those materials listed
	 * in _immutableFloorBuffMaterials.
	 */
	private HashSet<Material> _mutableFloorBuffMaterials = new HashSet<Material>();

	/**
	 * The set of all Materials that can never have a data/damage value modified
	 * when placed as a floor buff.
	 * 
	 * Essentially this is a set of materials where the data value is actually
	 * used for something. It's not a complete set in that it doesn't include
	 * stairs, for example.
	 */
	private HashSet<Material> _immutableFloorBuffMaterials = new HashSet<Material>(
		Arrays.asList(Material.WOOL, Material.STAINED_CLAY, Material.QUARTZ_BLOCK));
} // class BuffManager