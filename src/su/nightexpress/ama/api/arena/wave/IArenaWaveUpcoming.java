package su.nightexpress.ama.api.arena.wave;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.ama.api.arena.region.IArenaRegionWave;

import java.util.List;

public interface IArenaWaveUpcoming {

    @NotNull IArenaRegionWave getRegionWave();

    @NotNull List<IArenaWaveMob> getPreparedMobs();

    @NotNull List<Location> getPreparedSpawners();

    default boolean isAllMobsSpawned() {
        return this.getPreparedMobs().stream().allMatch(mob -> mob.getAmount() <= 0);
    }
}
