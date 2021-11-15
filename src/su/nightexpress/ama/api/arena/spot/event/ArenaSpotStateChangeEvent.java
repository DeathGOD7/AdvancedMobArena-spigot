package su.nightexpress.ama.api.arena.spot.event;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.ama.api.arena.IArena;
import su.nightexpress.ama.api.arena.game.ArenaGameEventType;
import su.nightexpress.ama.api.arena.spot.IArenaSpot;
import su.nightexpress.ama.api.arena.spot.IArenaSpotState;

public class ArenaSpotStateChangeEvent extends ArenaSpotEvent {

	private final IArenaSpotState state;
	
	public ArenaSpotStateChangeEvent(
			@NotNull IArena arena,
			@NotNull IArenaSpot spot,
			@NotNull IArenaSpotState state) {
		super(arena, ArenaGameEventType.SPOT_CHANGED, spot);
		this.state = state;
	}

	@NotNull
	public IArenaSpotState getNewState() {
		return this.state;
	}
}
