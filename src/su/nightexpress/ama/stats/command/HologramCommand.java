package su.nightexpress.ama.stats.command;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.commands.api.ISubCommand;
import su.nexmedia.engine.utils.CollectionsUT;
import su.nightexpress.ama.AMA;
import su.nightexpress.ama.Perms;
import su.nightexpress.ama.stats.HologramHandler;
import su.nightexpress.ama.stats.StatType;
import su.nightexpress.ama.stats.StatsManager;

import java.util.Arrays;
import java.util.List;

public class HologramCommand extends ISubCommand<AMA> {

	private final HologramHandler hologramHandler;
	
	public HologramCommand(@NotNull StatsManager statsManager, @NotNull HologramHandler hologramHandler) {
		super(statsManager.plugin(), new String[] {"hologram"}, Perms.ADMIN);
		this.hologramHandler = hologramHandler;
	}
	
	@Override
	@NotNull
	public String description() {
		return plugin.lang().Command_Holo_Desc.getMsg();
	}

	@Override
	@NotNull
	public String usage() {
		return plugin.lang().Command_Holo_Usage.getMsg();
	}

	@Override
	public boolean playersOnly() {
		return true;
	}

	@Override
	@NotNull
	public List<String> getTab(@NotNull Player player, int i, @NotNull String[] args) {
		if (i == 1) {
	       	return Arrays.asList("add", "remove");
	    }
		if (i == 2) {
	       	return CollectionsUT.getEnumsList(StatType.class);
	    }
		if (i == 3) {
	       	return plugin.getArenaManager().getArenaIds();
	    }
		return super.getTab(player, i, args);
	}
	
	@Override
	public void perform(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
		Player player = (Player) sender;
		if (args.length < 2) {
			plugin.lang().Help_Hologram.send(sender);
			//this.printUsage(sender);
			return;
		}
		
        if (args[1].equalsIgnoreCase("remove")) {
            if (hologramHandler.removeNear(player.getLocation())) {
              	plugin.lang().Command_Holo_Remove_Done.send(player); 
            }
            else {
            	plugin.lang().Command_Holo_Remove_Nothing.send(player); 
            }
        }
        else if (args[1].equalsIgnoreCase("add")) {
        	if (args.length < 3) {
        		plugin.lang().Command_Holo_Add_Usage.send(player);
        		return;
        	}
        	
        	StatType type = CollectionsUT.getEnum(args[2], StatType.class);
        	if (type == null) {
        		this.errType(player, StatType.class);
        		return;
        	}
        	
            String arena = null;
            if (args.length == 4) {
            	arena = args[3];
            	if (!plugin.getArenaManager().isArenaExists(arena)) {
            		plugin.lang().Arena_Error_Invalid.send(player);
            		return;
            	}
            }
             
            Location l = player.getLocation();
           	if (hologramHandler.add(l, type, arena)) {
	            plugin.lang().Command_Holo_Add_Done
	            	.replace(StatsManager.PLACEHOLDER_TOP_TYPE, plugin.lang().getEnum(type))
	            	.send(player);
           	}
		}
	}
}
