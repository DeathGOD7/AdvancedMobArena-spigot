package su.nightexpress.ama.commands;

import java.util.Arrays;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.commands.api.ISubCommand;
import su.nexmedia.engine.utils.PlayerUT;
import su.nexmedia.engine.utils.StringUT;
import su.nightexpress.ama.AMA;
import su.nightexpress.ama.Perms;

public class CoinsCmd extends ISubCommand<AMA> {

	public CoinsCmd(@NotNull AMA plugin) {
		super(plugin, new String[] {"coins"}, Perms.ADMIN);
	}
	
	@Override
	@NotNull
	public String usage() {
		return plugin.lang().Command_Coins_Usage.getMsg();
	}

	@Override
	@NotNull
	public String description() {
		return plugin.lang().Command_Coins_Desc.getMsg();
	}

	@Override
	public boolean playersOnly() {
		return false;
	}

	@Override
	@NotNull
	public List<String> getTab(@NotNull Player player, int i, @NotNull String[] args) {
		if (i == 1) {
	       	return Arrays.asList("add", "take", "set");
	    }
		if (i == 2) {
	       	return PlayerUT.getPlayerNames();
	    }
		if (i == 3) {
	       	return Arrays.asList("<amount>");
	    }
		return super.getTab(player, i, args);
	}
	
	@Override
	public void perform(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
		if (args.length != 4) {
			plugin.lang().Help_Coins.send(sender);
			//this.printUsage(sender);
			return;
		}
		
		if (args[1].equalsIgnoreCase("add")) {
			Player player = plugin.getServer().getPlayer(args[2]);
			if (player == null) {
				this.errPlayer(sender);
				return;
			}
			
			int amount = StringUT.getInteger(args[3], -1);
			if (amount < 0) return;
			
			plugin.getEconomy().add(player, amount);

			plugin.lang().Coins_Get.replace("%coins%", amount).send(player);
			
			plugin.lang().Command_Coins_Add_Done
				.replace("%player%", player.getName())
				.replace("%coins%", amount)
				.send(sender);
		}
		else if (args[1].equalsIgnoreCase("take")) {
			Player player = plugin.getServer().getPlayer(args[2]);
			if (player == null) {
				this.errPlayer(sender);
				return;
			}
			
			int amount = StringUT.getInteger(args[3], -1);
			if (amount < 0) return;
			
			plugin.getEconomy().take(player, amount);
			
			plugin.lang().Coins_Lost.replace("%coins%", String.valueOf(amount)).send(player);
			
			plugin.lang().Command_Coins_Take_Done
				.replace("%player%", player.getName())
				.replace("%coins%", amount)
				.send(sender);
		}
		else if (args[1].equalsIgnoreCase("set")) {
			Player player = plugin.getServer().getPlayer(args[2]);
			if (player == null) {
				this.errPlayer(sender);
				return;
			}
			
			int amount = StringUT.getInteger(args[3], -1);
			if (amount < 0) return;
			
			plugin.getEconomy().take(player, plugin.getEconomy().getBalance(player));
			plugin.getEconomy().add(player, amount);
			
			plugin.lang().Coins_Set.replace("%coins%", String.valueOf(amount)).send(player);
			
			plugin.lang().Command_Coins_Set_Done
				.replace("%player%", player.getName())
				.replace("%coins%", amount)
				.send(sender);
		}
	}
}
