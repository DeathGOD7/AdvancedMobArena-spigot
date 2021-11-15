package su.nightexpress.ama.api.arena.event;

import org.bukkit.event.Cancellable;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.ama.api.arena.IArena;
import su.nightexpress.ama.api.arena.game.ArenaGameEventType;
import su.nightexpress.ama.arena.ArenaPlayer;

public class ArenaPlayerJoinEvent extends ArenaPlayerGameEvent implements Cancellable {
	
	private boolean isCancelled;
	
	public ArenaPlayerJoinEvent(@NotNull IArena arena, @NotNull ArenaPlayer arenaPlayer) {
		super(arena, arenaPlayer, ArenaGameEventType.PLAYER_JOIN);
	}

	@Override
	public boolean isCancelled() {
		return this.isCancelled;
	}

	@Override
	public void setCancelled(boolean isCancelled) {
		this.isCancelled = isCancelled;
	}
}
