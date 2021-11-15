package su.nightexpress.ama.arena.editor.regions;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.editor.EditorUtils;
import su.nexmedia.engine.api.menu.AbstractMenu;
import su.nexmedia.engine.api.menu.IMenuClick;
import su.nexmedia.engine.api.menu.IMenuItem;
import su.nexmedia.engine.api.menu.MenuItemType;
import su.nexmedia.engine.utils.ItemUT;
import su.nightexpress.ama.AMA;
import su.nightexpress.ama.api.arena.game.ArenaGameEventType;
import su.nightexpress.ama.api.arena.region.IArenaRegion;
import su.nightexpress.ama.api.arena.region.IArenaRegionWave;
import su.nightexpress.ama.arena.region.ArenaRegion;
import su.nightexpress.ama.editor.ArenaEditorHandler;
import su.nightexpress.ama.editor.ArenaEditorType;

import java.util.Collections;

public class EditorRegionWaveSettings extends AbstractMenu<AMA> {

	private final IArenaRegionWave regionWave;
	
	public EditorRegionWaveSettings(@NotNull IArenaRegionWave regionWave) {
		super(regionWave.plugin(), ArenaEditorHandler.YML_ARENA_REGION_WAVE_SETTINGS, "");
		this.regionWave = regionWave;
		
		IMenuClick click = (player, type, e) -> {
			if (type == null) return;

			IArenaRegion region = this.regionWave.getRegion();
			if (type instanceof MenuItemType type2) {
				if (type2 == MenuItemType.RETURN) {
					if (!(region instanceof ArenaRegion region1)) return;
					
					region1.getEditor().getWaveList().open(player, 1);
				}
				return;
			}
			
			if (type instanceof ArenaEditorType type2) {
				switch (type2) {
					case REGION_WAVE_CHANGE_TRIGGERS -> {
						if (e.isRightClick()) {
							regionWave.getTriggers().clear();
							break;
						}
						plugin.getEditorHandlerNew().startEdit(player, this.regionWave, ArenaEditorType.REGION_WAVE_CHANGE_TRIGGERS_ADD);
						EditorUtils.tipCustom(player, plugin.lang().Editor_Enter_Triggers.getMsg());
						plugin.lang().Editor_Tip_Triggers.send(player);
						EditorUtils.sendSuggestTips(player, Collections.singletonList(ArenaGameEventType.WAVE_START.name()));
						//EditorUtils.sendSuggestTips(player, CollectionsUT.getEnumsList(ArenaGameEventType.class));
						player.closeInventory();
						return;
					}
					case REGION_WAVE_CHANGE_ID -> {
						if (e.isRightClick()) {
							regionWave.getArenaWaveIds().clear();
							break;
						}
						plugin.getEditorHandlerNew().startEdit(player, this.regionWave, type2);
						EditorUtils.tipCustom(player, plugin.lang().Editor_Region_Wave_Enter_Id.getMsg());
						EditorUtils.sendClickableTips(player, region.getArenaConfig().getWaveManager().getWaves().keySet());
						player.closeInventory();
						return;
					}
					case REGION_WAVE_CHANGE_SPAWNERS -> {
						if (e.isRightClick()) {
							this.regionWave.getSpawnerIds().clear();
							break;
						}

						plugin.getEditorHandlerNew().startEdit(player, this.regionWave, ArenaEditorType.REGION_WAVE_CHANGE_SPAWNERS_ADD);
						EditorUtils.tipCustom(player, plugin.lang().Editor_Region_Wave_Enter_SpawnerId.getMsg());
						EditorUtils.sendClickableTips(player, region.getMobSpawners().keySet());
						player.closeInventory();
						return;
					}
					default -> {return;}
				}
				
				region.save();
				this.open(player, 1);
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
	public void onPrepare(@NotNull Player player, @NotNull Inventory inventory) {

	}

	@Override
	public void onReady(@NotNull Player player, @NotNull Inventory inventory) {

	}

	@Override
	public void onItemPrepare(@NotNull Player player, @NotNull IMenuItem menuItem, @NotNull ItemStack item) {
		super.onItemPrepare(player, menuItem, item);
		ItemUT.replace(item, this.regionWave.replacePlaceholders());
	}

	@Override
	public boolean cancelClick(@NotNull SlotType slotType, int slot) {
		return true;
	}
}
