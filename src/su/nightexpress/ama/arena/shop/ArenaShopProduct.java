package su.nightexpress.ama.arena.shop;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.utils.ItemUT;
import su.nightexpress.ama.api.arena.game.IArenaGameEventTrigger;
import su.nightexpress.ama.api.arena.shop.IArenaShopManager;
import su.nightexpress.ama.api.arena.shop.IArenaShopProduct;
import su.nightexpress.ama.api.arena.type.ArenaLockState;
import su.nightexpress.ama.arena.editor.shop.EditorShopProductSettings;

import java.util.*;

public class ArenaShopProduct implements IArenaShopProduct {

	private final IArenaShopManager shopManager;
	private final String            id;
	private       ArenaLockState    state;

	private       double                                           price;
	private final Map<ArenaLockState, Set<IArenaGameEventTrigger>> triggers;
	private       Set<String>                                      applicableKits;
	private       ItemStack                                        preview;
	private       List<String>                                     commands;
	private       List<ItemStack>                                  items;
	
	private EditorShopProductSettings editor;
	
	public ArenaShopProduct(
			@NotNull IArenaShopManager shopManager,
			@NotNull String id,
			double price,
			@NotNull Map<ArenaLockState, Set<IArenaGameEventTrigger>> triggers,
			@NotNull Set<String> applicableKits,
			@NotNull ItemStack preview,
			@NotNull List<String> commands,
			@NotNull List<ItemStack> items
			) {
		this.shopManager = shopManager;
		this.id = id.toLowerCase();
		this.setState(ArenaLockState.LOCKED);

		this.triggers = triggers;
		this.setPrice(price);
		this.setApplicableKits(applicableKits);
		this.setPreview(preview);
		this.setCommands(commands);
		this.setItems(items);
	}
	
	@Override
	@NotNull
	public EditorShopProductSettings getEditor() {
		if (this.editor == null) {
			this.editor = new EditorShopProductSettings(this);
		}
		return this.editor;
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
	public IArenaShopManager getShopManager() {
		return shopManager;
	}

	@Override
	@NotNull
	public String getId() {
		return this.id;
	}

	@Override
	@NotNull
	public ArenaLockState getState() {
		return state;
	}

	@Override
	public void setState(@NotNull ArenaLockState state) {
		this.state = state;
	}
	
	@Override
	public double getPrice() {
		return this.price;
	}
	
	@Override
	public void setPrice(double price) {
		this.price = price;
	}

	@Override
	@NotNull
	public Map<ArenaLockState, Set<IArenaGameEventTrigger>> getStateTriggers() {
		return this.triggers;
	}

	/**
	 * Returns triggers for the OPPOSITE lock state of the shop item.
	 * @return Set of GameEvent triggers.
	 */
	@NotNull
	@Override
	public Set<IArenaGameEventTrigger> getTriggers() {
		return this.getStateTriggers(this.getState().getOpposite());
	}
	
	@Override
	@NotNull
	public Set<String> getApplicableKits() {
		return this.applicableKits;
	}
	
	@Override
	public void setApplicableKits(@NotNull Set<String> applicableKits) {
		this.applicableKits = new HashSet<>(applicableKits.stream().map(String::toLowerCase).toList());
	}
	
	@Override
	@NotNull
	public ItemStack getPreview() {
		return new ItemStack(preview);
	}
	
	@Override
	public void setPreview(@NotNull ItemStack preview) {
		this.preview = new ItemStack(preview);
	}
	
	@Override
	@NotNull
	public List<String> getCommands() {
		return this.commands;
	}
	
	@Override
	public void setCommands(@NotNull List<String> commands) {
		this.commands = commands;
	}
	
	@Override
	@NotNull
	public List<ItemStack> getItems() {
		return items;
	}
	
	@Override
	public void setItems(@NotNull List<ItemStack> items) {
		this.items = new ArrayList<>(items);
		this.items.removeIf(item -> item == null || ItemUT.isAir(item));
	}
}
