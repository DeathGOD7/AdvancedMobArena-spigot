package su.nightexpress.ama.config;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.config.api.ILangMsg;
import su.nexmedia.engine.core.config.CoreLang;
import su.nightexpress.ama.AMA;
import su.nightexpress.ama.api.arena.game.ArenaGameEventType;
import su.nightexpress.ama.api.arena.game.ArenaGameTargetType;
import su.nightexpress.ama.api.arena.type.ArenaLockState;
import su.nightexpress.ama.api.arena.type.ArenaState;
import su.nightexpress.ama.stats.StatType;

public class Lang extends CoreLang {
	
	public Lang(@NotNull AMA plugin) {
		super(plugin);
	}
	
	@Override
	protected void setupEnums() {
		this.setupEnum(ArenaState.class);
		this.setupEnum(ArenaLockState.class);
		this.setupEnum(StatType.class);
		this.setupEnum(ArenaGameEventType.class);
		this.setupEnum(ArenaGameTargetType.class);
	}
	
	public ILangMsg Help_Coins = new ILangMsg(
			this, """
			{message: ~prefix: false;}
			&a/ama coins add <player> <amount> &7- Add coins to a player.
			&a/ama coins take <player> <amount> &7- Take coins from a player.
			&a/ama coins set <player> <amount> &7- Set coins for a player.
			""");
	
	public ILangMsg Help_Score = new ILangMsg(
			this, """
			{message: ~prefix: false;}
			&a/ama score add <player> <amount> &7- Add score to a player.
			&a/ama score take <player> <amount> &7- Take score from a player.
			&a/ama score set <player> <amount> &7- Set score for a player.
			""");
	
	public ILangMsg Help_Hologram = new ILangMsg(
			this, """
			{message: ~prefix: false;}
			&a/ama hologram add <type> [arena] &7- Add stats hologram at your location.
			&a/ama hologram remove &7- Remove the nearest hologram.
			""");

	public ILangMsg Command_Coins_Desc = new ILangMsg(this, "Shows coins sub-commands.");
    public ILangMsg Command_Coins_Usage = new ILangMsg(this, "<add|take|set>");
    public ILangMsg Command_Coins_Add_Usage = new ILangMsg(this, "&cUsage: &f/ama coins add <player> <amount>");
    public ILangMsg Command_Coins_Add_Done = new ILangMsg(this, "&aAdded &f%coins% Coins to &a%player%'s &fbalance!");
    public ILangMsg Command_Coins_Take_Usage = new ILangMsg(this, "&cUsage: &f/ama coins take <player> <amount>");
    public ILangMsg Command_Coins_Take_Done = new ILangMsg(this, "&aTaken &f%coins% Coins from &a%player%'s &fbalance!");
    public ILangMsg Command_Coins_Set_Usage = new ILangMsg(this, "&cUsage: &f/ama coins set <player> <amount>");
    public ILangMsg Command_Coins_Set_Done = new ILangMsg(this, "&aSet &a%player%'s &fbalance to &a%coins% Coins&f!");
    
    public ILangMsg Command_ForceStart_Desc = new ILangMsg(this, "Force starts the specified arena.");
    public ILangMsg Command_ForceStart_Usage = new ILangMsg(this, "<arena>");
    public ILangMsg Command_ForceStart_Done = new ILangMsg(this, "Force started arena &a%arena_id%&7!");
    public ILangMsg Command_ForceStart_Error_NotReady = new ILangMsg(this, "This arena is not ready to start or already in-game");
    
    public ILangMsg Command_ForceEnd_Desc = new ILangMsg(this, "Force stops the specified arena.");
    public ILangMsg Command_ForceEnd_Usage = new ILangMsg(this, "<arena>");
    public ILangMsg Command_ForceEnd_Done = new ILangMsg(this, "Force ended arena &a%arena_id%&7!");
    public ILangMsg Command_ForceEnd_Error_NotInGame = new ILangMsg(this, "Arena &c%arena_id% &7is not in game.");
    
