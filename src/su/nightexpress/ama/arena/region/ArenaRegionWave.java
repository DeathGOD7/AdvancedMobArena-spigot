package su.nightexpress.ama.arena.region;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.ama.api.arena.game.IArenaGameEventTrigger;
import su.nightexpress.ama.api.arena.region.IArenaRegion;
import su.nightexpress.ama.api.arena.region.IArenaRegionWave;
import su.nightexpress.ama.arena.editor.regions.EditorRegionWaveSettings;

import java.util.Set;

public class ArenaRegionWave implements IArenaRegionWave {

	private final IArenaRegion                region;
	private final Set<IArenaGameEventTrigger> triggers;
	private final String id;

	private Set<String> arenaWaveIds;
	private Set<String> spawnerIds;
	
	private EditorRegionWaveSettings editor;
	
	public ArenaRegionWave(
			@NotNull IArenaRegion region,
			@NotNull String id,
			@NotNull Set<String> arenaWaveIds,
			@NotNull Set<String> spawnerIds,
			@NotNull Set<IArenaGameEventTrigger> triggers) {
		this.region = region;
		this.id = id.toLowerCase();
		this.setArenaWaveIds(arenaWaveIds);
		this.setSpawnerIds(spawnerIds);
		this.triggers = triggers;
	}
	
	@Override
	public void clear() {
		if (this.editor != null) {
			this.editor.clear();
			this.editor = null;
		}
	}

	@Override
	@NotNull
	public EditorRegionWaveSettings getEditor() {
		if (this.editor == null) {
			this.editor = new EditorRegionWaveSettings(this);
		}
		return this.editor;
	}

	@NotNull
	@Override
	public Set<IArenaGameEventTrigger> getTriggers() {
		return triggers;
	}

	@NotNull
	@Override
	public IArenaRegion getRegion() {
		return region;
	}

	@NotNull
	public String getId() {
		return this.id;
	}

	@NotNull
	@Override
	public Set<String> getArenaWaveIds() {
		return arenaWaveIds;
	}

	@Override
	public void setArenaWaveIds(@NotNull Set<String> arenaWaveIds) {
		this.arenaWaveIds = arenaWaveIds;
	}

	@Override
	@NotNull
	public Set<String> getSpawnerIds() {
		return this.spawnerIds;
	}

	@Override
	public void setSpawnerIds(@NotNull Set<String> spawnerIds) {
		this.spawnerIds = spawnerIds;
	}
}
