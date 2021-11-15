package su.nightexpress.ama.arena.reward;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.config.api.JYML;
import su.nightexpress.ama.api.arena.game.ArenaGameTargetType;
import su.nightexpress.ama.api.arena.game.IArenaGameEventTrigger;
import su.nightexpress.ama.api.arena.reward.IArenaReward;
import su.nightexpress.ama.api.arena.reward.IArenaRewardManager;
import su.nightexpress.ama.arena.config.ArenaConfig;
import su.nightexpress.ama.arena.editor.reward.EditorRewardList;
import su.nightexpress.ama.arena.game.trigger.AbstractArenaGameEventTrigger;

import java.util.*;

public class ArenaRewardManager implements IArenaRewardManager {

	private final ArenaConfig      arenaConfig;
	private final JYML             config;
	private       EditorRewardList editor;
	
	private boolean           isRetainOnLeave;
	private boolean           isRetainOnDeath;
	private Set<IArenaReward> rewards;

	private static final String CONFIG_NAME = "rewards.yml";

	public ArenaRewardManager(@NotNull ArenaConfig arenaConfig) {
		this.arenaConfig = arenaConfig;
		this.config = new JYML(this.arenaConfig.getFile().getParentFile().getAbsolutePath(), CONFIG_NAME);
	}

	@Override
	@Deprecated
	public void setup() {
		this.isRetainOnLeave = config.getBoolean("Retain_On_Leave");
		this.isRetainOnDeath = config.getBoolean("Retain_On_Death");
		this.rewards = new HashSet<>();
		for (String sId : config.getSection("List")) {
			String path2 = "List." + sId + ".";

			String name = config.getString(path2 + "Name", sId);
			boolean isLate = config.getBoolean(path2 + "Late");
			double chance = config.getDouble(path2 + "Chance");
			Set<IArenaGameEventTrigger> triggers = AbstractArenaGameEventTrigger.parse(arenaConfig, config, path2 + "Triggers");
			ArenaGameTargetType targetType = config.getEnum(path2 + "Target", ArenaGameTargetType.class, ArenaGameTargetType.PLAYER_ALL);
			List<String> commands = config.getStringList(path2 + "Commands");
			List<ItemStack> items = new ArrayList<>(Arrays.asList(config.getItemList64(path2 + "Items")));

			ArenaReward reward = new ArenaReward(arenaConfig, name, isLate, triggers, targetType, chance, commands, items);
			this.getRewards().add(reward);
		}
	}

	@Override
	public void shutdown() {
		if (this.editor != null) {
			this.editor.clear();
			this.editor = null;
		}
		if (this.rewards != null) {
			this.rewards.clear();
			this.rewards = null;
		}
	}

	@NotNull
	@Override
	public EditorRewardList getEditor() {
		if (this.editor == null) {
			this.editor = new EditorRewardList(this);
		}
		return editor;
	}

	@NotNull
	@Override
	public JYML getConfig() {
		return config;
	}

	@Override
	@NotNull
	public List<String> getProblems() {
		return new ArrayList<>();
	}

	@Override
	public void onSave() {
		config.set("Retain_On_Death", this.isRetainOnDeath());
		config.set("Retain_On_Leave", this.isRetainOnLeave());
		config.set("List", null);
		this.getRewards().forEach(reward -> {
			String path2 = "List." + UUID.randomUUID() + ".";
			reward.getTriggers().forEach(trigger -> {
				if (!(trigger instanceof AbstractArenaGameEventTrigger<?> trigger1)) return;
				trigger1.saveTo(config, path2 + "Triggers.");
			});
			config.set(path2 + "Name", reward.getName());
			config.set(path2 + "Late", reward.isLate());
			config.set(path2 + "Chance", reward.getChance());
			config.set(path2 + "Target", reward.getTargetType().name());
			config.set(path2 + "Commands", reward.getCommands());
			config.setItemList64(path2 + "Items", reward.getItems());
		});
	}

	@NotNull
	@Override
	public ArenaConfig getArenaConfig() {
		return arenaConfig;
	}

	public boolean isRetainOnDeath() {
		return isRetainOnDeath;
	}
	
	public boolean isRetainOnLeave() {
		return isRetainOnLeave;
	}
	
	public void setRetainOnDeath(boolean isRetainOnDeath) {
		this.isRetainOnDeath = isRetainOnDeath;
	}
	
	public void setRetainOnLeave(boolean retainOnLeave) {
		this.isRetainOnLeave = retainOnLeave;
	}
	
	@NotNull
	public Set<IArenaReward> getRewards() {
		return rewards;
	}

	public void setRewards(@NotNull Set<IArenaReward> rewards) {
		this.rewards = rewards;
	}
}
