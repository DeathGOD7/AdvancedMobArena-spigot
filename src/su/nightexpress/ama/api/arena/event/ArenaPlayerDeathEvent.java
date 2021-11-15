package su.nightexpress.ama.api.arena.event;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.ama.api.arena.IArena;
import su.nightexpress.ama.api.arena.game.ArenaGameEventType;
import su.nightexpress.ama.arena.ArenaPlayer;

public class ArenaPlayerDeathEvent extends ArenaPlayerGameEvent {

    public ArenaPlayerDeathEvent(@NotNull IArena arena, @NotNull ArenaPlayer arenaPlayer) {
        super(arena, arenaPlayer, ArenaGameEventType.PLAYER_DEATH);
    }
}
