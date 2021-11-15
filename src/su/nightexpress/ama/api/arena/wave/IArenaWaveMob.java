package su.nightexpress.ama.api.arena.wave;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.manager.IPlaceholder;
import su.nexmedia.engine.utils.NumberUT;

import java.util.function.UnaryOperator;

public interface IArenaWaveMob extends IPlaceholder {

    String PLACEHOLDER_ID = "%arena_wave_mob_id%";
    String PLACEHOLDER_AMOUNT = "%arena_wave_mob_amount%";
    String PLACEHOLDER_LEVEL = "%arena_wave_mob_level%";
    String PLACEHOLDER_CHANCE = "%arena_wave_mob_chance%";

    @Override
    @NotNull
    default UnaryOperator<String> replacePlaceholders() {
        return str -> str
                .replace(PLACEHOLDER_ID, this.getMobId())
                .replace(PLACEHOLDER_AMOUNT, String.valueOf(this.getAmount()))
                .replace(PLACEHOLDER_LEVEL, String.valueOf(this.getLevel()))
                .replace(PLACEHOLDER_CHANCE, NumberUT.format(this.getChance()))
                ;
    }

    @NotNull IArenaWave getArenaWave();

    @NotNull String getMobId();

    void setMobId(@NotNull String mobId);

    int getAmount();

    void setAmount(int amount);

    int getLevel();

    void setLevel(int level);

    double getChance();

    void setChance(double chance);
}
