package su.nightexpress.ama.arena.game.trigger.value;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.utils.StringUT;

public class ArenaGameTriggerValueNumber extends AbstractArenaGameTriggerValue<Double> {

    public ArenaGameTriggerValueNumber(@NotNull String value) {
        super(value);

        this.predicate = (arenaValue) -> {
            if (this.isInterval()) {
                if (arenaValue % (int) this.getValue().doubleValue() != 0) return false;
                return !this.isNegated();
            }

            if (this.isGreater()) {
                return this.isNegated() == (arenaValue <= this.getValue());
            }
            if (this.isSmaller()) {
                return this.isNegated() == (arenaValue >= this.getValue());
            }

            return this.isNegated() == (arenaValue != this.getValue().doubleValue());
        };
    }

    @Override
    protected void loadValue(@NotNull String input) {
        this.value = StringUT.getDouble(input, 0D, true);
    }
}
