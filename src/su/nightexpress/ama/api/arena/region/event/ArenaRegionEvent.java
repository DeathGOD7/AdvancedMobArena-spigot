package su.nightexpress.ama.api.arena.region.event;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.ama.api.arena.IArena;
import su.nightexpress.ama.api.arena.game.event.ArenaGameEventEvent;
import su.nightexpress.ama.api.arena.game.ArenaGameEventType;
import su.nightexpress.ama.api.arena.region.IArenaRegion;

public class ArenaRegionEvent extends ArenaGameEventEvent {

	private final IArenaRegion arenaRegion;

	public ArenaRegionEvent(
			@NotNull IArena arena,
			@NotNull ArenaGameEventType eventType,
			@NotNull IArenaRegion arenaRegion
			) {
		super(arena, eventType);
	    this.arenaRegion = arenaRegion;
	}
	
	@NotNull
	public IArenaRegion getArenaRegion() {
		return this.arenaRegion;
	}
}
