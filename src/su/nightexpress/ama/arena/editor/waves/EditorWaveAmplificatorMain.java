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
import su.nexmedia.engine.utils.CollectionsUT;
import su.nexmedia.engine.utils.ItemUT;
import su.nightexpress.ama.AMA;
import su.nightexpress.ama.api.arena.game.ArenaGameEventType;
import su.nightexpress.ama.api.arena.wave.IArenaWaveAmplificator;
import su.nightexpress.ama.editor.ArenaEditorHandler;
import su.nightexpress.ama.editor.ArenaEditorType;

public class EditorWaveAmplificatorMain extends AbstractMenu<AMA> {

	private final IArenaWaveAmplificator amplificator;
	
	public EditorWaveAmplificatorMain(@NotNull AMA plugin, @NotNull IArenaWaveAmplificator amplificator) {
		super(plugin, ArenaEditorHandler.YML_ARENA_WAVE_AMPLIFICATOR_SETTINGS, "");
		this.amplificator = amplificator;
		
		IMenuClick click = (player, type, e) -> {
			if (type == null) return;

			if (type instanceof MenuItemType type2) {
				if (type2 == MenuItemType.RETURN) {
					if (!(amplificator.getArenaWave().getEditor() instanceof EditorWaveSettings waveMain)) return;
					waveMain.getEditorAmplificators().open(player, 1);
				}
			}
			else if (type instanceof ArenaEditorType type2) {
				switch (type2) {
					case WAVES_CHANGE_WAVE_AMPLIFICATOR_VALUES -> {
						if (e.isLeftClick()) type2 = ArenaEditorType.WAVES_CHANGE_WAVE_AMPLIFICATOR_VALUE_AMOUNT;
						else if (e.isRightClick()) type2 = ArenaEditorType.WAVES_CHANGE_WAVE_AMPLIFICATOR_VALUE_LEVEL;

						plugin.getEditorHandlerNew().startEdit(player, amplificator, type2);
						EditorUtils.tipCustom(player, plugin.lang().Editor_Arena_Waves_Enter_Amplificator_Value.getMsg());
						player.closeInventory();
					}
					case WAVES_CHANGE_WAVE_AMPLIFICATOR_TRIGGERS -> {
						if (e.isRightClick()) {
							amplificator.getTriggers().clear();
							amplificator.getArenaWave().getArenaConfig().getWaveManager().save();
							this.open(player, 1);
							break;
						}
						plugin.getEditorHandlerNew().startEdit(player, amplificator, ArenaEditorType.WAVES_CHANGE_WAVE_AMPLIFICATOR_TRIGGERS_ADD);
						EditorUtils.tipCustom(player, plugin.lang().Editor_Enter_Triggers.getMsg());
						plugin.lang().Editor_Tip_Triggers.send(player);
						EditorUtils.sendSuggestTips(player, CollectionsUT.getEnumsList(ArenaGameEventType.class));
						player.closeInventory();
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
	public void onPrepare(@NotNull Player player, @NotNull Inventory inventory) {

	}

	@Override
	public void onReady(@NotNull Player player, @NotNull Inventory inventory) {

	}

	@Override
	public void onItemPrepare(@NotNull Player player, @NotNull IMenuItem menuItem, @NotNull ItemStack item) {
		super.onItemPrepare(player, menuItem, item);
		ItemUT.replace(item, this.amplificator.replacePlaceholders());
	}

	@Override
	public boolean cancelClick(@NotNull SlotType slotType, int slot) {
		return true;
	}
}
