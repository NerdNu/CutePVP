CutePVP
=======

A capture-the-flag, team-based PVP game for Minecraft Bukkit servers.


Team Insignia
-------------

 * Each team (as specified in the configuration file) has a distinctive color, which they wear as a wool block on their head.
 * Chat messages about teams and by team members are indicated by the team color.
 

Game Mechanics
--------------

 * Each team owns (defends) one or more flags, which is a wool block of the team's color.
 * Right-clicking on an opposition team's flag "steals" it.
 * To score, you must steal a flag and carry it back to your base, then click on one of your own team flags.  This is called a "capture".
 * If you steal a flag and log out or die, the flag is dropped on the ground and will return back to its home position a configurable number of minutes later.  
 * The opposition team can also prevent you from scoring by stealing all of your flags.  Because then you can't click on any of them.
 * The player who carries the flag can be discerned by (harmless) flames that appear around him or her.
 * Each player's compass will point to the nearest flag stolen from his or her team.
 * If none of the team's flags are stolen, the compass will point to the nearest flag's home position.


Buffs
-----

 * Buffs are distinguished blocks, similar to flags. But unlike flags, they never move.
 * They will typically be placed in hard-to-reach locations.
 * When a team member right-clicks on one, all members of the team receive beneficial potion effects for a configurable duration (several minutes).
 * There may be multiple buffs, and each one may confer different beneficial potion effects to the team.


Power Blocks
------------

Depending on configuration settings, wool of various colors may be employed as smart building materials to give teams a tactical advantage:

 * Stepping on wool of an opposition team's color may harm the player by imparting a detrimental potion effect.
 * Stepping on wool of the player's own team's color may impart a beneficial potion effect.
 * Various other kinds of blocks may be configured to impart potion effects when walked on.


Map Configuration
-----------------

Each team must have:

 * A single spawn point, set with the /cutepvp setspawn command.
 * Zero or more WorldGuard regions *per team* for the team base.  These regions must be configured with the WorldGuard "build" flag set to "allow" so that anyone may edit within them.  The CutePVP plugin will, however, prevent *opposition* team members from editing or PVP'ing within these regions.
 * One or more flags, set with the /cutepvp addflag command.  They should be of the same material as the helmets of the team guarding them and should be in a WorldGuard region that no player can edit.
 * One or more buffs, set with the /cutepvp addbuff command.  They can be of any material (e.g. a beacon) and should be in a WorldGuard region that no player can edit. 


Moderation
----------
 * Players given the CutePVP.exempt permission will be excluded from team membership.  
 * Moderators with the CutePVP.mod permission can see all team chats.
 * Administrators with the CutePVP.admin permission can set spawns, flags and buffs, in addition to moderator powers.
 * Both administrators and moderators automatically inherit the CutePVP.exempt permission and therefore cannot join a team.
 * However, when using the ModMode plugin, moderators can join a team when not in moderator mode.
 
