package su.nightexpress.ama.api.arena.wave;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.manager.ICleanable;
import su.nexmedia.engine.api.manager.IEditable;
import su.nexmedia.engine.api.manager.IPlaceholder;
import su.nexmedia.engine.utils.random.Rnd;
import su.nightexpress.ama.api.arena.config.IArenaObject;

import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

public interface IArenaWave extends IArenaObject, IEditable, ICleanable, IPlaceholder {

    String PLACEHOLDER_ID = "%arena_wave_id%";
    String PLACEHOLDER_MOBS = "%arena_wave_mobs%";

    @Override
    @NotNull
    default UnaryOperator<String> replacePlaceholders() {
        return str -> str
                .replace(PLACEHOLDER_ID, this.getId())
                .replace(PLACEHOLDER_MOBS, String.join("\n", this.getMobs().values().stream()
                        .map(IArenaWaveMob::getMobId).toList()))
                ;
    }

    @NotNull String getId();

    @NotNull Map<String, IArenaWaveMob> getMobs();

    void setMobs(@NotNull Map<String, IArenaWaveMob> mobs);

    @NotNull Map<String, IArenaWaveAmplificator> getAmplificators();

    void setAmplificators(@NotNull Map<String, IArenaWaveAmplificator> amplificators);

    @NotNull
    default List<IArenaWaveMob> getMobsByChance() {
        return this.getMobs().values().stream()
                .filter(mob -> mob.getAmount() > 0 && Rnd.get(true) < mob.getChance()).toList();
    }
}
