package su.nightexpress.ama.arena.wave;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.ama.api.arena.wave.IArenaWave;
import su.nightexpress.ama.api.arena.wave.IArenaWaveAmplificator;
import su.nightexpress.ama.api.arena.wave.IArenaWaveMob;
import su.nightexpress.ama.arena.config.ArenaConfig;
import su.nightexpress.ama.arena.editor.waves.EditorWaveSettings;

import java.util.Map;

public class ArenaWave implements IArenaWave {

	private final ArenaConfig                         arenaConfig;
	private final String                              id;
	private       Map<String, IArenaWaveMob>          mobs;
	private       Map<String, IArenaWaveAmplificator> amplificators;

	private EditorWaveSettings editor;
	
	public ArenaWave(
			@NotNull ArenaConfig arenaConfig,
			@NotNull String id,
			@NotNull Map<String, IArenaWaveMob> mobs,
			@NotNull Map<String, IArenaWaveAmplificator> amplificators
			) {
		this.arenaConfig = arenaConfig;
		this.id = id.toLowerCase();
		this.setMobs(mobs);
		this.setAmplificators(amplificators);
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
	public EditorWaveSettings getEditor() {
		if (this.editor == null) {
			this.editor = new EditorWaveSettings(this);
		}
		return this.editor;
	}

	@NotNull
	@Override
	public ArenaConfig getArenaConfig() {
		return arenaConfig;
	}

	@NotNull
	public String getId() {
		return this.id;
	}
	
	@NotNull
	public Map<String, IArenaWaveMob> getMobs() {
		return this.mobs;
	}
	
	public void setMobs(@NotNull Map<String, IArenaWaveMob> mobs) {
		this.mobs = mobs;
	}

	@NotNull
	@Override
	public Map<String, IArenaWaveAmplificator> getAmplificators() {
		return amplificators;
	}

	@Override
	public void setAmplificators(@NotNull Map<String, IArenaWaveAmplificator> amplificators) {
		this.amplificators = amplificators;
	}
}
