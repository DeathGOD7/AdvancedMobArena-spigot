package su.nightexpress.ama.arena.wave;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.ama.AMA;
import su.nightexpress.ama.api.arena.game.IArenaGameEventTrigger;
import su.nightexpress.ama.api.arena.wave.IArenaWave;
import su.nightexpress.ama.api.arena.wave.IArenaWaveAmplificator;
import su.nightexpress.ama.arena.editor.waves.EditorWaveAmplificatorMain;

import java.util.HashSet;
import java.util.Set;

public class ArenaWaveAmplificator implements IArenaWaveAmplificator {

	private final IArenaWave arenaWave;
	private final String                      id;
	private final Set<IArenaGameEventTrigger> triggers;
	private       int                         valueAmount;
	private       int                         valueLevel;
	
	private EditorWaveAmplificatorMain editor;
	
	public ArenaWaveAmplificator(
			@NotNull IArenaWave arenaWave,
			@NotNull String id,
			@NotNull Set<IArenaGameEventTrigger> triggers,
			int valueAmount, int valueLevel) {
		this.arenaWave = arenaWave;
		this.id = id.toLowerCase();
		this.triggers = new HashSet<>(triggers);
		this.setValueAmount(valueAmount);
		this.setValueLevel(valueLevel);
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
	public EditorWaveAmplificatorMain getEditor() {
		if (this.editor == null) {
			this.editor = new EditorWaveAmplificatorMain(AMA.getInstance(), this);
		}
		return this.editor;
	}

	@NotNull
	public String getId() {
		return this.id;
	}

	@NotNull
	@Override
	public IArenaWave getArenaWave() {
		return arenaWave;
	}

	@NotNull
	@Override
	public Set<IArenaGameEventTrigger> getTriggers() {
		return triggers;
	}

	@Override
	public int getValueAmount() {
		return valueAmount;
	}

	@Override
	public void setValueAmount(int valueAmount) {
		this.valueAmount = valueAmount;
	}

	@Override
	public int getValueLevel() {
		return valueLevel;
	}

	@Override
	public void setValueLevel(int valueLevel) {
		this.valueLevel = valueLevel;
	}
}
