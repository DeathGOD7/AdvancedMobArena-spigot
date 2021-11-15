package su.nightexpress.ama.arena.editor.waves;

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
import su.nightexpress.ama.api.arena.wave.IArenaWaveManager;
import su.nightexpress.ama.editor.ArenaEditorHandler;
import su.nightexpress.ama.editor.ArenaEditorType;

public class EditorWaveManager extends AbstractMenu<AMA> {

	private final IArenaWaveManager waveManager;

	private EditorWaveList    editorWaveList;
	private EditorWaveGradual editorGradual;
	
	public EditorWaveManager(@NotNull IArenaWaveManager waveManager) {
		super(waveManager.plugin(), ArenaEditorHandler.YML_ARENA_WAVE_MANAGER, "");
		this.waveManager = waveManager;
		
		IMenuClick click = (player, type, e) -> {
			if (type == null) return;

			if (type instanceof MenuItemType type2) {
				if (type2 == MenuItemType.RETURN) {
					this.waveManager.getArenaConfig().getEditor().open(player, 1);
				}
			}
			else if (type instanceof ArenaEditorType type2) {
				switch (type2) {
					case WAVES_CHANGE_DELAY -> {
						if (e.isLeftClick()) {
							plugin.getEditorHandlerNew().startEdit(player, waveManager, ArenaEditorType.WAVES_CHANGE_DELAY_FIRST);
							EditorUtils.tipCustom(player, plugin.lang().Editor_Arena_Waves_Enter_Delay_First.getMsg());
						}
						else if (e.isRightClick()) {
							plugin.getEditorHandlerNew().startEdit(player, waveManager, ArenaEditorType.WAVES_CHANGE_DELAY_DEFAULT);
							EditorUtils.tipCustom(player, plugin.lang().Editor_Arena_Waves_Enter_Delay_Default.getMsg());
						}
						player.closeInventory();
					}
					case WAVES_CHANGE_FINAL_WAVE -> {
						if (e.isRightClick()) {
							waveManager.setFinalWave(-1);
							this.waveManager.save();
							this.open(player, 1);
							break;
						}
						plugin.getEditorHandlerNew().startEdit(player, waveManager, type2);
						EditorUtils.tipCustom(player, plugin.lang().Editor_Arena_Waves_Enter_FinalWave.getMsg());
						player.closeInventory();
					}
					case WAVES_CHANGE_GRADUAL -> this.getEditorGradual().open(player, 1);
					case WAVES_CHANGE_WAVES -> this.getEditorWaveList().open(player, 1);
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
		if (this.editorWaveList != null) {
			this.editorWaveList.clear();
			this.editorWaveList = null;
		}
		if (this.editorGradual != null) {
			this.editorGradual.clear();
			this.editorGradual = null;
		}
		super.clear();
	}

	@NotNull
	public EditorWaveList getEditorWaveList() {
		if (this.editorWaveList == null) {
			this.editorWaveList = new EditorWaveList(this.waveManager);
		}
		return this.editorWaveList;
	}

	@NotNull
	public EditorWaveGradual getEditorGradual() {
		if (this.editorGradual == null) {
			this.editorGradual = new EditorWaveGradual(this.waveManager);
		}
		return this.editorGradual;
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

	@Override
	public void onItemPrepare(@NotNull Player player, @NotNull IMenuItem menuItem, @NotNull ItemStack item) {
		super.onItemPrepare(player, menuItem, item);
		ItemUT.replace(item, this.waveManager.replacePlaceholders());
	}
}
