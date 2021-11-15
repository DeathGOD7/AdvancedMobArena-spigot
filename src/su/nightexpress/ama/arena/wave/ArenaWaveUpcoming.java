package su.nightexpress.ama.arena.wave;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.ama.api.arena.region.IArenaRegionWave;
import su.nightexpress.ama.api.arena.wave.IArenaWaveMob;
import su.nightexpress.ama.api.arena.wave.IArenaWaveUpcoming;

import java.util.List;
import java.util.Map;

public class ArenaWaveUpcoming implements IArenaWaveUpcoming {

    private final IArenaRegionWave    regionWave;
    private final List<IArenaWaveMob> mobs;
    private final List<Location> spawners;

    public ArenaWaveUpcoming(@NotNull IArenaRegionWave regionWave, @NotNull List<IArenaWaveMob> mobs) {
        this.regionWave = regionWave;
        this.mobs = mobs;

        this.spawners = regionWave.getRegion().getMobSpawners().entrySet().stream()
                .filter(entry -> regionWave.getSpawnerIds().contains(entry.getKey()))
                .map(Map.Entry::getValue).toList();
    }

    @NotNull
    @Override
    public IArenaRegionWave getRegionWave() {
        return regionWave;
    }

    @Override
    @NotNull
    public List<IArenaWaveMob> getPreparedMobs() {
        return this.mobs;
    }

    @NotNull
    @Override
    public List<Location> getPreparedSpawners() {
        return spawners;
    }
}