    public ILangMsg Command_Holo_Desc = new ILangMsg(this, "Shows hologram sub-commands.");
    public ILangMsg Command_Holo_Usage = new ILangMsg(this, "<add|remove>");
    public ILangMsg Command_Holo_Remove_Nothing = new ILangMsg(this, "&7No holograms in radius of 5 blocks.");
    public ILangMsg Command_Holo_Remove_Done = new ILangMsg(this, "&7Hologram removed!");
    public ILangMsg Command_Holo_Add_Usage = new ILangMsg(this, "&cUsage: &f/ama hologramm add <stat type> [arena]");
    public ILangMsg Command_Holo_Add_Done = new ILangMsg(this, "&7Added hologram for &a%top_type% &7stats!");
    
    public ILangMsg Command_Join_Desc = new ILangMsg(this, "Join the specified (or random) arena.");
    public ILangMsg Command_Join_Usage = new ILangMsg(this, "[arena]");
    public ILangMsg Command_Join_Nothing = new ILangMsg(this, "No available arenas to join.");
    
    public ILangMsg Command_Region_Desc = new ILangMsg(this, "Manage in-game arena regions.");
    public ILangMsg Command_Region_Usage = new ILangMsg(this, "<lock|unlock>");
    public ILangMsg Command_Region_State_Done = new ILangMsg(this, "Region &a%region%'s &7lock state to &a%state%&7!");
    public ILangMsg Command_Region_State_Error_NotInGame = new ILangMsg(this, "&cYou must be in-game to do that!");
    public ILangMsg Command_Region_State_Error_InvalidRegion = new ILangMsg(this, "&cInvalid region id!");
    
    public ILangMsg Command_Score_Desc = new ILangMsg(this, "Manage player's game score.");
    public ILangMsg Command_Score_Usage = new ILangMsg(this, "<add|take|set>");
    public ILangMsg Command_Score_Add_Usage = new ILangMsg(this, "&cUsage: &f/ama score add <player> <amount>");
    public ILangMsg Command_Score_Add_Done = new ILangMsg(this, "&aAdded &f%points% points to &a%player%'s &fscore!");
    public ILangMsg Command_Score_Take_Usage = new ILangMsg(this, "&cUsage: &f/ama score take <player> <amount>");
    public ILangMsg Command_Score_Take_Done = new ILangMsg(this, "&aTaken &f%points% points from &a%player%'s &fscore!");
    public ILangMsg Command_Score_Set_Usage = new ILangMsg(this, "&cUsage: &f/ama score set <player> <amount>");
    public ILangMsg Command_Score_Set_Done = new ILangMsg(this, "&aSet &a%player%'s &fscore to &a%score%&f!");
    public ILangMsg Command_Score_Error_NotInGame = new ILangMsg(this, "&cThis player is not in game!");
    
    public ILangMsg Command_Spot_Desc = new ILangMsg(this, "Manage in-game arena spots.");
    public ILangMsg Command_Spot_Usage = new ILangMsg(this, "[state]");
    public ILangMsg Command_Spot_State_Done = new ILangMsg(this, "Changed &a%spot%'s &7state to &a%state%&7!");
    public ILangMsg Command_Spot_State_Error_NotInGame = new ILangMsg(this, "&cYou must be in-game to do that!");
    public ILangMsg Command_Spot_State_Error_InvalidSpot = new ILangMsg(this, "&cInvalid spot id!");
    public ILangMsg Command_Spot_State_Error_InvalidState = new ILangMsg(this, "&cInvalid spot state id!");
    
    public ILangMsg Command_Leave_Desc = new ILangMsg(this, "Leave the current arena.");
    
    public ILangMsg Command_List_Desc = new ILangMsg(this, "Shows arenas list.");
    
    public ILangMsg Command_Skipwave_Desc = new ILangMsg(this, "Skips current arena wave.");
    public ILangMsg Command_Skipwave_Usage = new ILangMsg(this, "[amount]");
    
    public ILangMsg Command_Spectate_Desc = new ILangMsg(this, "Join as spectator on specified arena.");
    public ILangMsg Command_Spectate_Usage = new ILangMsg(this, "<arena>");
	
    public ILangMsg Command_Shop_Desc = new ILangMsg(this, "Open arena shop.");
    public ILangMsg Command_Stats_Desc = new ILangMsg(this, "View your current stats.");
    
	public ILangMsg Arena_Error_Disabled = new ILangMsg(this, "Arena &c%arena_name% &7is disabled.");
	public ILangMsg Arena_Error_Invalid = new ILangMsg(this, "Arena does not exists.");
	
