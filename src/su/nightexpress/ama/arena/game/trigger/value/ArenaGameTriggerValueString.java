package su.nightexpress.ama.arena.game.trigger.value;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.utils.Constants;

public class ArenaGameTriggerValueString extends AbstractArenaGameTriggerValue<String> {

    public ArenaGameTriggerValueString(@NotNull String input) {
        super(input);

        this.predicate = (arenaValue) -> {
            if (this.isNegated()) {
                return !(this.getValue().equalsIgnoreCase(arenaValue));
            }
            return arenaValue.equalsIgnoreCase(this.getValue()) || this.getValue().equalsIgnoreCase(Constants.MASK_ANY);
        };
    }

    @Override
    protected void loadValue(@NotNull String input) {
        this.value = input;
    }
}
