package su.nightexpress.ama.arena.region;

import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.utils.ItemUT;
import su.nightexpress.ama.api.arena.game.IArenaGameEventTrigger;
import su.nightexpress.ama.api.arena.region.IArenaRegion;
import su.nightexpress.ama.api.arena.region.IArenaRegionContainer;
import su.nightexpress.ama.arena.editor.regions.EditorRegionContainerSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ArenaRegionContainer implements IArenaRegionContainer {

	private final IArenaRegion region;
	private final Chest                       chest;
	private final Set<IArenaGameEventTrigger> triggers;

	private int             minItems;
	private int             maxItems;
	private List<ItemStack> items;

	private EditorRegionContainerSettings editor;

	public ArenaRegionContainer(@NotNull IArenaRegion region, @NotNull Chest chest,
								@NotNull Set<IArenaGameEventTrigger> triggers,
								int minItems, int maxItems, @NotNull List<ItemStack> items) {
		this.region = region;
		this.chest = chest;
		this.triggers = triggers;

		this.setMinItems(minItems);
		this.setMaxItems(maxItems);
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
	public EditorRegionContainerSettings getEditor() {
		if (this.editor == null) {
			this.editor = new EditorRegionContainerSettings(this);
		}
		return editor;
	}

	@NotNull
	@Override
	public IArenaRegion getRegion() {
		return region;
	}

	@Override
	@NotNull
	public Chest getChest() {
		return this.chest;
	}

	@NotNull
	@Override
	public Set<IArenaGameEventTrigger> getTriggers() {
		return triggers;
	}

	@Override
	public int getMinItems() {
		return this.minItems;
	}

	@Override
	public void setMinItems(int minItems) {
		this.minItems = minItems;
	}

	@Override
	public int getMaxItems() {
		return this.maxItems;
	}

	@Override
	public void setMaxItems(int maxItems) {
		this.maxItems = maxItems;
	}

	@Override
	@NotNull
	public List<ItemStack> getItems() {
		return this.items;
	}
	
	@Override
	public void setItems(@NotNull List<ItemStack> items) {
		this.items = new ArrayList<>(items);
		this.items.removeIf(item -> item == null || ItemUT.isAir(item));
	}
}