	public ILangMsg Arena_Join_Error_Permission = new ILangMsg(this, "&cYou don't have permission to join this arena!");
    public ILangMsg Arena_Join_Error_Money      = new ILangMsg(this, "&cYou must have &e%arena_requirement_money% &cto join the arena!");
    public ILangMsg Arena_Join_Error_InGame = new ILangMsg(this, "You are already in game!");
    public ILangMsg Arena_Join_Error_Maximum = new ILangMsg(this, "Arena has maximum players.");
    public ILangMsg Arena_Join_Error_Started = new ILangMsg(this, "Arena &a%arena_name% &7is already in game. You can not join now.");
    
    public ILangMsg Arena_Join_Spectate_Success = new ILangMsg(this, "Now you are spectating arena &a%arena_name%");
    public ILangMsg Arena_Join_Spectate_Error_Disabled = new ILangMsg(this, "Spectating is disabled on this arena.");
    
    public ILangMsg Arena_Game_Notify_Start = new ILangMsg(this, "{message: ~type: TITLES; ~fadeIn: 10; ~stay: 40; ~fadeOut: 10;}&a&lYou have joined the arena! \n &2&lPickup your weapons and fight!");
    public ILangMsg Arena_Game_Notify_Reward = new ILangMsg(this, "You recieved reward: &a%reward_name%");

    public ILangMsg Arena_Game_Announce_End = new ILangMsg(this, "Game on the arena &a%arena_name% &7has ended. Type &a/ama join &a%arena_id% &7to play!");
    public ILangMsg Arena_Game_Announce_Start = new ILangMsg(this, "Arena &a%arena_name% &7will start in &2%time% &7seconds. Type &a/ama join &a%arena_id% &7to join!");
    
	public ILangMsg Arena_Game_Restrict_Commands = new ILangMsg(this, "External commands are disabled on this arena. Type &c/ama leave&7 to leave.");
	public ILangMsg Arena_Game_Restrict_Kits = new ILangMsg(this, "Kits are disabled on this arena.");
	public ILangMsg Arena_Game_Restrict_NoPets = new ILangMsg(this, "Pets are not allowed on this arena. Your pet has gone.");

	public ILangMsg Arena_Game_Lobby_Ready_True = new ILangMsg(this, "&a%player_name% &7is ready to play!");
	public ILangMsg Arena_Game_Lobby_Ready_False = new ILangMsg(this, "&c%player_name% &7is not ready to play...");
	public ILangMsg Arena_Game_Lobby_Enter        = new ILangMsg(this, "{message: ~type: TITLES; ~fadeIn: 10; ~stay: 40; ~fadeOut: 10;}&a&lWelcome to Mob Arena! \n &2&lPlease, choose your kit");
    public ILangMsg Arena_Game_Lobby_Timer        = new ILangMsg(this, "{message: ~type: TITLES; ~fadeIn: 10; ~stay: 40; ~fadeOut: 10;}&e&lThe game will start in \n &a&l%time% seconds!");
    public ILangMsg Arena_Game_Lobby_MinPlayers   = new ILangMsg(this, "Minimum players to start: &c%min%");
    public ILangMsg Arena_Game_Lobby_Joined       = new ILangMsg(this, "&a%player_name% &7has joined the arena.");
    
	public ILangMsg Arena_Game_Death_Lives = new ILangMsg(this, "{message: ~type: TITLES; ~fadeIn: 10; ~stay: 40; ~fadeOut: 10;}&4&lYou Died! \n &cLives left: &e&lx%player_lives%");
    public ILangMsg Arena_Game_Death_Player = new ILangMsg(this, "&c%player_name% &7died! Players left: &c%arena_players%");
    
    public ILangMsg Arena_Game_Wave_Latest = new ILangMsg(this, "&a&lCongrats! &7You just reached the latest arena wave!");
    public ILangMsg Arena_Game_Wave_Start = new ILangMsg(this, "{message: ~type: TITLES; ~fadeIn: 10; ~stay: 40; ~fadeOut: 10;}&6&lWave &e&l#%arena_wave_number% \n &4&lPrepare to fight!");
	public ILangMsg Arena_Game_Wave_Timer = new ILangMsg(this, "{message: ~type: ACTION_BAR;}&6&lNew wave in: &e&l%arena_wave_next_in% &6&lseconds!");
	public ILangMsg Arena_Game_Wave_TimerEnd = new ILangMsg(this, "{message: ~type: ACTION_BAR;}&b&l&nGame Ends in:&d&l %arena_wave_next_in% &b&lseconds!");
	public ILangMsg Arena_Game_Wave_Progress = new ILangMsg(this, "{message: ~type: ACTION_BAR;}&b[Mobs] &aAlive: &2%arena_mobs% &7| &eTotal: &6%arena_mobs_total%");
	
