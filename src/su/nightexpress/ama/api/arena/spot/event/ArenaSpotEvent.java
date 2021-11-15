package su.nightexpress.ama.api.arena.spot.event;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.ama.api.arena.IArena;
import su.nightexpress.ama.api.arena.game.ArenaGameEventType;
import su.nightexpress.ama.api.arena.game.event.ArenaGameEventEvent;
import su.nightexpress.ama.api.arena.spot.IArenaSpot;

public abstract class ArenaSpotEvent extends ArenaGameEventEvent {

	private final IArenaSpot spot;
	
	public ArenaSpotEvent(
			@NotNull IArena arena,
			@NotNull ArenaGameEventType eventType,
			@NotNull IArenaSpot spot) {
		super(arena, eventType);
		this.spot = spot;
	}

	@NotNull
	public IArenaSpot getSpot() {
		return this.spot;
	}
}
