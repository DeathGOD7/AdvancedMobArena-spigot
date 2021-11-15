package su.nightexpress.ama.arena.editor.regions;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.editor.EditorUtils;
import su.nexmedia.engine.api.menu.*;
import su.nexmedia.engine.utils.CollectionsUT;
import su.nexmedia.engine.utils.ItemUT;
import su.nightexpress.ama.AMA;
import su.nightexpress.ama.api.arena.game.ArenaGameEventType;
import su.nightexpress.ama.api.arena.region.IArenaRegion;
import su.nightexpress.ama.api.arena.type.ArenaLockState;
import su.nightexpress.ama.arena.region.ArenaRegion;
import su.nightexpress.ama.editor.ArenaEditorHandler;
import su.nightexpress.ama.editor.ArenaEditorType;

public class EditorRegionMain extends AbstractMenu<AMA> {

	private final ArenaRegion region;
	
	private EditorRegionWaveList waveList;
	private EditorRegionContainerList containerList;
	
	public EditorRegionMain(@NotNull ArenaRegion region) {
		super(region.plugin(), ArenaEditorHandler.YML_ARENA_REGION_MAIN, "");
		this.region = region;
		
		IMenuClick click = (player, type, e) -> {
			if (type == null) return;

			if (type instanceof MenuItemType type2) {
				if (type2 == MenuItemType.RETURN) {
					region.getArenaConfig().getRegionManager().getEditor().open(player, 1);
				}
			}
			else if (type instanceof ArenaEditorType type2) {
				switch (type2) {
					case REGION_CHANGE_ACTIVE -> {
						region.setActive(!region.isActive());
						region.save();
						this.open(player, 1);
					}
					case REGION_CHANGE_DEFAULT -> {
						IArenaRegion def = region.getArenaConfig().getRegionManager().getRegionDefault();
						if (def != null && !this.region.equals(def)) return;

						region.setDefault(!region.isDefault());
						region.getArenaConfig().save();
						this.open(player, 1);
					}
					case REGION_CHANGE_NAME -> {
						plugin.getEditorHandlerNew().startEdit(player, region, type2);
						EditorUtils.tipCustom(player, plugin.lang().Editor_Region_Enter_Name.getMsg());
						player.closeInventory();
					}
					case REGION_CHANGE_TRIGGERS_LOCKED, REGION_CHANGE_TRIGGERS_UNLOCKED -> {
						if (e.isRightClick()) {
							ArenaLockState state = type2 == ArenaEditorType.REGION_CHANGE_TRIGGERS_LOCKED ? ArenaLockState.LOCKED : ArenaLockState.UNLOCKED;
							region.getStateTriggers(state).clear();
							region.save();
							this.open(player, 1);
							break;
						}
						plugin.getEditorHandlerNew().startEdit(player, region, type2);
						EditorUtils.tipCustom(player, plugin.lang().Editor_Enter_Triggers.getMsg());
						plugin.lang().Editor_Tip_Triggers.send(player);
						EditorUtils.sendSuggestTips(player, CollectionsUT.getEnumsList(ArenaGameEventType.class));
						player.closeInventory();
					}
					case REGION_SETUP_KIT -> {
						if (region.isActive()) {
							plugin.lang().Setup_Region_Error_Enabled.send(player);
							return;
						}
						player.closeInventory();
						plugin.getArenaSetupManager().getRegionSetupManager().startSetup(player, region);
					}
					case REGION_OPEN_WAVES -> this.getWaveList().open(player, 1);
					case REGION_OPEN_CONTAINERS -> this.getContainerList().open(player, 1);
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
		if (this.waveList != null) {
			this.waveList.clear();
			this.waveList = null;
		}
		if (this.containerList != null) {
			this.containerList.clear();
			this.containerList = null;
		}
		super.clear();
	}
	
	@NotNull
	public EditorRegionWaveList getWaveList() {
		if (this.waveList == null) {
			this.waveList = new EditorRegionWaveList(this.region);
		}
		return this.waveList;
	}

	@NotNull
	public EditorRegionContainerList getContainerList() {
		if (this.containerList == null) {
			this.containerList = new EditorRegionContainerList(this.region);
		}
		return containerList;
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
		Enum<?> type = menuItem.getType();
		if (type instanceof ArenaEditorType type2) {
			if (type2 == ArenaEditorType.REGION_CHANGE_ACTIVE) {
				int tune = region.isActive() ? (!region.hasProblems() ? EditorUtils.TUNE_ENABLED : EditorUtils.TUNE_WARNING) : EditorUtils.TUNE_DISABLED;
				return menuItem.getDisplay(String.valueOf(tune));
			}
			if (type2 == ArenaEditorType.REGION_CHANGE_DEFAULT) {
				return menuItem.getDisplay(String.valueOf(region.isDefault() ? 1 : 0));
			}
		}
		return super.onItemDisplayPrepare(player, menuItem);
	}

	@Override
	public void onItemPrepare(@NotNull Player player, @NotNull IMenuItem menuItem, @NotNull ItemStack item) {
		super.onItemPrepare(player, menuItem, item);
		ItemUT.replace(item, this.region.replacePlaceholders());
	}

	@Override
	public boolean cancelClick(@NotNull SlotType slotType, int slot) {
		return true;
	}
}