    public ILangMsg Arena_Leave_Error_NotInGame = new ILangMsg(this, "You are not in game!");
    public ILangMsg Arena_Leave_Success = new ILangMsg(this, "{message: ~type: TITLES; ~fadeIn: 10; ~stay: 40; ~fadeOut: 10;}&4You have left the game.");
    
    public ILangMsg Arena_Region_Unlocked_Notify = new ILangMsg(this, "Arena region unlocked: &a%region_name%&7!");
    public ILangMsg Arena_Region_Locked_Notify = new ILangMsg(this, "{message: ~type: TITLES; ~fadeIn: 0; ~stay: 30; ~fadeOut: 10;}&c&lNew Region!\n&4&lFollow to the next arena region!");
	
    public ILangMsg Coins_Format = new ILangMsg(this, "Coins");
    public ILangMsg Coins_Balance = new ILangMsg(this, "Your balance: &a%coins% Coins&7!");
    public ILangMsg Coins_Get = new ILangMsg(this, "You recieved &a%coins% Coins&7!");
    public ILangMsg Coins_Lost = new ILangMsg(this, "You have lost &c%coins% Coins&7!");
    public ILangMsg Coins_Set = new ILangMsg(this, "Your coins balance has been set to &a%coins% Coins&7!");
	
	public ILangMsg Kit_Buy_Error_NoMoney        = new ILangMsg(this, "You cant afford for &c%kit_name% &7kit!");
	public ILangMsg Kit_Buy_Success              = new ILangMsg(this, "You successfully bought the kit &a%kit_name% &7for &a%kit_cost% $&7!");
	public ILangMsg Kit_Buy_Error_NoPermission   = new ILangMsg(this, "&cYou don't have permission to purchase this kit!");
	public ILangMsg Kit_Select_Error_NoPermission   = new ILangMsg(this, "&cYou don't have permission to use this kit!");
	public ILangMsg Kit_Select_Error_Disabled    = new ILangMsg(this, "This kit is disabled in this arena.");
	public ILangMsg Kit_Select_Error_NotObtained = new ILangMsg(this, "You don't have this kit!");
	public ILangMsg Kit_Select_Error_Limit       = new ILangMsg(this, "You can not use this kit, because there is already a maximum allowed number of players with this kit.");
	public ILangMsg Kit_Select_Error_NoPerm      = new ILangMsg(this, "You dont have permissions to use &c%kit_name%&7 kit!");
	public ILangMsg Kit_Select_Success           = new ILangMsg(this, "You choosen &a%kit_name%&7 as your kit.");
	
	public ILangMsg Shop_Notify_NewItems = new ILangMsg(this, "&aNew items appeared in the shop!");
	public ILangMsg Shop_Buy_Success = new ILangMsg(this, "You successfully bought &a%shop_product_name% &7for &a%shop_product_price%7!");
	public ILangMsg Shop_Buy_Error_NoMoney = new ILangMsg(this, "&cYou don't have enough money to buy &e%shop_product_name%&c!");
	public ILangMsg Shop_Buy_Error_Locked = new ILangMsg(this, "&cThis item is not available yet!");
	public ILangMsg Shop_Buy_Error_BadKit = new ILangMsg(this, "&cThis item is not available for your kit!");
	public ILangMsg Shop_Open_Error_InWave = new ILangMsg(this, "&cShop is closed until all mobs are killed!");

