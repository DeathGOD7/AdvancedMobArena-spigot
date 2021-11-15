package su.nightexpress.ama.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.commands.api.ISubCommand;
import su.nightexpress.ama.AMA;
import su.nightexpress.ama.Perms;
import su.nightexpress.ama.api.arena.IArena;
import su.nightexpress.ama.api.arena.type.ArenaState;

import java.util.List;

public class ForceStartCmd extends ISubCommand<AMA> {

	public ForceStartCmd(@NotNull AMA plugin) {
		super(plugin, new String[] {"forcestart"}, Perms.ADMIN);
	}
	
	@Override
	@NotNull
	public String description() {
		return plugin.lang().Command_ForceStart_Desc.getMsg();
	}

	@Override
	@NotNull
	public String usage() {
		return plugin.lang().Command_ForceStart_Usage.getMsg();
	}

	@Override
	public boolean playersOnly() {
		return false;
	}

	@Override
	@NotNull
	public List<String> getTab(@NotNull Player player, int i, @NotNull String[] args) {
		if (i == 1) {
	       	return plugin.getArenaManager().getArenaIds();
	    }
		return super.getTab(player, i, args);
	}
	
	@Override
	public void perform(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
		if (args.length != 2) {
			this.printUsage(sender);
			return;
		}
		
		String arenaId = args[1];
		IArena arena = plugin.getArenaManager().getArenaById(arenaId);
		if (arena == null) {
			plugin.lang().Arena_Error_Invalid.replace("%id%", arenaId).send(sender);
			return;
		}
		
		if (arena.getState() != ArenaState.READY) {
			plugin.lang().Command_ForceStart_Error_NotReady.replace("%id%", arenaId).send(sender);
			return;
		}
		
		plugin.lang().Command_ForceStart_Done
			.replace("%id%", arena.getConfig().getName())
			.send(sender);
		arena.setLobbyTimeleft(0);
	}
}
