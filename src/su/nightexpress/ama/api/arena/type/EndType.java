package su.nightexpress.ama.api.arena.type;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.ama.api.arena.game.ArenaGameEventType;

public enum EndType {

	TIMELEFT(LeaveReason.TIMELEFT),
	FINISH(LeaveReason.FINISH),
	FORCE(LeaveReason.FORCE),
	NO_REGION(LeaveReason.NO_REGION),
	;
	
	private LeaveReason reason;
	
	private EndType(LeaveReason reason) {
		this.reason = reason;
	}
	
	public LeaveReason getReason() {
		return this.reason;
	}

	@NotNull
	public ArenaGameEventType getGameEventType() {
		if (this == TIMELEFT) return ArenaGameEventType.GAME_END_TIME;
		if (this == FINISH) return  ArenaGameEventType.GAME_END_WIN;
		return ArenaGameEventType.GAME_END_LOSE;
	}
}
