package su.nightexpress.ama.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.commands.api.ISubCommand;
import su.nightexpress.ama.AMA;
import su.nightexpress.ama.Perms;
import su.nightexpress.ama.arena.ArenaPlayer;
import su.nightexpress.ama.api.arena.type.LeaveReason;

public class LeaveCmd extends ISubCommand<AMA> {

	public LeaveCmd(@NotNull AMA plugin) {
		super(plugin, new String[] {"leave"}, Perms.USER);
	}
	
	@Override
	@NotNull
	public String description() {
		return plugin.lang().Command_Leave_Desc.getMsg();
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
		
        ArenaPlayer arenaPlayer = plugin.getArenaManager().getArenaPlayer(player);
        if (arenaPlayer == null) {
        	plugin.lang().Arena_Leave_Error_NotInGame.send(player);
			return;
        }
        
        plugin.getArenaManager().leaveArena(arenaPlayer, LeaveReason.SELF);
	}
}
