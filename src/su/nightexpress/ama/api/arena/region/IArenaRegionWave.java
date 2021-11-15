package su.nightexpress.ama.api.arena.region;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.manager.ICleanable;
import su.nexmedia.engine.api.manager.IEditable;
import su.nexmedia.engine.utils.random.Rnd;
import su.nightexpress.ama.api.arena.IArena;
import su.nightexpress.ama.api.arena.config.IArenaConfig;
import su.nightexpress.ama.api.arena.config.IArenaObject;
import su.nightexpress.ama.api.arena.game.ArenaGameEventType;
import su.nightexpress.ama.api.arena.game.IArenaGameEventListener;
import su.nightexpress.ama.api.arena.game.event.ArenaGameEventEvent;
import su.nightexpress.ama.api.arena.type.ArenaLockState;
import su.nightexpress.ama.api.arena.wave.IArenaWave;
import su.nightexpress.ama.api.arena.wave.IArenaWaveMob;
import su.nightexpress.ama.arena.wave.ArenaWaveMob;
import su.nightexpress.ama.arena.wave.ArenaWaveUpcoming;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.UnaryOperator;

public interface IArenaRegionWave extends IArenaGameEventListener, IArenaObject, IEditable, ICleanable {

    String PLACEHOLDER_ID       = "%region_wave_id%";
    String PLACEHOLDER_WAVE_IDS = "%region_wave_wave_ids%";
    String PLACEHOLDER_SPAWNERS = "%region_wave_spawners%";

    @Override
    @NotNull
    default UnaryOperator<String> replacePlaceholders() {
        IArenaWave arenaWave = this.getArenaWave();

        return str -> IArenaGameEventListener.super.replacePlaceholders().apply(str
                .replace(PLACEHOLDER_ID, this.getId())
                .replace(PLACEHOLDER_WAVE_IDS, String.join(DELIMITER_DEFAULT, this.getArenaWaveIds()))
                .replace(PLACEHOLDER_SPAWNERS, String.join(DELIMITER_DEFAULT, this.getSpawnerIds()))
        );
    }

    @Override
    default boolean onGameEvent(@NotNull ArenaGameEventEvent gameEvent) {
        if (gameEvent.getEventType() != ArenaGameEventType.WAVE_START) return false;
        if (!this.isReady(gameEvent)) return false;

        IArenaRegion region = this.getRegion();
        if (this.getRegion().getState() == ArenaLockState.LOCKED) return false;

        IArenaWave wave = this.getArenaWave();
        if (wave == null) return false;

        // TODO If gradual spawn disabled, instant spawn this wave on arena? in case when using more than wave_start triggers

        // Check if this or linked regions contains players to spawn waves there.
        Set<IArenaRegion> linked = new HashSet<>(this.getArenaConfig().getRegionManager().getLinkedRegions(region));
        boolean hasNear = !this.getRegion().getPlayers().isEmpty() || linked.stream().anyMatch(reg -> !reg.getPlayers().isEmpty());
        if (!hasNear) return false;

        IArena arena = gameEvent.getArena();

        // Generate upcoming arena waves depends on region waves and arena wave mob chances.
        // Also set mob amount and levels depends on the Amplificators.
        // Creates new instances for IArenaWaveMob to not affect default ones.
        List<IArenaWaveMob> mobs = new ArrayList<>(wave.getMobsByChance().stream().map(ArenaWaveMob::new).toList());
        mobs.forEach(mob -> mob.setAmount((int) (mob.getAmount() + arena.getWaveAmplificatorAmount(wave.getId()))));
        mobs.forEach(mob -> mob.setLevel((int) (mob.getLevel() + arena.getWaveAmplificatorLevel(wave.getId()))));
        mobs.removeIf(mob -> mob.getAmount() <= 0);
        if (mobs.isEmpty()) return false;

        arena.getUpcomingWaves().add(new ArenaWaveUpcoming(this, mobs));
        return true;
    }

    @NotNull IArenaRegion getRegion();

    @Override
    @NotNull
    default IArenaConfig getArenaConfig() {
        return this.getRegion().getArenaConfig();
    }

    @Nullable
    default IArenaWave getArenaWave() {
        String waveId = Rnd.get(this.getArenaWaveIds());
        if (waveId == null) return null;

        return this.getArenaConfig().getWaveManager().getWave(waveId);
    }

    @NotNull String getId();

    @NotNull Set<String> getArenaWaveIds();

    void setArenaWaveIds(@NotNull Set<String> arenaWaveIds);

    @NotNull Set<String> getSpawnerIds();

    void setSpawnerIds(@NotNull Set<String> spawnerIds);
}
