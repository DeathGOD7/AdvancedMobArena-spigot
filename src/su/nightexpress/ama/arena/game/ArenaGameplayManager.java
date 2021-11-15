package su.nightexpress.ama.arena.game;

import org.bukkit.Material;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.hooks.Hooks;
import su.nexmedia.engine.utils.CollectionsUT;
import su.nexmedia.engine.utils.Constants;
import su.nightexpress.ama.api.arena.game.ArenaGameTargetType;
import su.nightexpress.ama.api.arena.game.IArenaGameCommand;
import su.nightexpress.ama.api.arena.game.IArenaGameEventTrigger;
import su.nightexpress.ama.api.arena.game.IArenaGameplayManager;
import su.nightexpress.ama.arena.config.ArenaConfig;
import su.nightexpress.ama.arena.editor.game.EditorArenaGameplay;
import su.nightexpress.ama.arena.game.trigger.AbstractArenaGameEventTrigger;
import su.nightexpress.ama.hooks.EHook;

import java.util.*;

public class ArenaGameplayManager implements IArenaGameplayManager {

	private final ArenaConfig arenaConfig;
	private final JYML        config;

	private EditorArenaGameplay editor;
	
	private int                        timeleft;
	private int                        lobbyTime;
	private double                     coinsMultiplier;
	private boolean                    isAnnouncesEnabled;
	private boolean                    isScoreboardEnabled;
	private boolean                    isShopEnabled;
	private boolean                    isHungerEnabled;
	private boolean                    isRegenerationEnabled;
	private boolean                    isItemDropEnabled;
	private boolean                    isItemPickupEnabled;
	private boolean                    isItemDurabilityEnabled;
	private boolean                    isSlimeSplitEnabled;
	private boolean                    isMobDropExpEnabled;
	private boolean                    isMobDropLootEnabled;

	private Set<IArenaGameCommand> autoCommands;
	private Set<Material> bannedItems;
	private Set<CreatureSpawnEvent.SpawnReason> allowedSpawnReasons;
	
	private int         playerMinAmount;
	private int         playerMaxAmount;
	private int         playerLivesAmount;
	private boolean		isPlayerDropItemsOnDeathEnabled;
	private boolean     isPlayerExpSavingEnabled;
	private boolean     isPlayerCommandsEnabled;
	private Set<String> playerCommandsAllowed;

	private boolean isSpectateEnabled;
	private boolean isSpectateOnDeathEnabled;
	
	private boolean              isKitsEnabled;
	private Set<String>          kitsAllowed;
	private Map<String, Integer> kitsLimits;
	
	private boolean isExternalPetsEnabled;
	private boolean isExternalMcmmoEnabled;

	private static final String CONFIG_NAME = "gameplay.yml";

	public ArenaGameplayManager(@NotNull ArenaConfig arenaConfig) {
		this.arenaConfig = arenaConfig;
		this.config = new JYML(arenaConfig.getFile().getParentFile().getAbsolutePath(), CONFIG_NAME);
	}
	
