CutePVP
=======

A capture-the-flag, team-based PVP game for Minecraft Bukkit servers.


Team Insignia
-------------

 * Each team (as specified in the configuration file) has a distinctive color, which they wear as a wool block on their head.
 * Chat messages about teams and by team members are indicated by the team color.
 

Game Mechanics
--------------

 * Players are assigned a team once they run /join
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

 * A single spawn point, set with the ```/cutepvp setspawn``` command.
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


Commands
--------

### Player Commands

 * To be assigned a team, players must run the command `/join`
 * A list of teams can be accessed via the use of `/teams`
 * Team based score can be retrieved with `/score`
 * The nearest flag location can be retrieved by running `/flag`
 * To forcefully drop a flag you are holding, run `/drop`

### Setting Up Spawns

There are 4 predefined spawns in the default configuration:

 * The first join spawn, named "firstjoin", where the player spawns the first time he logs in.  This defaults to a location in The End.
 * The non-team spawn, named "nonteam", where players who have not ever joined a team respawn if they die.  This also defaults to a location in The End.
 * The Red Team spawn, named "red".
 * The Blue Team spawn, named "blue".
 
To set a spawn, stand in the desired location, looking in the direction that the player should look and run the ```/cutepvp setspawn <name>``` command:
```
/cutepvp setspawn firstjoin
/cutepvp setspawn nonteam
/cutepvp setspawn red
/cutepvp setspawn blue
```


### Setting Up Flags

Flags can be listed as follows:
```
/cutepvp flag list
````

Each team has two flags, named "one" and "two", predefined in the default config.yml.  Run the ```/cutepvp flag set <team> <flag>``` command while staring at wool of the correct color to set the location of each flag.  

```
/cutepvp flag set red one
/cutepvp flag set red two
/cutepvp flag set blue one
/cutepvp flag set blue two
```

There are no commands to add or remove flags.  If you need more or less flags, you must stop the server and edit config.yml by hand.


### Setting Up Buffs

Buffs can be listed as follows:
```
/cutepvp buff list
```

There are two buffs, named NE and SW, predefined in the default config.yml.  Run the ```/cutepvp buff set <name>``` command while staring at a block (e.g. a beacon) to set the location of each buff.
```
/cutepvp buff set NE
/cutepvp buff set SW
```
There are no commands to add or remove buffs.  If you need more or less buffs, you must stop the server and edit config.yml by hand.

