package su.nightexpress.ama.api.arena.event;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.manager.api.event.IEvent;
import su.nightexpress.ama.api.arena.IArena;

public abstract class ArenaEvent extends IEvent {

	private final IArena arena;

	public ArenaEvent(@NotNull IArena arena) {
	    this.arena = arena;
	}
	
	@NotNull
	public IArena getArena() {
		return this.arena;
	}
}