	@Override
	public void setup() {
		this.setTimeleft(config.getInt("Timeleft", 30));
		this.setLobbyTime(config.getInt("Lobby_Prepare_Time", 30));
		//this.setCoinsMultiplier(config.getDouble("coins-multiplier", 1D));
		this.setAnnouncesEnabled(config.getBoolean("Announces_Enabled", true));
		this.setScoreboardEnabled(config.getBoolean("Scoreboard_Enabled", true));
		this.setShopEnabled(config.getBoolean("Shop_Enabled", true));
		this.setHungerEnabled(config.getBoolean("Hunger_Enabled"));
		this.setRegenerationEnabled(config.getBoolean("Regeneration_Enabled", true));
		this.setItemDropEnabled(config.getBoolean("Item_Drop_Enabled"));
		this.setItemPickupEnabled(config.getBoolean("Item_Pickup_Enabled"));
		this.setItemDurabilityEnabled(config.getBoolean("Item_Durability_Enabled"));
		this.setSlimeSplitEnabled(config.getBoolean("Slime_Split_Enabled"));
		this.setMobDropExpEnabled(config.getBoolean("Mob_Drop_Exp_Enabled"));
		this.setMobDropLootEnabled(config.getBoolean("Mob_Drop_Items_Enabled"));
		this.bannedItems = new HashSet<>(config.getStringSet("Banned_Items").stream()
			.map(Material::getMaterial).filter(Objects::nonNull).toList());
		this.allowedSpawnReasons = new HashSet<>(config.getStringSet("Allowed_Spawn_Reasons")
			.stream().map(raw -> CollectionsUT.getEnum(raw, CreatureSpawnEvent.SpawnReason.class))
			.filter(Objects::nonNull).toList());

		this.autoCommands = new HashSet<>();
		for (String cmdId : config.getSection("Auto_Commands")) {
			String path2 = "Auto_Commands." + cmdId + ".";
			Set<IArenaGameEventTrigger> triggers = AbstractArenaGameEventTrigger.parse(arenaConfig, config, path2 + "Triggers");
			ArenaGameTargetType targetType = config.getEnum(path2 + "Target", ArenaGameTargetType.class, ArenaGameTargetType.GLOBAL);
			List<String> commands = config.getStringList(path2 + "Commands");

			IArenaGameCommand gameCommand = new ArenaGameCommand(arenaConfig, triggers, targetType, commands);
			this.getAutoCommands().add(gameCommand);
		}
		
		String path = "Players.";
		this.setPlayerMinAmount(config.getInt(path + "Minimum", 1));
		this.setPlayerMaxAmount(config.getInt(path + "Maximum", 10));
		this.setPlayerLivesAmount(config.getInt(path + "Lives", 1));
		this.setPlayerExpSavingEnabled(config.getBoolean(path + "Save_Exp"));
		this.setPlayerDropItemsOnDeathEnabled(config.getBoolean(path + "Drop_Items_On_Death"));
		
		path = "Spectate.";
		this.setSpectateEnabled(config.getBoolean(path + "Enabled", true));
		this.setSpectateOnDeathEnabled(config.getBoolean(path + "After_Death", true));
		
		path = "Commands.";
		this.setPlayerCommandsEnabled(config.getBoolean(path + "Allowed"));
		this.setPlayerCommandsAllowed(config.getStringSet(path + "Whitelist"));
		
		path = "Kits.";
		this.setKitsEnabled(config.getBoolean(path + "Enabled", true));
		this.setKitsAllowed(config.getStringSet(path + "Allowed"));
		if (this.isKitsEnabled() && this.getKitsAllowed().isEmpty()) {
			this.getKitsAllowed().add(Constants.MASK_ANY);
		}
		
		Map<String, Integer> kitLimitsMap = new HashMap<>();
		for (String sId : config.getSection(path + "Limits")) {
			kitLimitsMap.put(sId.toLowerCase(), config.getInt(path + "Limits." + sId));
		}
		this.setKitsLimits(kitLimitsMap);
		
		path = "Compatibility.";
		this.setExternalPetsEnabled(config.getBoolean(path + "Pets_Enabled"));
		this.setExternalMcmmoEnabled(config.getBoolean(path + "Mcmmo_Enabled"));
	}
	
	@Override
	public void shutdown() {
		if (this.editor != null) {
			this.editor.clear();
			this.editor = null;
		}
	}

	@Override
	public void onSave() {
		config.set("Timeleft", this.getTimeleft());
		config.set("Lobby_Prepare_Time", this.getLobbyTime());
		//config.set("coins-multiplier", this.getCoinsMultiplier());
		config.set("Announces_Enabled", this.isAnnouncesEnabled());
		config.set("Scoreboard_Enabled", this.isScoreboardEnabled());
		config.set("Shop_Enabled", this.isShopEnabled());
		config.set("Hunger_Enabled", this.isHungerEnabled());
		config.set("Regeneration_Enabled", this.isRegenerationEnabled());
		config.set("Item_Drop_Enabled", this.isItemDropEnabled());
		config.set("Item_Pickup_Enabled", this.isItemPickupEnabled());
		config.set("Item_Durability_Enabled", this.isItemDurabilityEnabled());
		config.set("Slime_Split_Enabled", this.isSlimeSplitEnabled());
		config.set("Mob_Drop_Exp_Enabled", this.isMobDropExpEnabled());
		config.set("Mob_Drop_Items_Enabled", this.isMobDropLootEnabled());
		config.set("Banned_Items", this.getBannedItems().stream().map(Material::name).toList());
		config.set("Allowed_Spawn_Reasons", this.getAllowedSpawnReasons().stream().map(Enum::name).toList());

		String path = "Players.";
		config.set(path + "Minimum", this.getPlayerMinAmount());
		config.set(path + "Maximum", this.getPlayerMaxAmount());
		config.set(path + "Lives", this.getPlayerLivesAmount());
		config.set(path + "Save_Exp", this.isPlayerExpSavingEnabled());
		config.set(path + "Drop_Items_On_Death", this.isPlayerDropItemsOnDeathEnabled());
		
		path = "Spectate.";
		config.set(path + "Enabled", this.isSpectateEnabled());
		config.set(path + "After_Death", this.isSpectateOnDeathEnabled());
		
		path = "Commands.";
		config.set(path + "Allowed", this.isPlayerCommandsEnabled());
		config.set(path + "Whitelist", this.getPlayerCommandsAllowed());

		config.set("Auto_Commands", null);
		this.getAutoCommands().forEach(gameCommand -> {
			String path2 = "Auto_Commands." + UUID.randomUUID() + ".";

			gameCommand.getTriggers().forEach(trigger -> {
				if (!(trigger instanceof AbstractArenaGameEventTrigger<?> trigger1)) return;
				trigger1.saveTo(config, path2 + "Triggers.");
			});
			config.set(path2 + "Target", gameCommand.getTargetType().name());
			config.set(path2 + "Commands", gameCommand.getCommands());
		});
		
		path = "Kits.";
		config.set(path + "Enabled", this.isKitsEnabled());
		config.set(path + "Allowed", new ArrayList<>(this.getKitsAllowed()));
		final String path2 = path;
		config.set(path2 + "Limits", null);
		this.getKitsLimits().forEach((id, limit) -> {
			config.set(path2 + "Limits." + id, limit);
		});
		
		path = "Compatibility.";
		config.set(path + "Pets_Enabled", this.isExternalPetsEnabled());
		config.set(path + "Mcmmo_Enabled", this.isExternalMcmmoEnabled());
	}

