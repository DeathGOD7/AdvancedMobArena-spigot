package su.nightexpress.ama.economy;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import su.nightexpress.ama.AMA;

public abstract class IEconomy {

	protected AMA plugin;
	
	public IEconomy(@NotNull AMA plugin) {
		this.plugin = plugin;
	}
	
	@NotNull
	public abstract String getName();
	
	public abstract double getBalance(@NotNull Player player);
	
	public abstract void add(@NotNull Player player, double amount);
	
	public abstract void take(@NotNull Player player, double amount);
}
