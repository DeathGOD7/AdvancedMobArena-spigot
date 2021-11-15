package su.nightexpress.ama.arena.spot;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.config.api.JYML;
import su.nightexpress.ama.api.arena.spot.IArenaSpot;
import su.nightexpress.ama.api.arena.spot.IArenaSpotManager;
import su.nightexpress.ama.arena.config.ArenaConfig;
import su.nightexpress.ama.arena.editor.spots.EditorSpotList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArenaSpotManager implements IArenaSpotManager {

	private final ArenaConfig arenaConfig;
	
	private Map<String, IArenaSpot> spots;
	private EditorSpotList          editor;

	public static final String DIR_SPOTS = "/spots/";

	public ArenaSpotManager(@NotNull ArenaConfig arenaConfig) {
		this.arenaConfig = arenaConfig;
	}
	
	@Override
	public void setup() {
		this.spots = new HashMap<>();
		
		for (JYML cfg : JYML.loadAll(arenaConfig.getFile().getParentFile().getAbsolutePath() + DIR_SPOTS, false)) {
			try {
				ArenaSpot spot = new ArenaSpot(this.arenaConfig, cfg);
				this.spots.put(spot.getId(), spot);
			}
			catch (Exception ex) {
				arenaConfig.plugin().error("Could not load '" + cfg.getFile().getName() + "' spot for '" + arenaConfig.getId() + "' arena!");
				ex.printStackTrace();
			}
		}
	}
	
	@Override
	public void shutdown() {
		if (this.editor != null) {
			this.editor.clear();
			this.editor = null;
		}
		this.getSpots().forEach(IArenaSpot::clear);
		this.getSpots().clear();
	}
	
	@Override
	@NotNull
	public List<String> getProblems() {
		List<String> list = new ArrayList<>();
		this.getSpots().forEach(spot -> {
			if (spot.isActive() && spot.hasProblems()) {
				list.add("Problems with " + spot.getId() + " spot!");
			}
		});
		
		return list;
	}

	@NotNull
	@Override
	public EditorSpotList getEditor() {
		if (this.editor == null) {
			this.editor = new EditorSpotList(this);
		}
		return editor;
	}

	@Override
	@NotNull
	public ArenaConfig getArenaConfig() {
		return this.arenaConfig;
	}

	@Override
	@NotNull
	public Map<String, IArenaSpot> getSpotsMap() {
		return this.spots;
	}

	@Override
	public void addSpot(@NotNull IArenaSpot spot) {
		this.spots.put(spot.getId(), spot);
	}

	@Override
	public void removeSpot(@NotNull IArenaSpot spot) {
		if (spot.getFile().delete()) {
			spot.clear();
			this.spots.remove(spot.getId());
		}
	}
}
