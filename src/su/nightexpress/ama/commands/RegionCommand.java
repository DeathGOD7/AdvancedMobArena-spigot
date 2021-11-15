package su.nightexpress.ama.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.commands.api.ISubCommand;
import su.nightexpress.ama.AMA;
import su.nightexpress.ama.Perms;
import su.nightexpress.ama.api.arena.IArena;
import su.nightexpress.ama.api.arena.region.IArenaRegion;
import su.nightexpress.ama.api.arena.type.ArenaLockState;
import su.nightexpress.ama.arena.ArenaPlayer;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class RegionCommand extends ISubCommand<AMA> {

	public RegionCommand(@NotNull AMA plugin) {
		super(plugin, new String[] {"region"}, Perms.ADMIN);
	}

	@Override
	@NotNull
	public String usage() {
		return plugin.lang().Command_Region_Usage.getMsg();
	}

	@Override
	@NotNull
	public String description() {
		return plugin.lang().Command_Region_Desc.getMsg();
	}

	@Override
	public boolean playersOnly() {
		return true;
	}

	@Override
	@NotNull
	public List<String> getTab(@NotNull Player player, int i, @NotNull String[] args) {
		if (i == 1) {
			return Arrays.asList("lock", "unlock");
		}
		if (i == 2) {
			ArenaPlayer arenaPlayer = plugin.getArenaManager().getArenaPlayer(player);
			if (arenaPlayer == null) return super.getTab(player, i, args);
			
			List<String> list = arenaPlayer.getArena().getConfig().getRegionManager().getRegions()
					.stream().map(IArenaRegion::getId).collect(Collectors.toList());
			return list;
		}
		return super.getTab(player, i, args);
	}

	@Override
	protected void perform(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
		if (args.length < 3) {
			this.printUsage(sender);
			return;
		}
		
		Player player = (Player) sender;
		ArenaPlayer arenaPlayer = plugin.getArenaManager().getArenaPlayer(player);
		if (arenaPlayer == null) {
			plugin.lang().Command_Region_State_Error_NotInGame.send(sender);
			return;
		}
		
		IArena arena = arenaPlayer.getArena();
		boolean lock = args[1].equalsIgnoreCase("lock");
		boolean unlock = args[1].equalsIgnoreCase("unlock");
		if (!lock && !unlock) {
			this.printUsage(sender);
			return;
		}
		
		String regId = args[2];
		IArenaRegion region = arena.getConfig().getRegionManager().getRegion(regId);
		if (region == null) {
			plugin.lang().Command_Region_State_Error_InvalidRegion.send(sender);
			return;
		}
		
		if (lock) region.setState(ArenaLockState.LOCKED);
		else if (unlock) region.setState(ArenaLockState.UNLOCKED);
		
		plugin.lang().Command_Region_State_Done.replace(region.replacePlaceholders()).send(sender);
	}
}
