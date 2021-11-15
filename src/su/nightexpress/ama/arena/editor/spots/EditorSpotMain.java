package su.nightexpress.ama.arena.editor.spots;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.editor.EditorUtils;
import su.nexmedia.engine.api.menu.*;
import su.nexmedia.engine.utils.ItemUT;
import su.nightexpress.ama.AMA;
import su.nightexpress.ama.api.arena.spot.IArenaSpot;
import su.nightexpress.ama.editor.ArenaEditorHandler;
import su.nightexpress.ama.editor.ArenaEditorType;

public class EditorSpotMain extends AbstractMenu<AMA> {

	private final IArenaSpot spot;
	
	private EditorSpotStateList editorSpotStateList;
	
	public EditorSpotMain(@NotNull IArenaSpot spot) {
		super(spot.plugin(), ArenaEditorHandler.ARENA_SPOT_MAIN, "");
		this.spot = spot;
		
		IMenuClick click = (player, type, e) -> {
			if (type == null) return;

			if (type instanceof MenuItemType type2) {
				if (type2 == MenuItemType.RETURN) {
					spot.getArenaConfig().getSpotManager().getEditor().open(player, 1);
				}
			}
			else if (type instanceof ArenaEditorType type2) {
				switch (type2) {
					case SPOT_CHANGE_ACTIVE -> {
						spot.setActive(!spot.isActive());
						spot.save();
						this.open(player, 1);
					}
					case SPOT_CHANGE_NAME -> {
						plugin.getEditorHandlerNew().startEdit(player, spot, type2);
						EditorUtils.tipCustom(player, plugin.lang().Editor_Spot_Enter_Name.getMsg());
						player.closeInventory();
					}
					case SPOT_SETUP_KIT -> {
						player.closeInventory();
						plugin.getArenaSetupManager().getSpotSetupManager().startSetup(player, spot);
					}
					case SPOT_CHANGE_STATES -> {
						if (spot.getCuboid().isEmpty()) {
							plugin.lang().Editor_Spot_State_Error_NoCuboid.send(player);
							return;
						}
						this.openStates(player);
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
	
	@Override
	public void clear() {
		if (this.editorSpotStateList != null) {
			this.editorSpotStateList.clear();
			this.editorSpotStateList = null;
		}
		super.clear();
	}

	public void openStates(@NotNull Player player) {
		if (this.editorSpotStateList == null) {
			this.editorSpotStateList = new EditorSpotStateList(this.spot);
		}
		this.editorSpotStateList.open(player, 1);
	}

	@Override
	public void onPrepare(@NotNull Player player, @NotNull Inventory inventory) {

	}

	@Override
	public void onReady(@NotNull Player player, @NotNull Inventory inventory) {

	}

	@Override
	@Nullable
	public MenuItemDisplay onItemDisplayPrepare(@NotNull Player player, @NotNull IMenuItem menuItem) {
		if (menuItem.getType() instanceof ArenaEditorType type2) {
			if (type2 == ArenaEditorType.SPOT_CHANGE_ACTIVE) {
				return menuItem.getDisplay(String.valueOf(spot.isActive() ? (spot.hasProblems() ? 2 : 1) : 0));
			}
		}
		return super.onItemDisplayPrepare(player, menuItem);
	}

	@Override
	public void onItemPrepare(@NotNull Player player, @NotNull IMenuItem menuItem, @NotNull ItemStack item) {
		super.onItemPrepare(player, menuItem, item);
		ItemUT.replace(item, this.spot.replacePlaceholders());
	}

	@Override
	public boolean cancelClick(@NotNull SlotType slotType, int slot) {
		return true;
	}
}
