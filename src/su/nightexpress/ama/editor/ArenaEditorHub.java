package su.nightexpress.ama.editor;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.menu.AbstractMenu;
import su.nexmedia.engine.api.menu.IMenuClick;
import su.nexmedia.engine.api.menu.IMenuItem;
import su.nexmedia.engine.api.menu.MenuItemType;
import su.nightexpress.ama.AMA;
import su.nightexpress.ama.arena.editor.arena.EditorArenaList;
import su.nightexpress.ama.kits.editor.EditorKitList;

public class ArenaEditorHub extends AbstractMenu<AMA> {

	private EditorArenaList arenaEditor;
	private EditorKitList   kitEditor;
	
	public ArenaEditorHub(@NotNull AMA plugin) {
		super(plugin, ArenaEditorHandler.YML_HUB, "");
		
		IMenuClick click = (player, type, e) -> {
			if (type == null) return;

			if (type instanceof MenuItemType type2) {
				if (type2 == MenuItemType.CLOSE) {
					player.closeInventory();
				}
				return;
			}
			
			if (type instanceof ArenaEditorType type2) {
				switch (type2) {
					case EDITOR_ARENA -> this.getArenaEditor().open(player, 1);
					case EDITOR_KITS -> this.getKitEditor().open(player, 1);
					case EDITOR_MOBS -> {
						// TODO
					}
					default -> {}
				}
			}
		};
		
		for (String sId : cfg.getSection("Content")) {
			IMenuItem menuItem = cfg.getMenuItem("Content." + sId, MenuItemType.class);
			
			if (menuItem.getType() != null) {
				menuItem.setClick(click);
			}
			this.addItem(menuItem);
		}
		
		for (String sId : cfg.getSection("Editor")) {
			IMenuItem menuItem = cfg.getMenuItem("Editor." + sId, ArenaEditorType.class);
			
			if (menuItem.getType() != null) {
				menuItem.setClick(click);
			}
			this.addItem(menuItem);
		}
	}

	@NotNull
	public EditorArenaList getArenaEditor() {
		if (this.arenaEditor == null) {
			this.arenaEditor = new EditorArenaList(this.plugin);
		}
		return this.arenaEditor;
	}
	
	@NotNull
	public EditorKitList getKitEditor() {
		if (this.kitEditor == null) {
			this.kitEditor = new EditorKitList(plugin.getKitManager(), ArenaEditorHandler.KIT_LIST);
		}
		return this.kitEditor;
	}
	
	@Override
	public void clear() {
		if (this.arenaEditor != null) {
			this.arenaEditor.clear();
			this.arenaEditor = null;
		}
		if (this.kitEditor != null) {
			this.kitEditor.clear();
			this.kitEditor = null;
		}
		super.clear();
	}

	@Override
	public void onPrepare(@NotNull Player player, @NotNull Inventory inventory) {

	}

	@Override
	public void onReady(@NotNull Player player, @NotNull Inventory inventory) {

	}

	@Override
	public boolean cancelClick(@NotNull SlotType slotType, int slot) {
		return true;
	}
}
