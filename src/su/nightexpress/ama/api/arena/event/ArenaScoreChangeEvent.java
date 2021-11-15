package su.nightexpress.ama.api.arena.event;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.ama.api.arena.IArena;
import su.nightexpress.ama.api.arena.game.ArenaGameEventType;
import su.nightexpress.ama.api.arena.game.event.ArenaGameEventEvent;

public class ArenaScoreChangeEvent extends ArenaGameEventEvent {

	private final int oldScore;
	private final int newScore;
	
	public ArenaScoreChangeEvent(@NotNull IArena arena, @NotNull ArenaGameEventType eventType, int oldScore, int newScore) {
		super(arena, eventType);
		this.oldScore = oldScore;
		this.newScore = newScore;
	}

	public int getOldScore() {
		return this.oldScore;
	}
	
	public int getNewScore() {
		return this.newScore;
	}
}
