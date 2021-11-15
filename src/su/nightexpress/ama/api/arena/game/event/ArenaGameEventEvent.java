package su.nightexpress.ama.api.arena.game.event;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.ama.api.arena.IArena;
import su.nightexpress.ama.api.arena.event.ArenaEvent;
import su.nightexpress.ama.api.arena.game.ArenaGameEventType;

public abstract class ArenaGameEventEvent extends ArenaEvent {

    private final ArenaGameEventType eventType;

    public ArenaGameEventEvent(@NotNull IArena arena, @NotNull ArenaGameEventType eventType) {
        super(arena);
        this.eventType = eventType;
    }

    @NotNull
    public ArenaGameEventType getEventType() {
        return eventType;
    }
}
