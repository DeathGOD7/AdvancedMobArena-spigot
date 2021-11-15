package su.nightexpress.ama.arena.region;

import org.bukkit.Location;
import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.manager.AbstractLoadableItem;
import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.utils.Constants;
import su.nexmedia.engine.utils.StringUT;
import su.nightexpress.ama.AMA;
import su.nightexpress.ama.api.arena.game.IArenaGameEventTrigger;
import su.nightexpress.ama.api.arena.game.event.ArenaGameEventEvent;
import su.nightexpress.ama.api.arena.region.IArenaRegion;
import su.nightexpress.ama.api.arena.region.IArenaRegionContainer;
import su.nightexpress.ama.api.arena.region.IArenaRegionWave;
import su.nightexpress.ama.api.arena.type.ArenaLockState;
import su.nightexpress.ama.api.arena.wave.IArenaWave;
import su.nightexpress.ama.api.ArenaCuboid;
import su.nightexpress.ama.arena.config.ArenaConfig;
import su.nightexpress.ama.arena.editor.regions.EditorRegionMain;
import su.nightexpress.ama.arena.game.trigger.AbstractArenaGameEventTrigger;
import su.nightexpress.ama.hooks.external.HologramsHK;

import java.util.*;

public class ArenaRegion extends AbstractLoadableItem<AMA> implements IArenaRegion {

	private final ArenaConfig arenaConfig;
	private boolean           isActive;
	private boolean           isDefault;

	private       ArenaLockState                                   state;
	private final Map<ArenaLockState, Set<IArenaGameEventTrigger>> stateTriggers;

	private       String                     name;
	private       ArenaCuboid                cuboid;
	private       Location                   spawnLocation;
	private final Set<String>                linkedRegions;
	private final Map<String, Location>      mobSpawners;
	private final Set<IArenaRegionWave>      waves;
	private final Set<IArenaRegionContainer> containers;

	private boolean isHologramStateEnabled;
	private Location          hologramStateLocation;
	
	private EditorRegionMain editor;
	
	public ArenaRegion(@NotNull ArenaConfig arenaConfig, @NotNull String path) {
		super(arenaConfig.plugin(), path);
		this.arenaConfig = arenaConfig;
		
		this.setActive(false);
		this.setDefault(false);
		this.setState(ArenaLockState.UNLOCKED);
		this.setName(StringUT.capitalizeFully(this.getId()) + " Region");
		this.setCuboid(ArenaCuboid.empty());
		this.setSpawnLocation(this.getCuboid().isEmpty() ? null : this.getCuboid().getCenter());
		this.linkedRegions = new HashSet<>();
		this.mobSpawners = new HashMap<>();
		this.stateTriggers = new HashMap<>();
		this.waves = new HashSet<>();
		this.containers = new HashSet<>();

		this.setHologramStateEnabled(false);
		this.updateHologramState();
	}
	
