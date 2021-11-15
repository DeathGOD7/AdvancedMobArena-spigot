package su.nightexpress.ama.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.commands.api.ISubCommand;
import su.nightexpress.ama.AMA;
import su.nightexpress.ama.Perms;
import su.nightexpress.ama.api.arena.spot.IArenaSpot;
import su.nightexpress.ama.api.arena.spot.IArenaSpotState;
import su.nightexpress.ama.arena.ArenaPlayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SpotCmd extends ISubCommand<AMA> {

	public SpotCmd(@NotNull AMA plugin) {
		super(plugin, new String[] {"spot"}, Perms.ADMIN);
	}

	@Override
	public boolean playersOnly() {
		return true;
	}

	@Override
	@NotNull
	public String usage() {
		return plugin.lang().Command_Spot_Usage.getMsg();
	}

	@Override
	@NotNull
	public String description() {
		return plugin.lang().Command_Spot_Desc.getMsg();
	}

	@Override
	@NotNull
	public List<String> getTab(@NotNull Player player, int i, @NotNull String[] args) {
		if (i == 1) {
			return Arrays.asList("state");
		}
		if (i == 2) {
			ArenaPlayer arenaPlayer = plugin.getArenaManager().getArenaPlayer(player);
			if (arenaPlayer == null) return super.getTab(player, i, args);
			
			List<String> list = arenaPlayer.getArena().getConfig().getSpotManager().getSpotsMap()
					.values().stream().map(spot -> spot.getId()).collect(Collectors.toList());
			return list;
		}
		if (i == 3) {
			ArenaPlayer arenaPlayer = plugin.getArenaManager().getArenaPlayer(player);
			if (arenaPlayer == null) return super.getTab(player, i, args);
			
			String spotId = args[2];
			IArenaSpot spot = arenaPlayer.getArena().getConfig().getSpotManager().getSpot(spotId);
			if (spot == null) return super.getTab(player, i, args);
			
			return new ArrayList<>(spot.getStates().keySet());
		}
		return super.getTab(player, i, args);
	}

	@Override
	protected void perform(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
		if (args.length < 2) {
			this.printUsage(sender);
			return;
		}
		
		if (args[1].equalsIgnoreCase("state")) {
			if (args.length != 4) {
				this.printUsage(sender);
				return;
			}
			
			Player player = (Player) sender;
			ArenaPlayer arenaPlayer = plugin.getArenaManager().getArenaPlayer(player);
			if (arenaPlayer == null) {
				plugin.lang().Command_Spot_State_Error_NotInGame.send(sender);
				return;
			}
			
			String spotId = args[2];
			IArenaSpot spot = arenaPlayer.getArena().getConfig().getSpotManager().getSpot(spotId);
			if (spot == null) {
				plugin.lang().Command_Spot_State_Error_InvalidSpot.send(sender);
				return;
			}
			
			String stateId = args[3];
			IArenaSpotState state = spot.getState(stateId);
			if (state == null) {
				plugin.lang().Command_Spot_State_Error_InvalidState.send(sender);
				return;
			}
			
			spot.setState(arenaPlayer.getArena(), stateId);
			
			plugin.lang().Command_Spot_State_Done
				.replace("%spot%", spot.getName())
				.replace("%state%", state.getId())
				.send(sender);
			
			return;
		}
	}
}
