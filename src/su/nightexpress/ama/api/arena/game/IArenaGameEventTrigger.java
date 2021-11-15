package su.nightexpress.ama.api.arena.game;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.manager.IPlaceholder;
import su.nightexpress.ama.api.arena.game.event.ArenaGameEventEvent;

public interface IArenaGameEventTrigger extends IPlaceholder {

    String PLACEHOLDER_TYPE = "%trigger_type%";
    String PLACEHOLDER_VALUE = "%trigger_value%";
    String PLACEHOLDER_DEEP_VALUE = "%value%";

    @NotNull ArenaGameEventType getType();

    boolean isReady(@NotNull ArenaGameEventEvent event);
}
