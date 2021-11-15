package su.nightexpress.ama.arena.editor.waves;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.editor.EditorUtils;
import su.nexmedia.engine.api.menu.AbstractMenuAuto;
import su.nexmedia.engine.api.menu.IMenuClick;
import su.nexmedia.engine.api.menu.IMenuItem;
import su.nexmedia.engine.api.menu.MenuItemType;
import su.nexmedia.engine.utils.ItemUT;
import su.nexmedia.engine.utils.StringUT;
import su.nightexpress.ama.AMA;
import su.nightexpress.ama.api.arena.wave.IArenaWave;
import su.nightexpress.ama.api.arena.wave.IArenaWaveManager;
import su.nightexpress.ama.editor.ArenaEditorHandler;
import su.nightexpress.ama.editor.ArenaEditorType;

import java.util.ArrayList;
import java.util.List;

public class EditorWaveList extends AbstractMenuAuto<AMA, IArenaWave> {
	
	private final IArenaWaveManager waveManager;

	private final String       objectName;
	private final List<String> objectLore;
	
	public EditorWaveList(@NotNull IArenaWaveManager waveManager) {
		super(waveManager.plugin(), ArenaEditorHandler.YML_ARENA_WAVE_LIST, "");
		this.waveManager = waveManager;

		this.objectName = StringUT.color(cfg.getString("Object.Name", IArenaWave.PLACEHOLDER_ID));
		this.objectLore = StringUT.color(cfg.getStringList("Object.Lore"));
		
		IMenuClick click = (player, type, e) -> {
			if (type == null) return;

			if (type instanceof MenuItemType type2) {
				if (type2 == MenuItemType.RETURN) {
					waveManager.getEditor().open(player, 1);
				}
				else super.onItemClickDefault(player, type2);
			}
			else if (type instanceof ArenaEditorType type2) {
				if (type2 == ArenaEditorType.WAVES_CREATE_WAVE) {
					plugin.getEditorHandlerNew().startEdit(player, waveManager, type2);
					EditorUtils.tipCustom(player, plugin.lang().Editor_Arena_Waves_Enter_Wave_Create.getMsg());
					player.closeInventory();
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
	@NotNull
	protected List<IArenaWave> getObjects(@NotNull Player player) {
		return new ArrayList<>(this.waveManager.getWaves().values());
	}

	@Override
	@NotNull
	protected ItemStack getObjectStack(@NotNull Player player, @NotNull IArenaWave wave) {
		ItemStack item = new ItemStack(Material.BLAZE_POWDER);
		ItemMeta meta = item.getItemMeta();
		if (meta == null) return item;

		meta.setDisplayName(this.objectName);
		meta.setLore(this.objectLore);
		item.setItemMeta(meta);
		ItemUT.replace(item, wave.replacePlaceholders());
		return item;
	}

	@Override
	public void onReady(@NotNull Player player, @NotNull Inventory inventory) {

	}

	@Override
	@NotNull
	protected IMenuClick getObjectClick(@NotNull Player player, @NotNull IArenaWave wave) {
		return (p2, type, e) -> {
			if (e.isShiftClick() && e.isRightClick()) {
				wave.clear();
				this.waveManager.getWaves().remove(wave.getId());
				this.waveManager.save();
				this.open(p2, 1);
				return;
			}
			wave.getEditor().open(p2, 1);
		};
	}

	@Override
	public boolean cancelClick(@NotNull SlotType slotType, int slot) {
		return true;
	}
}
