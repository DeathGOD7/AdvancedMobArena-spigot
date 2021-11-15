package su.nightexpress.ama.api;

import org.jetbrains.annotations.NotNull;

import su.nightexpress.ama.AMA;
import su.nightexpress.ama.arena.ArenaManager;
import su.nightexpress.ama.economy.IEconomy;
import su.nightexpress.ama.kits.KitManager;
import su.nightexpress.ama.stats.StatsManager;

public class ArenaAPI {

	private static AMA plugin = AMA.getInstance();
	
	@NotNull
	public static ArenaManager getArenaManager() {
		return plugin.getArenaManager();
	}
	
	@NotNull
	public static KitManager getKitManager() {
		return plugin.getKitManager();
	}
	
	@NotNull
	public static StatsManager getStatsManager() {
		return plugin.getStatsManager();
	}
	
	@NotNull
	public static IEconomy getEconomy() {
		return plugin.getEconomy();
	}
}
