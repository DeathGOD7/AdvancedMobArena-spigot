package su.nightexpress.ama.hooks.external;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.hooks.HookState;
import su.nexmedia.engine.hooks.NHook;
import su.nexmedia.engine.utils.CollectionsUT;
import su.nexmedia.engine.utils.NumberUT;
import su.nexmedia.engine.utils.TimeUT;
import su.nightexpress.ama.AMA;
import su.nightexpress.ama.api.ArenaAPI;
import su.nightexpress.ama.api.arena.IArena;
import su.nightexpress.ama.arena.ArenaPlayer;
import su.nightexpress.ama.data.ArenaUser;
import su.nightexpress.ama.stats.StatType;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class PlaceholderHook extends NHook<AMA> {
	
	public PlaceholderHook(@NotNull AMA plugin) {
		super(plugin);
	}

	@Override
	@NotNull
	public HookState setup() {
		new PPatch().patch();
		return HookState.SUCCESS;
	}
	
	@Override
	public void shutdown() {
		
	}
	
	public void register() {
		
	}
	
	class PPatch {
		
		public void patch() {
			new AMAExpansion().register();
		}
	}
	
	public class AMAExpansion extends PlaceholderExpansion {
		
	    @Override
	    @NotNull
		public String getAuthor() {
			return plugin.getAuthor();
		}

		@Override
		@NotNull
		public String getIdentifier() {
			return "ama";
		}

		@Override
		@NotNull
		public String getVersion() {
			return plugin.getDescription().getVersion();
		}

		@Override
		public boolean persist() {
			return true;
		}
		
		@Override
	    public String onPlaceholderRequest(Player player, String tmp) {
	        if (tmp.startsWith("stats_")) {
	        	String typeRaw = tmp.replace("stats_", "");
	        	
	            StatType type = CollectionsUT.getEnum(typeRaw, StatType.class);
	            if (type == null) return "NaN";
	            
	            ArenaUser user = plugin.getUserManager().getOrLoadUser(player);
	            return user == null ? "NaN" : String.valueOf(user.getStats(type));
	        }
	        if (tmp.equalsIgnoreCase("coins")) {
	        	return NumberUT.format(plugin.getEconomy().getBalance(player));
	        }
	        if (tmp.startsWith("arena_")) {
	        	ArenaPlayer arenaPlayer = ArenaAPI.getArenaManager().getArenaPlayer(player);
	        	if (arenaPlayer == null) return "-";
	        	
	        	IArena arena = arenaPlayer.getArena();
	        	String var = tmp.replace("arena_", "");
	        	
	        	if (var.equalsIgnoreCase("mobs")) { // TODO mobs/ total mobs
	        		return String.valueOf(arena.getMobs().size());
	        	}
	        	if (var.equalsIgnoreCase("name")) {
	        		return arena.getConfig().getName();
	        	}
	        	if (var.equalsIgnoreCase("score")) {
	        		return String.valueOf(arenaPlayer.getScore());
	        	}
	        	if (var.equalsIgnoreCase("streak_length")) {
	        		return String.valueOf(arenaPlayer.getKillStreak());
	        	}
	        	if (var.equalsIgnoreCase("streak_decay")) {
	        		DateTimeFormatter FORMAT_STREAK = DateTimeFormatter.ofPattern("ss");
	        		LocalTime timeStreak = TimeUT.getLocalTimeOf(arenaPlayer.getKillStreakDecay());
	        		return timeStreak.format(FORMAT_STREAK);
	        	}
	        	if (var.equalsIgnoreCase("kills")) {
	        		return String.valueOf(arenaPlayer.getStats(StatType.MOB_KILLS));
	        	}
	        	if (var.equalsIgnoreCase("timeleft")) {
	        		DateTimeFormatter FORMAT_TIMELEFT = DateTimeFormatter.ofPattern("HH:mm:ss");
	        		return TimeUT.getLocalTimeOf(arena.getGameTimeleft()).format(FORMAT_TIMELEFT);
	        	}
	        	if (var.equalsIgnoreCase("wave")) {
	        		return String.valueOf(arena.getWaveNumber());
	        	}
	        	if (var.equalsIgnoreCase("players")) {
	        		return String.valueOf(arena.getPlayersIngame().size());
	        	}
	        	if (var.equalsIgnoreCase("balance")) {
	        		return String.valueOf(plugin.getEconomy().getBalance(player));
	        	}
	        	if (var.equalsIgnoreCase("next_wave")) {
	        		return String.valueOf(arena.getWaveNextTimeleft());
	        	}
	        }
	        
	        return null;
	    }
	}
}
