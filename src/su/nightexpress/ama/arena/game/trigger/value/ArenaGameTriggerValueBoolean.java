package su.nightexpress.ama.arena.game.trigger.value;

import org.jetbrains.annotations.NotNull;

public class ArenaGameTriggerValueBoolean extends AbstractArenaGameTriggerValue<Boolean> {

    public ArenaGameTriggerValueBoolean(@NotNull String input) {
        super(input);

        this.predicate = (arenaValue -> {
            return this.isNegated() == (arenaValue != this.getValue());
        });
    }

    @Override
    protected void loadValue(@NotNull String input) {
        this.value = Boolean.valueOf(input);
    }
}
