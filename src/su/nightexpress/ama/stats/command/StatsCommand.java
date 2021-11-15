package su.nightexpress.ama.stats.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.commands.api.ISubCommand;
import su.nightexpress.ama.AMA;
import su.nightexpress.ama.Perms;
import su.nightexpress.ama.stats.StatsManager;

public class StatsCommand extends ISubCommand<AMA> {

	private final StatsManager statsManager;
	
	public StatsCommand(@NotNull StatsManager statsManager) {
		super(statsManager.plugin(), new String[] {"stats"}, Perms.USER);
		this.statsManager = statsManager;
	}
	
	@Override
	@NotNull
	public String usage() {
		return "";
	}

	@Override
	@NotNull
	public String description() {
		return plugin.lang().Command_Stats_Desc.getMsg();
	}

	@Override
	public boolean playersOnly() {
		return true;
	}

	@Override
	public void perform(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
		Player player = (Player) sender;
		this.statsManager.getMenuStats().open(player, 1);
	}
}
