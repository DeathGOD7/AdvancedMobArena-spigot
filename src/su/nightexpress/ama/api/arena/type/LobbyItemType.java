package su.nightexpress.ama.api.arena.type;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.utils.CollectionsUT;
import su.nexmedia.engine.utils.DataUT;
import su.nightexpress.ama.api.arena.IArena;
import su.nightexpress.ama.arena.ArenaManager;
import su.nightexpress.ama.arena.ArenaPlayer;

import java.util.function.BiFunction;

public enum LobbyItemType {

	KIT_SELECT((arena, arenaPlayer) -> {
		arena.plugin().getKitManager().getSelectMenu().open(arenaPlayer.getPlayer(), 1);
		return null;
	}),
	KIT_SHOP((arena, arenaPlayer) -> {
		arena.plugin().getKitManager().getShopMenu().open(arenaPlayer.getPlayer(), 1);
		return null;
	}),
	EXIT((arena, arenaPlayer) -> {
		arena.plugin().getArenaManager().leaveArena(arenaPlayer, LeaveReason.SELF);
		return null;
	}),
	STATS((arena, arenaPlayer) -> {
		arena.plugin().getStatsManager().getMenuStats().open(arenaPlayer.getPlayer(), 1);
		return null;
	}),
	READY(((arena, arenaPlayer) -> {
		arenaPlayer.setReady(!arenaPlayer.isReady());
		return null;
	}))
	;

	private boolean isEnabled;
	private ItemStack                             item;
	private int                                   slot;
	private final BiFunction<IArena, ArenaPlayer, Void> usage;

	LobbyItemType(@NotNull BiFunction<IArena, ArenaPlayer, Void> usage) {
		this.usage = usage;
	}

	public boolean isEnabled() {
		return isEnabled;
	}

	public void setEnabled(boolean isEnabled) {
		this.isEnabled = isEnabled;
	}

	public void setItem(@NotNull ItemStack item) {
		this.item = new ItemStack(item);
		DataUT.setData(this.item, ArenaManager.KEY_LOBBY_ITEM, this.name());
	}
	
	public void setSlot(int slot) {
		this.slot = slot;
	}
	
	public void giveItem(@NotNull Player player) {
		if (!this.isEnabled() || this.item == null) return;
		
		player.getInventory().setItem(this.slot, this.item);
	}

	public void use(@NotNull ArenaPlayer arenaPlayer) {
		this.usage.apply(arenaPlayer.getArena(), arenaPlayer);
	}

	@Nullable
	public static LobbyItemType getType(@NotNull ItemStack item) {
		String raw = DataUT.getStringData(item, ArenaManager.KEY_LOBBY_ITEM);
		return raw == null ? null : CollectionsUT.getEnum(raw, LobbyItemType.class);
	}
}