	public ArenaRegion(@NotNull ArenaConfig arenaConfig, @NotNull JYML cfg) {
		super(arenaConfig.plugin(), cfg);
		this.arenaConfig = arenaConfig;
		
		this.setActive(cfg.getBoolean("Enabled"));
		this.setDefault(cfg.getBoolean("Is_Default"));
		this.setState(this.isDefault() ? ArenaLockState.UNLOCKED : ArenaLockState.LOCKED);
		this.setName(cfg.getString("Name", this.getId()));
		
		Location from = cfg.getLocation("Bounds.From");
		Location to = cfg.getLocation("Bounds.To");
		this.setCuboid((from == null || to == null) ? ArenaCuboid.empty() : new ArenaCuboid(from, to));

		this.linkedRegions = cfg.getStringSet("Linked_Regions");
		this.spawnLocation = cfg.getLocation("Spawn_Location");
		
		this.mobSpawners = new HashMap<>();
		for (String sId : cfg.getSection("Mob_Spawners")) {
			Location loc = cfg.getLocation("Mob_Spawners." + sId);
			if (loc == null) {
				plugin.error("Invalid location for '" + sId + "' mob spawner in '" + getFile().getName() + "' region!");
				continue;
			}
			this.mobSpawners.put(sId.toLowerCase(), loc);
		}

		String path = "Entrance.State.";
		this.stateTriggers = new HashMap<>();
		for (ArenaLockState lockState : ArenaLockState.values()) {
			this.stateTriggers.put(lockState, AbstractArenaGameEventTrigger.parse(arenaConfig, cfg, path + lockState.name() + ".Triggers"));
		}

		path = "Entrance.Hologram.";
		this.setHologramStateLocation(cfg.getLocation(path + "Location"));
		this.setHologramStateEnabled(cfg.getBoolean(path + "Enabled"));
		
		this.waves = new HashSet<>();
		for (String sId : cfg.getSection("Waves.List")) {
			String path2 = "Waves.List." + sId + ".";

			Set<IArenaGameEventTrigger> triggers = AbstractArenaGameEventTrigger.parse(arenaConfig, cfg, path2 + "Triggers");

			String waveIdOld = cfg.getString(path2 + "Arena_Wave_Id", "");
			Set<String> waveIds = cfg.getStringSet(path2 + "Arena_Wave_Ids");
			if (!waveIdOld.isEmpty() && waveIds.isEmpty()) waveIds.add(waveIdOld);

			waveIds.removeIf(waveId -> {
				IArenaWave arenaWave = arenaConfig.getWaveManager().getWave(waveId);
				if (arenaWave == null) {
					plugin.error("Invalid arena wave id '" + waveId + "' in '" + this.getFile().getName() + "' region!");
					return true;
				}
				return false;
			});
			
			Set<String> waveSpawners = new HashSet<>();
			for (String spawnerName : cfg.getStringList(path2 + "Spawners")) {
				// Support for wildcard '*'
				if (spawnerName.equals(Constants.MASK_ANY)) {
					waveSpawners.addAll(this.mobSpawners.keySet());
					break;
				}
				
				spawnerName = spawnerName.toLowerCase();
				if (!this.mobSpawners.containsKey(spawnerName)) {
					plugin.error("Invalid mob spawner id '" + spawnerName + "' for '" + sId + "' wave in '" + this.getFile().getName() + "' region!");
					continue;
				}
				waveSpawners.add(spawnerName);
			}
			
			ArenaRegionWave waveRegion = new ArenaRegionWave(this, sId, waveIds, waveSpawners, triggers);
			this.waves.add(waveRegion);
		}
		
		this.containers = new HashSet<>();
		for (String sId : cfg.getSection("Containers")) {
			String path2 = "Containers." + sId + ".";
			
			Location cLoc = cfg.getLocation(path2 + "Location");
			if (cLoc == null || !(cLoc.getBlock().getState() instanceof Chest chest)) {
				plugin.error("Invalid location of '" + sId + "' container in '" + this.getFile().getName() + "' region!");
				continue;
			}

			Set<IArenaGameEventTrigger> triggers = AbstractArenaGameEventTrigger.parse(arenaConfig, cfg, path2 + "Refill.Triggers");
			int cMinItems = cfg.getInt(path2 + "Refill.Items.Min");
			int cMaxItems = cfg.getInt(path2 + "Refill.Items.Max");
			List<ItemStack> cItems = Arrays.asList(cfg.getItemList64(path2 + "Items"));

			ArenaRegionContainer container = new ArenaRegionContainer(this, chest, triggers, cMinItems, cMaxItems, cItems);
			this.containers.add(container);
		}
		
		this.updateHologramState();
		this.getProblems().forEach(problem -> {
			this.plugin().warn("Problem in '" + getFile().getName() + "' Region: " + problem);
		});
	}

	@Override
	public void onSave() {
		cfg.set("Enabled", this.isActive());
		cfg.set("Is_Default", this.isDefault());
		cfg.set("Name", this.getName());
		cfg.set("Bounds.From", this.getCuboid().getLocationMin());
		cfg.set("Bounds.To", this.getCuboid().getLocationMax());
		cfg.set("Spawn_Location", this.getSpawnLocation());
		cfg.set("Linked_Regions", this.getLinkedRegions());
		cfg.set("Mob_Spawners", null);
		this.mobSpawners.forEach((id, loc) -> {
			cfg.set("Mob_Spawners." + id, loc);
		});

		cfg.set("Entrance.State", null);
		this.getStateTriggers().forEach((lockState, triggers) -> {
			String path = "Entrance.State." + lockState.name() + ".Triggers.";
			triggers.forEach(trigger -> {
				if (!(trigger instanceof  AbstractArenaGameEventTrigger<?> trigger1)) return;
				trigger1.saveTo(cfg, path);
			});
		});

		cfg.set("Entrance.Hologram.Enabled", this.isHologramStateEnabled());
		cfg.set("Entrance.Hologram.Location", this.getHologramStateLocation());

		cfg.set("Waves.List", null);
		for (IArenaRegionWave regionWave : this.getWaves()) {
			String path2 = "Waves.List." + regionWave.getId() + ".";

			regionWave.getTriggers().forEach(trigger -> {
				if (!(trigger instanceof  AbstractArenaGameEventTrigger<?> trigger1)) return;
				trigger1.saveTo(cfg, path2 + "Triggers.");
			});
			cfg.set(path2 + "Arena_Wave_Id", null);
			cfg.set(path2 + "Arena_Wave_Ids", regionWave.getArenaWaveIds());
			cfg.set(path2 + "Spawners", regionWave.getSpawnerIds());
		}
		
		cfg.set("Containers", null);
		int counter = 0;
		for (IArenaRegionContainer container : this.containers) {
			String path2 = "Containers." + (counter++) + ".";
			
			cfg.set(path2 + "Location", container.getLocation());
			container.getTriggers().forEach(trigger -> {
				if (!(trigger instanceof AbstractArenaGameEventTrigger<?> trigger1)) return;
				trigger1.saveTo(cfg, path2 + "Refill.Triggers.");
			});
			cfg.set(path2 + "Refill.Items.Min", container.getMinItems());
			cfg.set(path2 + "Refill.Items.Max", container.getMaxItems());
			cfg.setItemList64(path2 + "Items", container.getItems());
		}
	}
	
