package su.nightexpress.ama.arena.editor.regions;

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
import su.nightexpress.ama.api.arena.region.IArenaRegion;
import su.nightexpress.ama.api.arena.region.IArenaRegionWave;
import su.nightexpress.ama.editor.ArenaEditorHandler;
import su.nightexpress.ama.editor.ArenaEditorType;

import java.util.ArrayList;
import java.util.List;

public class EditorRegionWaveList extends AbstractMenuAuto<AMA, IArenaRegionWave> {
	
	private final IArenaRegion  region;

	private final String       objectName;
	private final List<String> objectLore;
	
	public EditorRegionWaveList(@NotNull IArenaRegion region) {
		super(region.plugin(), ArenaEditorHandler.YML_ARENA_REGION_WAVE_LIST, "");
		this.region = region;

		this.objectName = StringUT.color(cfg.getString("Object.Name", "%wave%"));
		this.objectLore = StringUT.color(cfg.getStringList("Object.Lore"));
		
		IMenuClick click = (player, type, e) -> {
			if (type == null) return;

			if (type instanceof MenuItemType type2) {
				if (type2 == MenuItemType.RETURN) {
					region.getEditor().open(player, 1);
				}
				else super.onItemClickDefault(player, type2);
			}
			else if (type instanceof ArenaEditorType type2) {
				if (type2 == ArenaEditorType.REGION_WAVE_CREATE) {
					plugin.getEditorHandlerNew().startEdit(player, region, type2);
					EditorUtils.tipCustom(player, plugin.lang().Editor_Arena_Waves_Enter_Wave_Create.getMsg());
					EditorUtils.sendClickableTips(player, region.getArenaConfig().getWaveManager().getWaves().keySet());
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
	public boolean cancelClick(@NotNull SlotType slotType, int slot) {
		return true;
	}

	@Override
	public void onReady(@NotNull Player player, @NotNull Inventory inventory) {

	}

	@Override
	@NotNull
	protected List<IArenaRegionWave> getObjects(@NotNull Player player) {
		return new ArrayList<>(this.region.getWaves());
	}

	@Override
	@NotNull
	protected ItemStack getObjectStack(@NotNull Player player, @NotNull IArenaRegionWave wave) {
		ItemStack item = new ItemStack(Material.BLAZE_POWDER);
		ItemMeta meta = item.getItemMeta();
		if (meta == null) return item;

		meta.setLore(this.objectLore);
		meta.setDisplayName(this.objectName);
		item.setItemMeta(meta);
		ItemUT.replace(item, wave.replacePlaceholders());

		return item;
	}

	@Override
	@NotNull
	protected IMenuClick getObjectClick(@NotNull Player player, @NotNull IArenaRegionWave wave) {
		return (p2, type, e) -> {
			if (e.isShiftClick() && e.isRightClick()) {
				wave.clear();
				this.region.getWaves().remove(wave);
				this.region.save();
				this.open(p2, 1);
				return;
			}
			wave.getEditor().open(p2, 1);
		};
	}
}
