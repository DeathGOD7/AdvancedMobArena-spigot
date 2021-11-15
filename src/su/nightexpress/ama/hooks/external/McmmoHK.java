package su.nightexpress.ama.hooks.external;

import com.gmail.nossr50.events.experience.McMMOPlayerXpGainEvent;
import com.gmail.nossr50.events.skills.abilities.McMMOPlayerAbilityActivateEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.hooks.HookState;
import su.nexmedia.engine.hooks.NHook;
import su.nightexpress.ama.AMA;
import su.nightexpress.ama.api.arena.IArena;
import su.nightexpress.ama.arena.ArenaPlayer;

public class McmmoHK extends NHook<AMA> {

	public McmmoHK(@NotNull AMA plugin) {
		super(plugin);
	}
	
	@Override
	@NotNull
	public HookState setup() {
		this.registerListeners();
		return HookState.SUCCESS;
	}
	
	@Override
	public void shutdown() {
		this.unregisterListeners();
	}

	@EventHandler
	public void onUseSkill(McMMOPlayerAbilityActivateEvent e) {
		Player p = e.getPlayer();
		ArenaPlayer arenaPlayer = plugin.getArenaManager().getArenaPlayer(p);
		if (arenaPlayer == null) return;
		
		IArena arena = arenaPlayer.getArena();
		if (!arena.getConfig().getGameplayManager().isExternalMcmmoEnabled()) {
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onExpGain(McMMOPlayerXpGainEvent e) {
		Player p = e.getPlayer();
		ArenaPlayer arenaPlayer = plugin.getArenaManager().getArenaPlayer(p);
		if (arenaPlayer == null) return;
		
		IArena arena = arenaPlayer.getArena();
		if (!arena.getConfig().getGameplayManager().isExternalMcmmoEnabled()) {
			e.setCancelled(true);
		}
	}
}
