package su.nightexpress.ama.api.arena.game.event;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.ama.api.arena.IArena;
import su.nightexpress.ama.api.arena.game.ArenaGameEventType;
import su.nightexpress.ama.api.arena.game.event.ArenaGameEventEvent;

public class ArenaGameStartEvent extends ArenaGameEventEvent {

	public ArenaGameStartEvent(@NotNull IArena arena) {
		super(arena, ArenaGameEventType.GAME_START);
	}

}