	public ILangMsg Arena_Game_Trigger_Format_Full                 = new ILangMsg(this, "&c%trigger_type%: &f%trigger_value%");
	public ILangMsg Arena_Game_Trigger_Format_Value_Delimiter      = new ILangMsg(this, "&7/");
	public ILangMsg Arena_Game_Trigger_Format_Value_Number_Each    = new ILangMsg(this, "&eEach x%value%");
	public ILangMsg Arena_Game_Trigger_Format_Value_Number_EachNot = new ILangMsg(this, "&6Each Not %value%");
	public ILangMsg Arena_Game_Trigger_Format_Value_Number_Not     = new ILangMsg(this, "&cNot %value%");
	public ILangMsg Arena_Game_Trigger_Format_Value_Number_Raw     = new ILangMsg(this, "&a%value%");

	public ILangMsg Arena_Region_Hologram_State_Locked = new ILangMsg(
			this, """
			&e&lRegion: &f%region_name% &7(&c%region_state%&7)
			&7
			&7Region will be &aUnlocked &7on
			&7one of the following conditions:
			%arena_game_triggers%
			"""
			);
	public ILangMsg Arena_Region_Hologram_State_Unlocked = new ILangMsg(this, """
			&e&lRegion: &f%region_name% &7(&a%region_state%&7)
			&7
			&7Region will be &cLocked &7on
			&7one of the following conditions:
			%arena_game_triggers%
			"""
	);
	
	public ILangMsg Holograms_Stats_Header_Orphan = new ILangMsg(this, "&aTop Stats for &2%top_type%");
	public ILangMsg Holograms_Stats_Header_Arena = new ILangMsg(this, "&aTop Stats for &2%top_type% &7in &2%arena_name%");
	public ILangMsg Holograms_Stats_Line = new ILangMsg(this, "&a%top_position%. &2%top_name% &7- &a%top_score% &7%top_type%");

	public ILangMsg Titles_Leave_Death = new ILangMsg(this, "{message: ~type: TITLES; ~fadeIn: 10; ~stay: 60; ~fadeOut: 10;}&c&lYou died! \n &7&lYou has left the arena.");
	public ILangMsg Titles_Leave_Finish = new ILangMsg(this, "{message: ~type: TITLES; ~fadeIn: 10; ~stay: 60; ~fadeOut: 10;}&a&lCongratulations! You finished the arena! \n &2&lCheck your inventory for rewards!");
	public ILangMsg Titles_Leave_Timeleft = new ILangMsg(this, "{message: ~type: TITLES; ~fadeIn: 10; ~stay: 60; ~fadeOut: 10;}&4&lTime is ended! \n &7&lYou has left the arena.");
	public ILangMsg Titles_Leave_NoRegion = new ILangMsg(this, "{message: ~type: TITLES; ~fadeIn: 10; ~stay: 60; ~fadeOut: 10;}&4&lNo Regions! \n &7&lYou has left the arena.");
	public ILangMsg Titles_Leave_ForceEnd = new ILangMsg(this, "{message: ~type: TITLES; ~fadeIn: 10; ~stay: 60; ~fadeOut: 10;}&4&lForce End! \n &7&lYou has left the arena.");
	public ILangMsg Titles_Leave_Outside = new ILangMsg(this, "{message: ~type: TITLES; ~fadeIn: 10; ~stay: 60; ~fadeOut: 10;}&4&lYou're out of the arena! \n &7&lYou has left the arena.");
	public ILangMsg Titles_Leave_Self = new ILangMsg(this, "{message: ~type: TITLES; ~fadeIn: 10; ~stay: 60; ~fadeOut: 10;}&4&lMob Arena \n &7&lYou has left the arena.");
	public ILangMsg Titles_Leave_NoKit = new ILangMsg(this, "{message: ~type: TITLES; ~fadeIn: 10; ~stay: 60; ~fadeOut: 10;}&4&lYou don't have a kit! \n &7&lYou has left the arena.");
	public ILangMsg Titles_Leave_Kick = new ILangMsg(this, "{message: ~type: TITLES; ~fadeIn: 10; ~stay: 60; ~fadeOut: 10;}&4&lKICKED! \n &7&lYou has left the arena.");