	@Override
	@NotNull
	public EditorArenaGameplay getEditor() {
		if (this.editor == null) {
			this.editor = new EditorArenaGameplay(this);
		}
		return this.editor;
	}

	@NotNull
	@Override
	public JYML getConfig() {
		return config;
	}

	@NotNull
	@Override
	public ArenaConfig getArenaConfig() {
		return this.arenaConfig;
	}
	
	@Override
	@NotNull
	public List<String> getProblems() {
		List<String> list = new ArrayList<>();
		if (this.isKitsEnabled() && this.getKitsAllowed().isEmpty()) {
			list.add("Kits are enabled, but no kits are allowed!");
		}
		return list;
	}

	@Override
	public int getTimeleft() {
		return this.timeleft;
	}

	@Override
	public void setTimeleft(int timeleft) {
		this.timeleft = timeleft;
	}

	@Override
	public int getLobbyTime() {
		return this.lobbyTime;
	}

	@Override
	public void setLobbyTime(int lobbyTime) {
		this.lobbyTime = Math.max(1, lobbyTime);
	}

	/*@Deprecated
	public double getCoinsMultiplier() {
		return this.coinsMultiplier;
	}

	@Deprecated
	public void setCoinsMultiplier(double coinsMultiplier) {
		this.coinsMultiplier = coinsMultiplier;
	}*/

	@Override
	public boolean isAnnouncesEnabled() {
		return this.isAnnouncesEnabled;
	}

	@Override
	public void setAnnouncesEnabled(boolean isAnnouncesEnabled) {
		this.isAnnouncesEnabled = isAnnouncesEnabled;
	}

	@Override
	public boolean isScoreboardEnabled() {
		return this.isScoreboardEnabled;
	}

	@Override
	public void setScoreboardEnabled(boolean isScoreboardEnabled) {
		this.isScoreboardEnabled = isScoreboardEnabled && Hooks.hasPlugin(EHook.PROTOCOL_LIB);
	}

	@Override
	public boolean isShopEnabled() {
		return this.isShopEnabled;
	}

	@Override
	public void setShopEnabled(boolean isShopEnabled) {
		this.isShopEnabled = isShopEnabled;
	}

	@Override
	public boolean isHungerEnabled() {
		return this.isHungerEnabled;
	}

	@Override
	public void setHungerEnabled(boolean isHungerEnabled) {
		this.isHungerEnabled = isHungerEnabled;
	}

	@Override
	public boolean isRegenerationEnabled() {
		return this.isRegenerationEnabled;
	}

	@Override
	public void setRegenerationEnabled(boolean isRegenerationEnabled) {
		this.isRegenerationEnabled = isRegenerationEnabled;
	}

	@Override
	public boolean isItemDropEnabled() {
		return this.isItemDropEnabled;
	}

	@Override
	public void setItemDropEnabled(boolean isItemDropEnabled) {
		this.isItemDropEnabled = isItemDropEnabled;
	}

	@Override
	public boolean isItemPickupEnabled() {
		return this.isItemPickupEnabled;
	}

	@Override
	public void setItemPickupEnabled(boolean isItemPickupEnabled) {
		this.isItemPickupEnabled = isItemPickupEnabled;
	}

	@Override
	public boolean isItemDurabilityEnabled() {
		return this.isItemDurabilityEnabled;
	}

	@Override
	public void setItemDurabilityEnabled(boolean isItemDurabilityEnabled) {
		this.isItemDurabilityEnabled = isItemDurabilityEnabled;
	}

	@NotNull
	@Override
	public Set<Material> getBannedItems() {
		return bannedItems;
	}

	@NotNull
	@Override
	public Set<CreatureSpawnEvent.SpawnReason> getAllowedSpawnReasons() {
		return allowedSpawnReasons;
	}

