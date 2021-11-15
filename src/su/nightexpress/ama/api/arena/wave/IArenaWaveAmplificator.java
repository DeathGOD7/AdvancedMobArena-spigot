package su.nightexpress.ama.api.arena.wave;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.manager.ICleanable;
import su.nexmedia.engine.api.manager.IEditable;
import su.nightexpress.ama.api.arena.IArena;
import su.nightexpress.ama.api.arena.game.IArenaGameEventListener;
import su.nightexpress.ama.api.arena.game.event.ArenaGameEventEvent;

import java.util.function.UnaryOperator;

public interface IArenaWaveAmplificator extends IArenaGameEventListener, IEditable, ICleanable {

    String PLACEHOLDER_ID = "%wave_amplificator_id%";
    String PLACEHOLDER_VALUE_AMOUNT = "%wave_amplificator_value_amount%";
    String PLACEHOLDER_VALUE_LEVEL = "%wave_amplificator_value_level%";

    @Override
    @NotNull
    default UnaryOperator<String> replacePlaceholders() {
        return str -> IArenaGameEventListener.super.replacePlaceholders().apply(str
                .replace(PLACEHOLDER_ID, this.getId())
                .replace(PLACEHOLDER_VALUE_AMOUNT, String.valueOf(this.getValueAmount()))
                .replace(PLACEHOLDER_VALUE_LEVEL, String.valueOf(this.getValueLevel()))
        );
    }

    @Override
    default boolean onGameEvent(@NotNull ArenaGameEventEvent gameEvent) {
        if (!this.isReady(gameEvent)) return false;

        IArena arena = gameEvent.getArena();
        arena.addWaveAmplificatorAmount(this.getArenaWave().getId(), this.getValueAmount());
        arena.addWaveAmplificatorLevel(this.getArenaWave().getId(), this.getValueLevel());
        return true;
    }

    @NotNull String getId();

    @NotNull IArenaWave getArenaWave();

    int getValueAmount();

    void setValueAmount(int valueAmount);

    int getValueLevel();

    void setValueLevel(int valueLevel);
}