	public ILangMsg Editor_Tip_Triggers = new ILangMsg(this, """
			{message: ~prefix: false;}
			&7
			&b&lTrigger Tips:
			&2▸ &aMain Syntax: &2<Trigger_Type> <Value(s)>
			&2▸ &aExample: &2WAVE_START %5,!10,12
			&7
			&b&lValue Parameters:
			&6▸ &ePercent (%): &6Means each X number.
			&dExample: &bWAVE_START %5 &7- Each 5th wave = 5/10/15/20/etc.
			&7
			&6▸ &eGreater/Smaller (> or <): &6Means greater or smaller than X number.
			&dExample: &bWAVE_START >10,<20 &7- Any wave above 10th and below 20th.
			&7
			&6▸ &eNot (!): &6Any except X number.
			&dExample: &bWAVE_START %5,!10 &7- Each 5th wave, but 10th.
			&dExample #2: &bWAVE_START !%5 &7- Each NOT 5th wave = 1/2/3/4/6/7/8/9/11/etc.
			&7
			&bList of all available trigger types is below.
			&dFor more details please visit &ahttp://nexwiki.info/
			""");

	public ILangMsg Editor_Enter_Triggers = new ILangMsg(this, "&7Enter trigger type and value(s)...");
	public ILangMsg Editor_Error_Triggers            = new ILangMsg(this, "&7Invalid trigger &ctype &7or &cvalue!");

	public ILangMsg Editor_Arena_Tip_Create = new ILangMsg(this, "&7Enter &aunqiue &7arena &aidentifier&7...");
	public ILangMsg Editor_Arena_Error_Exist = new ILangMsg(this, "&cArena already exists!");
	public ILangMsg Editor_Arena_Enter_JoinCost = new ILangMsg(this, "&7Enter join &acost&7...");

	public ILangMsg Editor_Arena_Gameplay_Enter_Timeleft           = new ILangMsg(this, "&7Enter time &c(in minutes)");
	public ILangMsg Editor_Arena_Gameplay_Enter_LobbyTime          = new ILangMsg(this, "&7Enter time &c(in seconds)");
	public ILangMsg Editor_Arena_Gameplay_Enter_Players_Lives      = new ILangMsg(this, "&7Enter lives amount");
	public ILangMsg Editor_Arena_Gameplay_Enter_Players_MinMax     = new ILangMsg(this, "&7Enter players amount");
	public ILangMsg Editor_Arena_Gameplay_Enter_BannedItems        = new ILangMsg(this, "&7Enter &amaterial &7name...");
	public ILangMsg Editor_Arena_Gameplay_Enter_AllowedSpawnReason = new ILangMsg(this, "&7Enter &aspawn reason&7...");
	public ILangMsg Editor_Arena_Gameplay_Enter_Commands_AddWhite  = new ILangMsg(this, "&7Enter a command");
	public ILangMsg Editor_Arena_Gameplay_Enter_Kits_AddLimit      = new ILangMsg(this, "&7Enter a limit like: &a2 warrior");
	public ILangMsg Editor_Arena_Gameplay_Enter_Kits_AddAllowed    = new ILangMsg(this, "&7Enter a kit id");
	public ILangMsg Editor_Arena_Gameplay_Error_BannedItems        = new ILangMsg(this, "&7Invalid material!");
	public ILangMsg Editor_Arena_Gameplay_Error_Kits_InvalidKit    = new ILangMsg(this, "&7Invalid kit!");
	public ILangMsg Editor_Arena_Gameplay_Error_Kits_InvalidLimit  = new ILangMsg(this, "&7Use format like: &c2 warrior");
	
	public ILangMsg Editor_Arena_Waves_Enter_Delay_First   = new ILangMsg(this, "&7Enter &afirst&7 wave delay...");
	public ILangMsg Editor_Arena_Waves_Enter_Delay_Default = new ILangMsg(this, "&7Enter &adefault&7 wave delay...");
	public ILangMsg Editor_Arena_Waves_Enter_FinalWave     = new ILangMsg(this, "&7Enter &afinal wave&7...");

	public ILangMsg Editor_Arena_Waves_Enter_Gradual_First_Percent    = new ILangMsg(this, "&7Enter first spawn &apercent&7...");
	public ILangMsg Editor_Arena_Waves_Enter_Gradual_Next_Percent     = new ILangMsg(this, "&7Enter next spawn &apercent&7...");
	public ILangMsg Editor_Arena_Waves_Enter_Gradual_Next_Interval    = new ILangMsg(this, "&7Enter next spawn &ainterval&7...");
	public ILangMsg Editor_Arena_Waves_Enter_Gradual_Next_KillPercent = new ILangMsg(this, "&7Enter next spawn kill &apercent&7...");