	@Override
	public void clear() {
		if (this.editor != null) {
			this.editor.clear();
			this.editor = null;
		}
		if (this.waves != null) {
			this.waves.forEach(IArenaRegionWave::clear);
			this.waves.clear();
		}
		this.deleteHologramState();
	}

	@Override
	@NotNull
	public List<String> getProblems() {
		List<String> list = new ArrayList<>();
		if (this.getCuboid().isEmpty()) {
			list.add(PROBLEM_REGION_CUBOID_INVALID);
		}
		if (this.getSpawnLocation() == null || !this.getCuboid().contains(this.getSpawnLocation())) {
			list.add(PROBLEM_REGION_SPAWN_LOCATION);
		}
		if (this.getMobSpawners().isEmpty()) {
			list.add(PROBLEM_REGION_SPAWNERS_EMPTY);
		}
		return list;
	}
	
	@Override
	@NotNull
	public EditorRegionMain getEditor() {
		if (this.editor == null) {
			this.editor = new EditorRegionMain(this);
		}
		return this.editor;
	}

	@Override
	public boolean onGameEvent(@NotNull ArenaGameEventEvent gameEvent) {
		if (IArenaRegion.super.onGameEvent(gameEvent)) {
			this.updateHologramState();
			return true;
		}
		return false;
	}

	@Override
	@NotNull
	public ArenaConfig getArenaConfig() {
		return this.arenaConfig;
	}

	@NotNull
	@Override
	public ArenaLockState getState() {
		return state;
	}

	@Override
	public void setState(@NotNull ArenaLockState state) {
		this.state = state;
	}

	@Override
	@NotNull
	public String getName() {
		return this.name;
	}
	
	@Override
	public void setName(@NotNull String name) {
		this.name = StringUT.color(name);
	}

	@Override
	public boolean isActive() {
		return this.isActive;
	}
	
	@Override
	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	@Override
	public boolean isDefault() {
		return this.isDefault;
	}

	@Override
	public void setDefault(boolean isDefault) {
		this.isDefault = isDefault;
	}

	/**
	 * Returns triggers for the OPPOSITE state of the current region Lock State.
	 * If region is LOCKED, then returns triggers for UNLOCKED state, and vice versa.
	 * @return Set of GameEvent triggers.
	 */
	@Override
	@NotNull
	public Set<IArenaGameEventTrigger> getTriggers() {
		return this.getStateTriggers(this.getState().getOpposite());
	}

	@NotNull
	@Override
	public Map<ArenaLockState, Set<IArenaGameEventTrigger>> getStateTriggers() {
		return stateTriggers;
	}

	@NotNull
	@Override
	public Set<String> getLinkedRegions() {
		return linkedRegions;
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
	public Location getSpawnLocation() {
		return this.spawnLocation == null ? null : this.spawnLocation.clone();
	}
	
	@Override
	public void setSpawnLocation(Location spawnLocation) {
		this.spawnLocation = spawnLocation == null ? null : spawnLocation.clone();
	}
	
	@Override
	@NotNull
	public Map<String, Location> getMobSpawners() {
		return this.mobSpawners;
	}
	
	@Override
	@NotNull
	public Set<IArenaRegionWave> getWaves() {
		return this.waves;
	}
	
	@Override
	@NotNull
	public Set<IArenaRegionContainer> getContainers() {
		return this.containers;
	}

	@Override
	public boolean isHologramStateEnabled() {
		return this.isHologramStateEnabled;
	}

	@Override
	public void setHologramStateEnabled(boolean isHologramStateEnabled) {
		this.isHologramStateEnabled = isHologramStateEnabled;
	}

	@Override
	public Location getHologramStateLocation() {
		return hologramStateLocation;
	}

	@Override
	public void setHologramStateLocation(Location hologramStateLocation) {
		this.deleteHologramState();
		this.hologramStateLocation = hologramStateLocation;
		this.updateHologramState();
	}

	@Override
	public void updateHologramState() {
		if (!this.isHologramStateEnabled() || this.getHologramStateLocation() == null) return;

		HologramsHK holograms = this.plugin.getHolograms();
		if (holograms != null) {
			holograms.updateRegionState(this);
		}
	}

	@Override
	public void deleteHologramState() {
		if (!this.isHologramStateEnabled() || this.getHologramStateLocation() == null) return;

		HologramsHK holograms = this.plugin.getHolograms();
		if (holograms != null) {
			holograms.deleteRegionState(this);
		}
	}
}
