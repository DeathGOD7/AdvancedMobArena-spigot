package su.nightexpress.ama.economy.object;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import su.nightexpress.ama.AMA;
import su.nightexpress.ama.data.ArenaUser;
import su.nightexpress.ama.economy.IEconomy;

public class InternalEco extends IEconomy {

	public InternalEco(@NotNull AMA plugin) {
		super(plugin);
	}
	
	@Override
	@NotNull
	public String getName() {
		return "Arena Coins";
	}

	@Override
	public double getBalance(@NotNull Player player) {
		ArenaUser user = plugin.getUserManager().getOrLoadUser(player);
		if (user == null) return 0D;
		
		return user.getCoins();
	}
	
	@Override
	public void add(@NotNull Player player, double amount) {
		ArenaUser user = plugin.getUserManager().getOrLoadUser(player);
		if (user == null) return;
		
		user.setCoins((int) (user.getCoins() + amount));
	}
	
	@Override
	public void take(@NotNull Player player, double amount) {
		ArenaUser user = plugin.getUserManager().getOrLoadUser(player);
		if (user == null) return;
		
		user.setCoins((int) (user.getCoins() - amount));
	}
}
