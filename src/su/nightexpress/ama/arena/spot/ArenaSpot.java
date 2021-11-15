package su.nightexpress.ama.arena.spot;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.manager.AbstractLoadableItem;
import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.utils.StringUT;
import su.nightexpress.ama.AMA;
import su.nightexpress.ama.api.arena.game.IArenaGameEventTrigger;
import su.nightexpress.ama.api.arena.spot.IArenaSpot;
import su.nightexpress.ama.api.arena.spot.IArenaSpotState;
import su.nightexpress.ama.api.ArenaCuboid;
import su.nightexpress.ama.arena.config.ArenaConfig;
import su.nightexpress.ama.arena.editor.spots.EditorSpotMain;
import su.nightexpress.ama.arena.game.trigger.AbstractArenaGameEventTrigger;

import java.util.*;

public class ArenaSpot extends AbstractLoadableItem<AMA> implements IArenaSpot {

	private final ArenaConfig arenaConfig;
	
	private       boolean                      isActive;
	private       String                       name;
	private       ArenaCuboid                  cuboid;
	private final Map<String, IArenaSpotState> states;
	
	private EditorSpotMain editor;
	
	public ArenaSpot(@NotNull ArenaConfig arenaConfig, @NotNull String path, @NotNull ArenaCuboid cuboid) {
		super(arenaConfig.plugin(), path);
		this.arenaConfig = arenaConfig;
		
		this.setActive(false);
		this.setName(StringUT.capitalizeFully(this.getId()) + " Spot");
		this.setCuboid(cuboid);
		this.states = new HashMap<>();
	}

	public ArenaSpot(@NotNull ArenaConfig arenaConfig, @NotNull JYML cfg) {
		super(arenaConfig.plugin(), cfg);
		this.arenaConfig = arenaConfig;
		
		this.setActive(cfg.getBoolean("Enabled"));
		this.setName(cfg.getString("Name", this.getId()));
		this.states = new HashMap<>();
		
		Location from = cfg.getLocation("Bounds.From");
		Location to = cfg.getLocation("Bounds.To");
		if (from == null || to == null) {
			plugin.error("Invalid cuboid bounds in '" + getId() + "' spot of '" + arenaConfig.getId() + "' arena!");
			this.setCuboid(ArenaCuboid.empty());
		}
		else {
			this.setCuboid(new ArenaCuboid(from, to));
		}
		
		for (String stateId : cfg.getSection("States")) {
			String path2 = "States." + stateId + ".";
			
			Set<IArenaGameEventTrigger> triggers = AbstractArenaGameEventTrigger.parse(arenaConfig, cfg, path2 + "Triggers");
			List<String> blockSchemeRaw = new ArrayList<>(cfg.getStringList(path2 + "Scheme"));

			ArenaSpotState state = new ArenaSpotState(this, stateId, triggers, blockSchemeRaw);
			this.states.put(state.getId(), state);
		}
	}

	@Override
	public void onSave() {
		cfg.set("Enabled", this.isActive());
		cfg.set("Name", this.getName());

		cfg.set("Bounds", null);
		if (!this.cuboid.isEmpty()) {
			cfg.set("Bounds.From", cuboid.getLocationMin());
			cfg.set("Bounds.To", cuboid.getLocationMax());
		}

		cfg.set("States", null);
		this.states.forEach((id, state) -> {
			String path2 = "States." + id + ".";
			
			state.getTriggers().forEach(trigger -> {
				if (!(trigger instanceof AbstractArenaGameEventTrigger<?> trigger1)) return;
				trigger1.saveTo(cfg, path2 + "Triggers.");
			});
			cfg.set(path2 + "Scheme", state.getSchemeRaw());
		});
	}

	@Override
	@NotNull
	public List<String> getProblems() {
		List<String> list = new ArrayList<>();
		if (this.getCuboid().isEmpty()) {
			list.add("Invalid Cuboid Selection!");
		}
		if (this.getStates().isEmpty()) {
			list.add("No Spot States!");
		}
		
		return list;
	}
	
	@Override
	@NotNull
	public ArenaConfig getArenaConfig() {
		return this.arenaConfig;
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
	public EditorSpotMain getEditor() {
		if (this.editor == null) {
			this.editor = new EditorSpotMain(this);
		}
		return this.editor;
	}

	@Override
	public boolean isActive() {
		return this.isActive;
	}
	
	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}
	
	@Override
	@NotNull
	public String getName() {
		return this.name;
	}
	
	public void setName(@NotNull String name) {
		this.name = StringUT.color(name);
	}

	@Override
	@NotNull
	public ArenaCuboid getCuboid() {
		return this.cuboid;
	}
	
	@Override
	public void setCuboid(@NotNull ArenaCuboid cuboid) {
		this.cuboid = cuboid;
	}
	
	@Override
	@NotNull
	public Map<String, IArenaSpotState> getStates() {
		return this.states;
	}
}
