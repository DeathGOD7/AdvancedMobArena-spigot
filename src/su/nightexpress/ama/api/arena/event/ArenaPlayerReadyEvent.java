package su.nightexpress.ama.api.arena.event;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.ama.api.arena.IArena;
import su.nightexpress.ama.arena.ArenaPlayer;

public class ArenaPlayerReadyEvent extends ArenaPlayerEvent {

    public ArenaPlayerReadyEvent(@NotNull IArena arena, @NotNull ArenaPlayer arenaPlayer) {
        super(arena, arenaPlayer);
    }

    public boolean isReady() {
        return this.getArenaPlayer().isReady();
    }
}
