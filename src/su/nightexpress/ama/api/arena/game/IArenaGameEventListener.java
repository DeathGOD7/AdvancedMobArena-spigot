package su.nightexpress.ama.api.arena.game;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.manager.IPlaceholder;
import su.nightexpress.ama.AMA;
import su.nightexpress.ama.api.arena.game.event.ArenaGameEventEvent;

import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

public interface IArenaGameEventListener extends IPlaceholder {

    String PLACEHOLDER_TRIGGERS = "%arena_game_triggers%";

    @Override
    @NotNull
    default UnaryOperator<String> replacePlaceholders() {
        AMA plugin = AMA.getInstance();
        String format = plugin.lang().Arena_Game_Trigger_Format_Full.getMsg();

        return str -> str
                .replace(PLACEHOLDER_TRIGGERS, this.getTriggers().stream()
                        .map(trigger -> trigger.replacePlaceholders().apply(format))
                        .collect(Collectors.joining("\n")))
                ;
    }

    @NotNull Set<IArenaGameEventTrigger> getTriggers();

    boolean onGameEvent(@NotNull ArenaGameEventEvent gameEvent);

    default boolean isReady(@NotNull ArenaGameEventEvent gameEvent) {
        return this.getTriggers().stream().anyMatch(trigger -> trigger.isReady(gameEvent));
    }
}
