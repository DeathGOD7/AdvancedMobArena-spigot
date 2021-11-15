package su.nightexpress.ama.arena.reward;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.utils.StringUT;
import su.nightexpress.ama.api.arena.config.IArenaConfig;
import su.nightexpress.ama.api.arena.game.ArenaGameTargetType;
import su.nightexpress.ama.api.arena.game.IArenaGameEventTrigger;
import su.nightexpress.ama.api.arena.reward.IArenaReward;
import su.nightexpress.ama.arena.editor.reward.EditorRewardSettings;

import java.util.List;
import java.util.Set;

public class ArenaReward implements IArenaReward {

	private final IArenaConfig arenaConfig;

	private       String                      name;
	private       boolean                     isLate;
	private final Set<IArenaGameEventTrigger> triggers;
	private       ArenaGameTargetType         targetType;
	private       double                      chance;
	private       List<String>                commands;
	private       List<ItemStack>             items;

	private EditorRewardSettings editor;
	
	public ArenaReward(
			@NotNull IArenaConfig arenaConfig,

			@NotNull String name,
			boolean isLate,
			@NotNull Set<IArenaGameEventTrigger> triggers,
			@NotNull ArenaGameTargetType targetType,
			double chance, 
			@NotNull List<String> commands,
			@NotNull List<ItemStack> items
			) {
		this.arenaConfig = arenaConfig;

		this.setName(name);
		this.setLate(isLate);
		this.triggers = triggers;
		this.setTargetType(targetType);
		this.setChance(chance);
		this.setCommands(commands);
		this.setItems(items);
	}

	@Override
	public void clear() {
		if (this.editor != null) {
			this.editor.clear();
			this.editor = null;
		}
	}

	@NotNull
	@Override
	public EditorRewardSettings getEditor() {
		if (this.editor == null) {
			this.editor = new EditorRewardSettings(this);
		}
		return editor;
	}

	@NotNull
	@Override
	public IArenaConfig getArenaConfig() {
		return arenaConfig;
	}

	@NotNull
	public String getName() {
		return this.name;
	}
	
	public void setName(@NotNull String name) {
		this.name = StringUT.color(name);
	}
	
	public boolean isLate() {
		return isLate;
	}
	
	public void setLate(boolean late) {
		this.isLate = late;
	}

	@NotNull
	@Override
	public Set<IArenaGameEventTrigger> getTriggers() {
		return triggers;
	}

	@NotNull
	@Override
	public ArenaGameTargetType getTargetType() {
		return targetType;
	}

	public void setTargetType(@NotNull ArenaGameTargetType targetType) {
		this.targetType = targetType;
	}

	public double getChance() {
		return this.chance;
	}
	
	public void setChance(double chance) {
		this.chance = chance;
	}
	
	@NotNull
	public List<String> getCommands() {
		return this.commands;
	}
	
	public void setCommands(@NotNull List<String> commands) {
		this.commands = commands;
	}
	
	@NotNull
	public List<ItemStack> getItems() {
		return this.items;
	}
	
	public void setItems(@NotNull List<ItemStack> items) {
		this.items = items;
	}
}