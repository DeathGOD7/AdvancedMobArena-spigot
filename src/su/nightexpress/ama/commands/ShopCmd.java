package su.nightexpress.ama.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.commands.api.ISubCommand;
import su.nightexpress.ama.AMA;
import su.nightexpress.ama.Perms;
import su.nightexpress.ama.arena.ArenaPlayer;

public class ShopCmd extends ISubCommand<AMA> {

	public ShopCmd(@NotNull AMA plugin) {
		super(plugin, new String[] {"shop"}, Perms.USER);
	}

	@Override
	@NotNull
	public String usage() {
		return "";
	}

	@Override
	@NotNull
	public String description() {
		return plugin.lang().Command_Shop_Desc.getMsg();
	}

	@Override
	public boolean playersOnly() {
		return true;
	}

	@Override
	public void perform(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
		Player player = (Player) sender;
		
		ArenaPlayer arenaPlayer = plugin.getArenaManager().getArenaPlayer(player);
		if (arenaPlayer == null) return;
		
		arenaPlayer.getArena().getConfig().getShopManager().open(player);
	}

}
