package su.nightexpress.ama.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.commands.api.ISubCommand;
import su.nexmedia.engine.utils.StringUT;
import su.nightexpress.ama.AMA;
import su.nightexpress.ama.Perms;
import su.nightexpress.ama.api.arena.IArena;
import su.nightexpress.ama.api.arena.type.ArenaState;
import su.nightexpress.ama.arena.ArenaPlayer;

public class SkipwaveCmd extends ISubCommand<AMA> {

	public SkipwaveCmd(@NotNull AMA plugin) {
		super(plugin, new String[] {"skipwave"}, Perms.ADMIN);
	}
	
	@Override
	@NotNull
	public String description() {
		return plugin.lang().Command_Skipwave_Desc.getMsg();
	}

	@Override
	@NotNull
	public String usage() {
		return plugin.lang().Command_Skipwave_Usage.getMsg();
	}

	@Override
	public boolean playersOnly() {
		return true;
	}

	@Override
	public void perform(@NotNull CommandSender sender, @NotNull String labe, @NotNull String[] args) {
		Player player = (Player) sender;
		
        ArenaPlayer arenaPlayer = plugin.getArenaManager().getArenaPlayer(player);
        if (arenaPlayer == null) {
        	plugin.lang().Arena_Leave_Error_NotInGame.send(player);
        	return;
        }
        
        int amount = args.length >= 2 ? StringUT.getInteger(args[1], 1) : 1;
        IArena arena = arenaPlayer.getArena();
        
        new BukkitRunnable() {
        	int count = 0;
        	
			@Override
			public void run() {
				if (this.count++ >= amount || arena.getState() != ArenaState.INGAME) {
					this.cancel();
					return;
				}
				arena.skipWave();
			}
		}.runTaskTimer(plugin, 0L, 45L);
	}
}
