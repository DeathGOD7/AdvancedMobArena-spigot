package su.nightexpress.ama.config;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.config.api.IConfigTemplate;
import su.nexmedia.engine.config.api.ILangMsg;
import su.nexmedia.engine.utils.ItemUT;
import su.nexmedia.engine.utils.StringUT;
import su.nightexpress.ama.AMA;
import su.nightexpress.ama.api.arena.type.LobbyItemType;
import su.nightexpress.ama.arena.ArenaKillStreak;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.DoubleUnaryOperator;

public class Config extends IConfigTemplate {
	
	public Config(@NotNull AMA plugin) {
		super(plugin);
	}
	
	public static boolean GEN_DISABLE_INVENTORY_MANAGER;
	
	public static boolean DEBUG_MOB_SPAWN;
	
	public static boolean CHAT_ENABLED;
	public static boolean CHAT_IGNORE_GLOBAL;
	public static String CHAT_FORMAT;
	
	public static boolean GEN_VAULT_CURRENCY;

	public static Map<String, Double> MOBS_COINS_TABLE;
	public static Map<String, Integer> MOBS_SCORE_TABLE;
	public static long MOBS_KILL_STREAK_DECAY;
	public static Map<Integer, ArenaKillStreak> MOBS_KILL_STREAK_TABLE;

	public static int LOBBY_READY_DROP_TIMER;
	public static boolean LOBBY_READY_FREEZE_TIMER_WHEN_DROPPED;

	public static String SOUND_LOBBY_TICK;
	public static String SOUND_GAME_START;
	public static String SOUND_GAME_END;

	public static List<String> SIGNS_JOIN_FORMAT;
	public static List<String> SIGNS_READY_FORMAT;

	@Override
    public void load() {
    	Config.GEN_DISABLE_INVENTORY_MANAGER = cfg.getBoolean("general.disable-inventory-manager");
    	Config.GEN_VAULT_CURRENCY = cfg.getBoolean("general.vault-currency");
    	
    	Config.DEBUG_MOB_SPAWN = cfg.getBoolean("Debug.Mob_Spawning");
    	
    	CHAT_ENABLED = cfg.getBoolean("Chat.Enabled");
    	CHAT_IGNORE_GLOBAL = cfg.getBoolean("Chat.Ignore_Global_Chat");
    	CHAT_FORMAT = StringUT.color(cfg.getString("Chat.Format", "&7(&6%kit%&7) &a%player%: &f%msg%"));

    	String path = "Lobby.Ready_State.";
    	cfg.addMissing(path + "Drop_Timer_To", 15);
    	cfg.addMissing(path + "Freeze_Dropped_Timer_When_Not_Ready", true);
    	LOBBY_READY_DROP_TIMER = cfg.getInt(path + "Drop_Timer_To", 15);
    	LOBBY_READY_FREEZE_TIMER_WHEN_DROPPED = cfg.getBoolean(path + "Freeze_Dropped_Timer_When_Not_Ready", true);

    	path = "Lobby.Items.";
    	if (!cfg.contains(path + LobbyItemType.READY.name())) {
			cfg.set(path + LobbyItemType.READY.name() + ".Enabled", true);
			cfg.set(path + LobbyItemType.READY.name() + ".Slot", 7);
			cfg.set(path + LobbyItemType.READY.name() + ".Item.material", Material.LIME_DYE.name());
			cfg.set(path + LobbyItemType.READY.name() + ".Item.name", "&a&nReady State");
			cfg.set(path + LobbyItemType.READY.name() + ".Item.lore", Collections.singletonList("&7(Right click to change your state)"));
		}

    	for (LobbyItemType itemType : LobbyItemType.values()) {
    		String path2 = path + itemType.name() + ".";

			itemType.setEnabled(cfg.getBoolean(path2 + "Enabled"));
			if (!itemType.isEnabled()) continue;
    		
    		ItemStack item = cfg.getItem(path2 + "Item");
    		if (ItemUT.isAir(item)) continue;
    		
    		int slot = cfg.getInt(path2 + "Slot");

    		itemType.setItem(item);
    		itemType.setSlot(slot);
    	}

    	path = "mobs.";
    	Config.MOBS_COINS_TABLE = new HashMap<>();
    	Config.MOBS_SCORE_TABLE = new HashMap<>();
    	for (String mobId : cfg.getSection(path + "money-table")) {
    		double money = cfg.getDouble(path + "money-table." + mobId);
    		if (money > 0) {
    			Config.MOBS_COINS_TABLE.put(mobId.toLowerCase(), money);
    		}
    	}
    	for (String mobId : cfg.getSection(path + "score-table")) {
    		int score = cfg.getInt(path + "score-table." + mobId);
    		if (score > 0) {
    			Config.MOBS_SCORE_TABLE.put(mobId.toLowerCase(), score);
    		}
    	}
    	
    	Config.SOUND_LOBBY_TICK = cfg.getString("Sounds.Lobby.Time_Tick", "");
    	Config.SOUND_GAME_START = cfg.getString("Sounds.Game.Start", "");
    	Config.SOUND_GAME_END = cfg.getString("Sounds.Game.End", "");

    	if (!cfg.contains("Signs.Ready.Format")) {
    		cfg.set("Signs.Ready.Format", Arrays.asList("&8[&4Mob Arena&8]", "&a&lReady State", "&7", "&bClick to Ready!"));
		}

    	SIGNS_JOIN_FORMAT = StringUT.color(cfg.getStringList("Signs.Join.Format"));
    	SIGNS_READY_FORMAT = StringUT.color(cfg.getStringList("Signs.Ready.Format"));

    	cfg.saveChanges();
    }
	
