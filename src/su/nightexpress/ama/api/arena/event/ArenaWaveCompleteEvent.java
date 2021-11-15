package su.nightexpress.ama.api.arena.event;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.ama.api.arena.IArena;
import su.nightexpress.ama.api.arena.game.ArenaGameEventType;
import su.nightexpress.ama.api.arena.game.event.ArenaGameEventEvent;

public class ArenaWaveCompleteEvent extends ArenaGameEventEvent {

	public ArenaWaveCompleteEvent(@NotNull IArena arena) {
		super(arena, ArenaGameEventType.WAVE_END);
	}

}
