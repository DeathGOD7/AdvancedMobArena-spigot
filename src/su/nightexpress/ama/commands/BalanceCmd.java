package su.nightexpress.ama.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.commands.api.ISubCommand;
import su.nexmedia.engine.utils.NumberUT;
import su.nightexpress.ama.AMA;
import su.nightexpress.ama.Perms;

public class BalanceCmd extends ISubCommand<AMA> {

	public BalanceCmd(@NotNull AMA plugin) {
		super(plugin, new String[] {"balance"}, Perms.USER);
	}
	
	@Override
	@NotNull
	public String usage() {
		return "";
	}

	@Override
	@NotNull
	public String description() {
		return plugin.lang().Coins_Balance.getMsg();
	}

	@Override
	public boolean playersOnly() {
		return true;
	}

	@Override
	public void perform(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
		Player player = (Player) sender;
		
		double amount = plugin.getEconomy().getBalance(player);
		plugin.lang().Coins_Balance.replace("%coins%", NumberUT.format(amount)).send(player);
	}
}