	public ILangMsg Editor_Arena_Waves_Enter_Wave_Create         = new ILangMsg(this, "&7Enter &aunique &7wave &aidentifier&7...");
	public ILangMsg Editor_Arena_Waves_Enter_Mob_Create          = new ILangMsg(this, "&7Enter mob &aidentifier&7...");
	public ILangMsg Editor_Arena_Waves_Enter_Mob_Amount          = new ILangMsg(this, "&7Enter mob &astart amount&7...");
	public ILangMsg Editor_Arena_Waves_Enter_Mob_Level           = new ILangMsg(this, "&7Enter mob &astart level&7...");
	public ILangMsg Editor_Arena_Waves_Enter_Mob_Chance          = new ILangMsg(this, "&7Enter mob &aspawn chance&7...");
	public ILangMsg Editor_Arena_Waves_Enter_Amplificator_Create = new ILangMsg(this, "&7Enter &aunique &7amplificator &aidentifier&7...");
	public ILangMsg Editor_Arena_Waves_Enter_Amplificator_Value  = new ILangMsg(this, "&7Enter amplificator &avalue&7...");
	
	public ILangMsg Editor_Arena_Waves_Error_Wave_Exist         = new ILangMsg(this, "&7Wave already exist!");
	public ILangMsg Editor_Arena_Waves_Error_Mob_Exist          = new ILangMsg(this, "&7Mob already exist!");
	public ILangMsg Editor_Arena_Waves_Error_Mob_Invalid        = new ILangMsg(this, "&7No such Arena Mob or Mythic Mob!");
	public ILangMsg Editor_Arena_Waves_Error_Amplificator_Exist = new ILangMsg(this, "&7Amplificator already exist!");
	
	public ILangMsg Editor_Arena_Shop_Enter_Product_Create      = new ILangMsg(this, "&7Enter &aunique &7product &aidentifier&7...");
	public ILangMsg Editor_Arena_Shop_Enter_Product_Price       = new ILangMsg(this, "&7Enter product &aprice&7...");
	public ILangMsg Editor_Arena_Shop_Enter_Product_Command     = new ILangMsg(this, "&7Enter a command...");
	public ILangMsg Editor_Arena_Shop_Enter_Product_RequiredKit = new ILangMsg(this, "&7Enter &akit identifier&7...");
	public ILangMsg Editor_Arena_Shop_Error_Product_Exist       = new ILangMsg(this, "&cProduct already exist!");

	public ILangMsg Editor_Region_Enter_Id             = new ILangMsg(this, "&7Enter &aunique &7region identifier...");
	public ILangMsg Editor_Region_Enter_Name           = new ILangMsg(this, "&7Enter region name...");
	public ILangMsg Editor_Region_Error_Create         = new ILangMsg(this, "&7Region already exists!");
	public ILangMsg Editor_Region_Wave_Enter_Id        = new ILangMsg(this, "&7Enter &aarena wave &7identifier...");
	public ILangMsg Editor_Region_Wave_Enter_SpawnerId = new ILangMsg(this, "&7Enter &aspawner &7identifier...");
	public ILangMsg Editor_Region_Wave_Enter_Create    = new ILangMsg(this, "&7Enter &aunique &7wave identifier");
	public ILangMsg Editor_Region_Wave_Error_Create    = new ILangMsg(this, "&7Wave already exist!");
	public ILangMsg Editor_Region_Container_Enter_Amount = new ILangMsg(this, "&7Enter &aitems &7amount...");

	public ILangMsg Editor_Reward_Enter_Name = new ILangMsg(this, "&7Enter reward &aname&7...");
	public ILangMsg Editor_Reward_Enter_Command = new ILangMsg(this, "&7Enter a &acommand&7...");
	public ILangMsg Editor_Reward_Enter_Chance = new ILangMsg(this, "&7Enter reward &achance&7...");

	public ILangMsg Editor_Spot_Enter_Id = new ILangMsg(this, "&7Enter &aunique&7 spot identifier...");
	public ILangMsg Editor_Spot_Enter_Name = new ILangMsg(this, "&7Enter spot name...");
	public ILangMsg Editor_Spot_Error_Id = new ILangMsg(this, "&7Such spot already exist!");

