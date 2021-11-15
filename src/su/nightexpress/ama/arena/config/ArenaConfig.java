package su.nightexpress.ama.arena.config;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.manager.AbstractLoadableItem;
import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.utils.StringUT;
import su.nightexpress.ama.AMA;
import su.nightexpress.ama.api.arena.config.IArenaConfig;
import su.nightexpress.ama.api.arena.game.IArenaGameplayManager;
import su.nightexpress.ama.api.arena.region.IArenaRegionManager;
import su.nightexpress.ama.api.arena.reward.IArenaRewardManager;
import su.nightexpress.ama.api.arena.shop.IArenaShopManager;
import su.nightexpress.ama.api.arena.spot.IArenaSpotManager;
import su.nightexpress.ama.api.arena.type.ArenaLocationType;
import su.nightexpress.ama.api.arena.wave.IArenaWaveManager;
import su.nightexpress.ama.arena.Arena;
import su.nightexpress.ama.arena.editor.arena.EditorArenaMain;
import su.nightexpress.ama.arena.game.ArenaGameplayManager;
import su.nightexpress.ama.arena.region.ArenaRegionManager;
import su.nightexpress.ama.arena.reward.ArenaRewardManager;
import su.nightexpress.ama.arena.shop.ArenaShopManager;
import su.nightexpress.ama.arena.spot.ArenaSpotManager;
import su.nightexpress.ama.arena.wave.ArenaWaveManager;

import java.io.File;
import java.util.*;

public class ArenaConfig extends AbstractLoadableItem<AMA> implements IArenaConfig {

	private       Arena   arena;
	private       boolean isActive;
	private final String  name;

	private boolean isPermissionRequired;
	private double joinMoneyRequired;

	private final Map<ArenaLocationType, Location> locations;
	
	private IArenaWaveManager     waveManager;
	private IArenaRegionManager   regionManager;
	private IArenaGameplayManager gameplayManager;
	private IArenaSpotManager     spotManager;
	private IArenaRewardManager   rewardManager;
	private IArenaShopManager     shopManager;
	
	private EditorArenaMain editorMain;
	
	public ArenaConfig(@NotNull AMA plugin, @NotNull String path) {
		this(plugin, new JYML(new File(path)));
		this.setActive(false);
	}
	
	public ArenaConfig(@NotNull AMA plugin, @NotNull JYML cfg) {
		super(plugin, cfg);
		
		this.setActive(cfg.getBoolean("Active"));
		this.name = StringUT.color(cfg.getString("Name", this.getId() + " Arena"));

		String path = "Join_Requirements.";
		this.setPermissionRequired(cfg.getBoolean(path + "Permission"));
		this.setJoinMoneyRequired(cfg.getDouble(path + "Money"));

		this.locations = new HashMap<>();
		for (ArenaLocationType locationType : ArenaLocationType.values()) {
			this.locations.put(locationType, cfg.getLocation("Locations." + locationType.name()));
		}
		
		this.setup();
	}
	
	@Override
	public void setup() {
		this.waveManager = new ArenaWaveManager(this);
		this.waveManager.setup();
		
		this.regionManager = new ArenaRegionManager(this);
		this.regionManager.setup();
		
		this.gameplayManager = new ArenaGameplayManager(this);
		this.gameplayManager.setup();
		
		this.spotManager = new ArenaSpotManager(this);
		this.spotManager.setup();
		
		this.rewardManager = new ArenaRewardManager(this);
		this.rewardManager.setup();
		
		this.shopManager = new ArenaShopManager(this);
		this.shopManager.setup();
		
		this.getConfig().saveChanges();
	}
	
