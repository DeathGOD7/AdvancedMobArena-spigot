package su.nightexpress.ama.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.commands.api.ISubCommand;
import su.nightexpress.ama.AMA;
import su.nightexpress.ama.Perms;

public class ListCmd extends ISubCommand<AMA> {
	
	public ListCmd(@NotNull AMA plugin) {
		super(plugin, new String[] {"list"}, Perms.USER);
	}
	
	@Override
	@NotNull
	public String description() {
		return plugin.lang().Command_List_Desc.getMsg();
	}

	@Override
	@NotNull
	public String usage() {
		return "";
	}

	@Override
	public boolean playersOnly() {
		return true;
	}

	@Override
	public void perform(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
		Player player = (Player) sender;
		plugin.getArenaManager().getArenaListMenu().open(player, 1);
	}
}