	@Override
	public boolean isSlimeSplitEnabled() {
		return this.isSlimeSplitEnabled;
	}

	@Override
	public void setSlimeSplitEnabled(boolean isSlimeSplitEnabled) {
		this.isSlimeSplitEnabled = isSlimeSplitEnabled;
	}

	@Override
	public boolean isMobDropExpEnabled() {
		return this.isMobDropExpEnabled;
	}

	@Override
	public void setMobDropExpEnabled(boolean isMobDropExpEnabled) {
		this.isMobDropExpEnabled = isMobDropExpEnabled;
	}

	@Override
	public boolean isMobDropLootEnabled() {
		return isMobDropLootEnabled;
	}

	@Override
	public void setMobDropLootEnabled(boolean mobDropLootEnabled) {
		isMobDropLootEnabled = mobDropLootEnabled;
	}

	@NotNull
	@Override
	public Set<IArenaGameCommand> getAutoCommands() {
		return autoCommands;
	}

	@Override
	public int getPlayerMinAmount() {
		return playerMinAmount;
	}

	@Override
	public void setPlayerMinAmount(int playerMinAmount) {
		this.playerMinAmount = playerMinAmount;
	}

	@Override
	public int getPlayerMaxAmount() {
		return playerMaxAmount;
	}

	@Override
	public void setPlayerMaxAmount(int playerMaxAmount) {
		this.playerMaxAmount = playerMaxAmount;
	}

	@Override
	public int getPlayerLivesAmount() {
		return playerLivesAmount;
	}

	@Override
	public void setPlayerLivesAmount(int playerLivesAmount) {
		this.playerLivesAmount = playerLivesAmount;
	}

	@Override
	public boolean isPlayerExpSavingEnabled() {
		return isPlayerExpSavingEnabled;
	}

	@Override
	public void setPlayerExpSavingEnabled(boolean playerExpSavingEnabled) {
		isPlayerExpSavingEnabled = playerExpSavingEnabled;
	}

	@Override
	public boolean isPlayerDropItemsOnDeathEnabled() {
		return isPlayerDropItemsOnDeathEnabled;
	}

	@Override
	public void setPlayerDropItemsOnDeathEnabled(boolean playerDropItemsOnDeathEnabled) {
		isPlayerDropItemsOnDeathEnabled = playerDropItemsOnDeathEnabled;
	}

	@Override
	public boolean isPlayerCommandsEnabled() {
		return isPlayerCommandsEnabled;
	}

	@Override
	public void setPlayerCommandsEnabled(boolean playerCommandsEnabled) {
		isPlayerCommandsEnabled = playerCommandsEnabled;
	}

	@NotNull
	@Override
	public Set<String> getPlayerCommandsAllowed() {
		return playerCommandsAllowed;
	}

	@Override
	public void setPlayerCommandsAllowed(@NotNull Set<String> playerCommandsAllowed) {
		this.playerCommandsAllowed = playerCommandsAllowed;
	}

	@Override
	public boolean isSpectateEnabled() {
		return this.isSpectateEnabled;
	}

	@Override
	public void setSpectateEnabled(boolean spectateEnabled) {
		isSpectateEnabled = spectateEnabled;
	}

	@Override
	public boolean isSpectateOnDeathEnabled() {
		return isSpectateOnDeathEnabled;
	}

	@Override
	public void setSpectateOnDeathEnabled(boolean spectateOnDeathEnabled) {
		isSpectateOnDeathEnabled = spectateOnDeathEnabled;
	}

	@Override
	public boolean isKitsEnabled() {
		return isKitsEnabled;
	}

	@Override
	public void setKitsEnabled(boolean kitsEnabled) {
		isKitsEnabled = kitsEnabled;
	}

	@NotNull
	@Override
	public Set<String> getKitsAllowed() {
		return kitsAllowed;
	}

	@Override
	public void setKitsAllowed(@NotNull Set<String> kitsAllowed) {
		this.kitsAllowed = kitsAllowed;
	}

	@NotNull
	@Override
	public Map<String, Integer> getKitsLimits() {
		return kitsLimits;
	}

	@Override
	public void setKitsLimits(@NotNull Map<String, Integer> kitsLimits) {
		this.kitsLimits = kitsLimits;
	}

	@Override
	public boolean isExternalPetsEnabled() {
		return isExternalPetsEnabled;
	}

	@Override
	public void setExternalPetsEnabled(boolean externalPetsEnabled) {
		isExternalPetsEnabled = externalPetsEnabled;
	}

	@Override
	public boolean isExternalMcmmoEnabled() {
		return isExternalMcmmoEnabled;
	}

	@Override
	public void setExternalMcmmoEnabled(boolean externalMcmmoEnabled) {
		isExternalMcmmoEnabled = externalMcmmoEnabled;
	}
}
