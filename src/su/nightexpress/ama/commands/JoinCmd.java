package su.nightexpress.ama.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.commands.api.ISubCommand;
import su.nightexpress.ama.AMA;
import su.nightexpress.ama.Perms;
import su.nightexpress.ama.api.arena.IArena;

import java.util.List;

public class JoinCmd extends ISubCommand<AMA> {

	public JoinCmd(@NotNull AMA plugin) {
		super(plugin, new String[] {"join"}, Perms.USER);
	}
	
	@Override
	@NotNull
	public String description() {
		return plugin.lang().Command_Join_Desc.getMsg();
	}

	@Override
	@NotNull
	public String usage() {
		return plugin.lang().Command_Join_Usage.getMsg();
	}

	@Override
	public boolean playersOnly() {
		return true;
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
		if (args.length > 2) {
			this.printUsage(sender);
			return;
		}
		
		Player player = (Player) sender;
		IArena arena;
		
		if (args.length == 2) {
			arena = plugin.getArenaManager().getArenaById(args[1]);
			if (arena == null) {
	            plugin.lang().Arena_Error_Invalid.send(sender);
	            return;
	        }
		}
		else {
			arena = plugin.getArenaManager().getArenas().stream()
					.filter(arena1 -> arena1.canJoin(player, false)).findFirst().orElse(null);
		}
		
        if (arena != null) {
        	plugin.getArenaManager().joinLobby(player, arena);
        }
        else {
        	plugin.lang().Command_Join_Nothing.send(player);
        }
	}
}
