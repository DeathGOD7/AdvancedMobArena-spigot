package su.nightexpress.ama.api.arena.game.event;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.ama.api.arena.IArena;
import su.nightexpress.ama.api.arena.type.EndType;

public class ArenaGameEndEvent extends ArenaGameEventEvent {

	private final EndType type;
	
	public ArenaGameEndEvent(@NotNull IArena arena, @NotNull EndType type) {
		super(arena, type.getGameEventType());
		this.type = type;
	}

	@NotNull
	public EndType getType() {
		return this.type;
	}
}