	public ILangMsg Editor_Spot_State_Enter_Id = new ILangMsg(this, "&7Enter &aunique&7 state identifier...");
	public ILangMsg Editor_Spot_State_Error_Id = new ILangMsg(this, "&7Such state already exist!");
	public ILangMsg Editor_Spot_State_Error_NoCuboid = new ILangMsg(this, "&cYou must set a spot cuboid!");

	public ILangMsg Editor_Kit_Enter_Create  = new ILangMsg(this, "&7Enter &aunique &7kit &aidentifier&7...");
	public ILangMsg Editor_Kit_Enter_Name    = new ILangMsg(this, "&7Enter kit &aname&7...");
	public ILangMsg Editor_Kit_Enter_Command = new ILangMsg(this, "&7Enter a &acommand&7...");
	public ILangMsg Editor_Kit_Enter_Effect  = new ILangMsg(this, "&7Enter &aEffect:Level&7...");
	public ILangMsg Editor_Kit_Enter_Cost    = new ILangMsg(this, "&7Enter &acost&7...");
	public ILangMsg Editor_Kit_Error_Exist   = new ILangMsg(this, "&cKit already exists!");

	public ILangMsg Setup_Arena_Lobby_Set = new ILangMsg(this, "&7Defined lobby location for &a%arena_id% &7arena!");
	public ILangMsg Setup_Arena_Leave_Set = new ILangMsg(this, "&7Defined leave location for &a%arena_id% &7arena!");
	public ILangMsg Setup_Arena_Leave_UnSet = new ILangMsg(this, "&7Undefined leave location for &a%arena_id% &7arena!");
	public ILangMsg Setup_Arena_Spectate_Set = new ILangMsg(this, "&7Defined spectate location for &a%arena_id% &7arena!");

	public ILangMsg Setup_Region_Error_Enabled = new ILangMsg(this, "&cYou must disable region first!");
	public ILangMsg Setup_Region_Spawn_Set = new ILangMsg(this, "&7Defined spawn location for &a%region_id% &7region!");
	public ILangMsg Setup_Region_Spawner_Add = new ILangMsg(this, "&7Added spawner to &a%region_id% &7region!");
	public ILangMsg Setup_Region_Spawner_Remove = new ILangMsg(this, "&7Removed spawner from &a%region_id% &7region!");
	public ILangMsg Setup_Region_Container_Add = new ILangMsg(this, "&7Added container to &a%region_id% &7region!");
	public ILangMsg Setup_Region_Container_Remove = new ILangMsg(this, "&7Removed container from &a%region_id% &7region!");
	public ILangMsg Setup_Region_Hologram_Changed = new ILangMsg(this, "&7Changed hologram location for &a%region_id% &7region!");
	public ILangMsg Setup_Region_Hologram_Toggled = new ILangMsg(this, "&7Region state hologram enabled: %region_hologram_enabled%");
	public ILangMsg Setup_Region_Cuboid_Error_Overlap = new ILangMsg(this, "&7This position is overlaps with &c%region_id% &7arena region!");
	public ILangMsg Setup_Region_Cuboid_Set = new ILangMsg(this, "&7Set &a#%corner% &7corner for the &a%region_id% &7region!");
	public ILangMsg Setup_Reigon_Cuboid_Preview = new ILangMsg(
			this, """
			{message: ~prefix: false;}
			&c&m----------------------------------------
			&eYou defined new region position(s). The following changes will be made:
			&c▸ &7Mob Spawners Lost: &c%spawners-lost%
			&c▸ &7Region Containers Lost: &c%containers-lost%
			&c▸ &7Region Spawn Location Lost: &c%spawn-lost%
			&7
			&eIf you want to cancel these changes simply use &cExit Tool&7.
			&c&m----------------------------------------""");
	public ILangMsg Setup_Region_Error_Outside       = new ILangMsg(this, "&cLocation is outside of the editing region!");
	
	public ILangMsg Setup_Spot_Cuboid_Error_Overlap = new ILangMsg(this, "&cThis location is overlaps with other &e%spot_id% &cspot!");
	public ILangMsg Setup_Spot_Cuboid_Set = new ILangMsg(this, "&7Set &a#%corner% &7corner for the &a%spot_id% &7spot!");
	public ILangMsg Setup_Spot_State_Error_Outside = new ILangMsg(this, "&cLocation is outside of the editing spot!");
}
