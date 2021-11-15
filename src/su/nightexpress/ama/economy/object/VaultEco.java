package su.nightexpress.ama.economy.object;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import net.milkbowl.vault.economy.Economy;
import su.nexmedia.engine.hooks.external.VaultHK;
import su.nightexpress.ama.AMA;
import su.nightexpress.ama.economy.IEconomy;

public class VaultEco extends IEconomy {

	private Economy eco;
	
	public VaultEco(@NotNull AMA plugin, @NotNull VaultHK vault) {
		super(plugin);
        this.eco = vault.getEconomy();
	}
	
	@Override
	public double getBalance(@NotNull Player player) {
		return this.eco.getBalance(player);
	}
	
	@Override
	public void add(@NotNull Player player, double amount) {
		this.eco.depositPlayer(player, amount);
	}
	
	@Override
	public void take(@NotNull Player player, double amount) {
		this.eco.withdrawPlayer(player, amount);
	}

	@Override
	@NotNull
	public String getName() {
		return eco.getName();
	}
}
