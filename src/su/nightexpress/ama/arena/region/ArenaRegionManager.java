package su.nightexpress.ama.arena.region;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.config.api.JYML;
import su.nightexpress.ama.api.arena.region.IArenaRegion;
import su.nightexpress.ama.api.arena.region.IArenaRegionManager;
import su.nightexpress.ama.arena.config.ArenaConfig;
import su.nightexpress.ama.arena.editor.regions.EditorRegionList;

import java.util.*;
import java.util.stream.Collectors;

public class ArenaRegionManager implements IArenaRegionManager {

	private final ArenaConfig arenaConfig;
	private final String      regionsPath;

	private final Map<String, IArenaRegion> regions;
	private final Map<IArenaRegion, Set<IArenaRegion>> regionsLinked;

	private EditorRegionList editor;

	public static final String DIR_REGIONS = "/regions/";
	
	public ArenaRegionManager(@NotNull ArenaConfig arenaConfig) {
		this.arenaConfig = arenaConfig;
		this.regionsPath = this.arenaConfig.getFile().getParentFile().getAbsolutePath() + DIR_REGIONS;
		
		this.regions = new HashMap<>();
		this.regionsLinked = new HashMap<>();
	}
	
	@Override
	public void setup() {
		for (JYML rCfg : JYML.loadAll(this.getRegionsPath(), false)) {
			try {
				ArenaRegion region = new ArenaRegion(this.arenaConfig, rCfg);
				this.addRegion(region);
			}
			catch (Exception e) {
				arenaConfig.plugin().error("Could not load '" + rCfg.getFile().getName() + "' region in '" + arenaConfig.getFile().getName() + "' arena!");
				e.printStackTrace();
			}
		}

		this.getRegions().forEach(region -> {
			Set<IArenaRegion> linked = region.getLinkedRegions().stream().map(this::getRegion).filter(Objects::nonNull).collect(Collectors.toSet());
			this.getLinkedRegionsMap().put(region, linked);
		});

		this.getProblems().forEach(problem -> {
			this.plugin().warn("Problem in '" + arenaConfig.getId() + "' arena Region Manager: " + problem);
		});
	}
	
	@Override
	public void shutdown() {
		if (this.editor != null) {
			this.editor.clear();
			this.editor = null;
		}
		this.getRegions().forEach(IArenaRegion::clear);
		this.getRegions().clear();
	}
	
	@Override
	public void save() {
		this.regions.values().forEach(IArenaRegion::save);
	}
	
	@Override
	@NotNull
	public List<String> getProblems() {
		List<String> list = new ArrayList<>();
		if (this.regions.isEmpty()) {
			list.add("No Regions Defined!");
		}
		if (this.getRegionDefault() == null) {
			list.add("No Default Region!");
		}
		else if (!this.getRegionDefault().isActive()) {
			list.add("Default Region is Inactive!");
		}
		
		for (IArenaRegion region : this.getRegions()) {
			if (region.isActive() && region.hasProblems()) {
				list.add("Problems with " + region.getId() + " region!");
			}
		}
		return list;
	}

	@NotNull
	@Override
	public EditorRegionList getEditor() {
		if (editor == null) {
			editor = new EditorRegionList(this);
		}
		return editor;
	}

	@Override
	@NotNull
	public ArenaConfig getArenaConfig() {
		return this.arenaConfig;
	}

	@NotNull
	public String getRegionsPath() {
		return this.regionsPath;
	}

	@Override
	@NotNull
	public Map<String, IArenaRegion> getRegionsMap() {
		return this.regions;
	}

	@Override
	@NotNull
	public Map<IArenaRegion, Set<IArenaRegion>> getLinkedRegionsMap() {
		return this.regionsLinked;
	}
}