	@Override
	public void shutdown() {
		if (this.editorMain != null) {
			this.editorMain.clear();
			this.editorMain = null;
		}
		if (this.spotManager != null) {
			this.spotManager.shutdown();
			this.spotManager = null;
		}
		if (this.regionManager != null) {
			this.regionManager.shutdown();
			this.regionManager = null;
		}
		if (this.gameplayManager != null) {
			this.gameplayManager.shutdown();
			this.gameplayManager = null;
		}
		if (this.waveManager != null) {
			this.waveManager.shutdown();
			this.waveManager = null;
		}
		if (this.rewardManager != null) {
			this.rewardManager.shutdown();
			this.rewardManager = null;
		}
		if (this.shopManager != null) {
			this.shopManager.shutdown();
			this.shopManager = null;
		}
	}

	@Override
	public void onSave() {
		cfg.set("Active", this.isActive());
		cfg.set("Name", this.getName());

		String path = "Join_Requirements.";
		cfg.set(path + "Permission", this.isPermissionRequired());
		cfg.set(path + "Money", this.getJoinMoneyRequired());

		this.locations.forEach((locType, loc) -> {
			cfg.set("Locations." + locType.name(), loc);
		});
		
		this.waveManager.save();
		this.regionManager.save();
		this.gameplayManager.save();
		this.rewardManager.save();
		this.shopManager.save();
	}
	
	@Override
	@NotNull
	public List<String> getProblems() {
		List<String> list = new ArrayList<>();
		if (this.locations.get(ArenaLocationType.LOBBY) == null) {
			list.add("No Lobby Location");
		}
		if (this.locations.get(ArenaLocationType.SPECTATE) == null) {
			list.add("No Spectate Location");
		}
		
		if (this.getRegionManager().hasProblems()) {
			list.add("Problems in Region Manager");
		}
		if (this.getGameplayManager().hasProblems()) {
			list.add("Problems in Gameplay Manager");
		}
		if (this.getWaveManager().hasProblems()) {
			list.add("Problems in Wave Manager");
		}
		if (this.getSpotManager().hasProblems()) {
			list.add("Problems in Spot Manager");
		}
		if (this.getRewardManager().hasProblems()) {
			list.add("Problems in Reward Manager");
		}
		
		return list;
	}

	@Override
	@NotNull
	public EditorArenaMain getEditor() {
		if (this.editorMain == null) {
			this.editorMain = new EditorArenaMain(this.plugin, this);
		}
		return this.editorMain;
	}

	@NotNull
	@Override
	public Arena getArena() {
		if (this.arena == null) {
			this.arena = new Arena(this);
		}
		return arena;
	}

	public boolean isActive() {
		return this.isActive;
	}
	
	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}
	
	@NotNull
	public String getName() {
		return this.name;
	}

	@Override
	public boolean isPermissionRequired() {
		return isPermissionRequired;
	}

	@Override
	public void setPermissionRequired(boolean permissionRequired) {
		isPermissionRequired = permissionRequired;
	}

	@Override
	public double getJoinMoneyRequired() {
		return joinMoneyRequired;
	}

	@Override
	public void setJoinMoneyRequired(double joinMoneyRequired) {
		this.joinMoneyRequired = joinMoneyRequired;
	}

	@Override
	@NotNull
	public Location getLocation(@NotNull ArenaLocationType locationType) {
		return this.locations.get(locationType);
	}

	@Override
	public void setLocation(@NotNull ArenaLocationType locationType, @Nullable Location location) {
		this.locations.put(locationType, location);
	}

	@NotNull
	public IArenaWaveManager getWaveManager() {
		return this.waveManager;
	}
	
	@NotNull
	public IArenaRegionManager getRegionManager() {
		return this.regionManager;
	}
	
	@NotNull
	public IArenaGameplayManager getGameplayManager() {
		return this.gameplayManager;
	}
	
	@NotNull
	public IArenaSpotManager getSpotManager() {
		return this.spotManager;
	}
	
	@NotNull
	public IArenaRewardManager getRewardManager() {
		return rewardManager;
	}
	
	@NotNull
	public IArenaShopManager getShopManager() {
		return shopManager;
	}
}
