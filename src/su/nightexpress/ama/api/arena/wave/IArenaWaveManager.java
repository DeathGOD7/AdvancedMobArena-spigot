package su.nightexpress.ama.api.arena.wave;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.manager.ConfigHolder;
import su.nexmedia.engine.api.manager.IEditable;
import su.nexmedia.engine.api.manager.ILoadable;
import su.nexmedia.engine.utils.NumberUT;
import su.nexmedia.engine.utils.TimeUT;
import su.nightexpress.ama.api.arena.config.IArenaObject;
import su.nightexpress.ama.api.arena.config.IProblematic;

import java.util.Map;
import java.util.function.UnaryOperator;

public interface IArenaWaveManager extends IArenaObject, ConfigHolder, ILoadable, IProblematic, IEditable {

    String PLACEHOLDER_DELAY_FIRST = "%arena_waves_delay_first%";
    String PLACEHOLDER_DELAY_DEFAULT = "%arena_waves_delay_default%";
    String PLACEHOLDER_FINAL_WAVE = "%arena_waves_final_wave%";
    String PLACEHOLDER_GRADUAL_ENABLED = "%arena_waves_gradual_enabled%";
    String PLACEHOLDER_GRADUAL_FIRST_PERCENT = "%arena_waves_gradual_first_percent%";
    String PLACEHOLDER_GRADUAL_NEXT_PERCENT = "%arena_waves_gradual_next_percent%";
    String PLACEHOLDER_GRADUAL_NEXT_INTERVAL = "%arena_waves_gradual_next_interval%";
    String PLACEHOLDER_GRADUAL_NEXT_KILL_PERCENT = "%arena_waves_gradual_next_kill_percent%";

    @Override
    @NotNull
    default UnaryOperator<String> replacePlaceholders() {
        return str -> {
            str = IProblematic.super.replacePlaceholders().apply(str);
            return str
                    .replace(PLACEHOLDER_DELAY_FIRST, TimeUT.formatTime(this.getDelayFirst() * 1000L))
                    .replace(PLACEHOLDER_DELAY_DEFAULT, TimeUT.formatTime(this.getDelayDefault() * 1000L))
                    .replace(PLACEHOLDER_FINAL_WAVE, String.valueOf(this.getFinalWave()))
                    .replace(PLACEHOLDER_GRADUAL_ENABLED, plugin().lang().getBool(this.isGradualSpawnEnabled()))
                    .replace(PLACEHOLDER_GRADUAL_FIRST_PERCENT, NumberUT.format(this.getGradualSpawnPercentFirst()))
                    .replace(PLACEHOLDER_GRADUAL_NEXT_PERCENT, NumberUT.format(this.getGradualSpawnNextPercent()))
                    .replace(PLACEHOLDER_GRADUAL_NEXT_INTERVAL, TimeUT.formatTime(this.getGradualSpawnNextInterval() * 1000L))
                    .replace(PLACEHOLDER_GRADUAL_NEXT_KILL_PERCENT, NumberUT.format(this.getGradualSpawnNextKillPercent()))
                    ;
        };
    }

    @NotNull Map<String, IArenaWave> getWaves();

    @Nullable
    default IArenaWave getWave(@NotNull String id) {
        return this.getWaves().get(id.toLowerCase());
    }

    int getFinalWave();

    void setFinalWave(int finalWave);

    int getDelayFirst();

    void setDelayFirst(int delayFirst);

    int getDelayDefault();

    void setDelayDefault(int delayDefault);

    boolean isGradualSpawnEnabled();

    void setGradualSpawnEnabled(boolean isGradualSpawningEnabled);

    double getGradualSpawnPercentFirst();

    void setGradualSpawnPercentFirst(double gradualSpawnPercentFirst);

    int getGradualSpawnNextInterval();

    void setGradualSpawnNextInterval(int gradualSpawnNextInterval);

    double getGradualSpawnNextPercent();

    void setGradualSpawnNextPercent(double gradualSpawnNextPercent);

    double getGradualSpawnNextKillPercent();

    void setGradualSpawnNextKillPercent(double gradualSpawnNextKillPercent);
}
