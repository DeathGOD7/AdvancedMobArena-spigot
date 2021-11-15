package su.nightexpress.ama.arena.game.trigger;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.ama.AMA;
import su.nightexpress.ama.api.arena.IArena;
import su.nightexpress.ama.api.arena.config.IArenaConfig;
import su.nightexpress.ama.api.arena.game.ArenaGameEventType;
import su.nightexpress.ama.api.arena.game.event.ArenaGameEventEvent;
import su.nightexpress.ama.arena.game.trigger.value.AbstractArenaGameTriggerValue;
import su.nightexpress.ama.arena.game.trigger.value.ArenaGameTriggerValueBoolean;

public class ArenaGameEventTriggerBoolean extends AbstractArenaGameEventTrigger<Boolean> {

    public ArenaGameEventTriggerBoolean(
            @NotNull IArenaConfig arenaConfig, @NotNull ArenaGameEventType eventType, @NotNull String inputs) {
        super(arenaConfig, eventType, inputs);
    }

    @Override
    protected void loadValues(@NotNull String[] values) {
        for (String input: values) {
            this.values.add(new ArenaGameTriggerValueBoolean(input));
        }
    }

    @Override
    public boolean isReady(@NotNull ArenaGameEventEvent event) {
        if (event.getEventType() != this.getType()) return false;

        IArena arena = event.getArena();
        boolean arenaValue = switch (this.getType()) {
            case GAME_START, GAME_END_LOSE, GAME_END_TIME, GAME_END_WIN -> true;
            default -> false;
        };
        return this.test(arenaValue);
    }

    @Override
    @NotNull
    protected String formatValue(@NotNull AbstractArenaGameTriggerValue<Boolean> triggerValue) {
        AMA plugin = this.arenaConfig.plugin();
        return plugin.lang().getBool(triggerValue.getValue());
    }
}