	@Nullable
	public static ArenaKillStreak getKillStreak(int streak) {
		Map<Integer, ArenaKillStreak> streaks = Config.MOBS_KILL_STREAK_TABLE;
		return streaks != null ? streaks.getOrDefault(streak, null) : null;
	}
	
	public void setupStreaks() {
    	String path = "mobs.kill-streak.";
    	if (cfg.getBoolean(path + "enabled")) {
	    	Config.MOBS_KILL_STREAK_DECAY = (long)(int)(cfg.getDouble(path + "streak-decay", 5D) * 1000D);
	    	Config.MOBS_KILL_STREAK_TABLE = new TreeMap<>();
	    	for (String sId : cfg.getSection(path + "streaks")) {
	    		int streak = StringUT.getInteger(sId, -1);
	    		if (streak <= 0) continue;
	    		
	    		String path2 = path + "streaks." + sId + ".";
	    		ILangMsg streakMessage = new ILangMsg(plugin.lang(), cfg.getString(path2 + "message", "&c&lx%streak% Kill!"));
	    		String streakMoney = cfg.getString(path2 + "extra-money", "0");
	    		String streakScore = cfg.getString(path2 + "extra-score", "0");
	    		List<String> commands = cfg.getStringList(path2 + "commands");
	    		
	    		boolean moneyMod = streakMoney.endsWith("%");
	    		boolean scoreMod = streakScore.endsWith("%");
	    		
	    		double sMoney = StringUT.getDouble(streakMoney.replace("%", ""), 0);
	    		double sScore = StringUT.getDouble(streakScore.replace("%", ""), 0);
	    		
	    		DoubleUnaryOperator fMoney = (money) -> moneyMod ? (money * (1D + sMoney / 100D)) : (money + sMoney);
	    		DoubleUnaryOperator fScore = (score) -> scoreMod ? (score * (1D + sScore / 100D)) : (score + sScore);
	    		
	    		ArenaKillStreak killStreak = new ArenaKillStreak(streak, streakMessage, fMoney, fScore, commands);
	    		Config.MOBS_KILL_STREAK_TABLE.put(streak, killStreak);
	    	}
    	}
	}
}
