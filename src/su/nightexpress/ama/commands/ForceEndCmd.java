package su.nightexpress.ama.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.commands.api.ISubCommand;
import su.nexmedia.engine.utils.CollectionsUT;
import su.nightexpress.ama.AMA;
import su.nightexpress.ama.Perms;
import su.nightexpress.ama.api.arena.IArena;
import su.nightexpress.ama.api.arena.type.ArenaState;
import su.nightexpress.ama.api.arena.type.EndType;

import java.util.Arrays;
import java.util.List;

public class ForceEndCmd extends ISubCommand<AMA> {
	
	public ForceEndCmd(@NotNull AMA plugin) {
		super(plugin, new String[] {"forceend"}, Perms.ADMIN);
	}
	
	@Override
	@NotNull
	public String description() {
		return plugin.lang().Command_ForceEnd_Desc.getMsg();
	}

	@Override
	@NotNull
	public String usage() {
		return plugin.lang().Command_ForceEnd_Usage.getMsg();
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
		if (i == 2) {
			return Arrays.stream(EndType.values()).map(EndType::name).toList();
		}
		return super.getTab(player, i, args);
	}
	
	@Override
	public void perform(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
		if (args.length < 2) {
			this.printUsage(sender);
			return;
		}
		
		String arenaId = args[1];
		IArena arena = plugin.getArenaManager().getArenaById(arenaId);
		if (arena == null) {
			plugin.lang().Arena_Error_Invalid.send(sender);
			return;
		}
		
		if (arena.getState() != ArenaState.INGAME) {
			plugin.lang().Command_ForceEnd_Error_NotInGame.replace(arena.replacePlaceholders()).send(sender);
			return;
		}
		
		String typeRaw = args.length >= 3 ? args[2] : null;
		EndType endType = typeRaw != null ? CollectionsUT.getEnum(typeRaw, EndType.class) : EndType.FORCE;
		arena.stop(endType != null ? endType : EndType.FORCE);
		
		plugin.lang().Command_ForceEnd_Done.replace(arena.replacePlaceholders()).send(sender);
	}
}
