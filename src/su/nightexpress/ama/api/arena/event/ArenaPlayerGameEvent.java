package su.nightexpress.ama.api.arena.event;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.ama.api.arena.IArena;
import su.nightexpress.ama.api.arena.game.ArenaGameEventType;
import su.nightexpress.ama.api.arena.game.event.ArenaGameEventEvent;
import su.nightexpress.ama.arena.ArenaPlayer;

public class ArenaPlayerGameEvent extends ArenaGameEventEvent {

    protected final ArenaPlayer arenaPlayer;

    public ArenaPlayerGameEvent(@NotNull IArena arena, @NotNull ArenaPlayer arenaPlayer, @NotNull ArenaGameEventType eventType) {
        super(arena, eventType);
        this.arenaPlayer = arenaPlayer;
    }

    @NotNull
    public final ArenaPlayer getArenaPlayer() {
        return this.arenaPlayer;
    }
}
