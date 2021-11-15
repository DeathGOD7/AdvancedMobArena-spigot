package su.nightexpress.ama.arena.editor.spots;

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
import su.nexmedia.engine.utils.StringUT;
import su.nightexpress.ama.AMA;
import su.nightexpress.ama.api.arena.spot.IArenaSpot;
import su.nightexpress.ama.api.arena.spot.IArenaSpotManager;
import su.nightexpress.ama.editor.ArenaEditorHandler;
import su.nightexpress.ama.editor.ArenaEditorType;

import java.util.ArrayList;
import java.util.List;

public class EditorSpotList extends AbstractMenuAuto<AMA, IArenaSpot> {
	
	private final IArenaSpotManager spotManager;

	private final String       objectName;
	private final List<String> objectLore;
	
	public EditorSpotList(@NotNull IArenaSpotManager spotManager) {
		super(spotManager.plugin(), ArenaEditorHandler.ARENA_SPOT_LIST, "");
		
		this.spotManager = spotManager;
		this.objectName = StringUT.color(cfg.getString("Object.Name", "%name%"));
		this.objectLore = StringUT.color(cfg.getStringList("Object.Lore"));
		
		IMenuClick click = (player, type, e) -> {
			if (type == null) return;

			if (type instanceof MenuItemType type2) {
				if (type2 == MenuItemType.RETURN) {
					spotManager.getArenaConfig().getEditor().open(player, 1);
				}
				else this.onItemClickDefault(player, type2);
			}
			else if (type instanceof ArenaEditorType type2) {
				if (type2 == ArenaEditorType.SPOT_CREATE) {
					plugin.getEditorHandlerNew().startEdit(player, spotManager, type2);
					EditorUtils.tipCustom(player, plugin.lang().Editor_Spot_Enter_Id.getMsg());
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
			IMenuItem guiItem = cfg.getMenuItem("Editor." + sId, ArenaEditorType.class);
			
			if (guiItem.getType() != null) {
				guiItem.setClick(click);
			}
			this.addItem(guiItem);
		}
	}

	@Override
	public void onReady(@NotNull Player player, @NotNull Inventory inventory) {

	}

	@Override
	@NotNull
	protected List<IArenaSpot> getObjects(@NotNull Player player) {
		return new ArrayList<>(this.spotManager.getSpots());
	}

	@Override
	@NotNull
	protected ItemStack getObjectStack(@NotNull Player player, @NotNull IArenaSpot spot) {
		ItemStack item = new ItemStack(Material.CYAN_TERRACOTTA);
		ItemMeta meta = item.getItemMeta();
		if (meta == null) return item;

		List<String> lore = new ArrayList<>(this.objectLore);
		lore.replaceAll(spot.replacePlaceholders());

		meta.setDisplayName(spot.replacePlaceholders().apply(this.objectName));
		meta.setLore(lore);

		item.setItemMeta(meta);
		return item;
	}

	@Override
	@NotNull
	protected IMenuClick getObjectClick(@NotNull Player player, @NotNull IArenaSpot spot) {
		return (p, type, e) -> {
			if (e.isShiftClick() && e.isRightClick()) {
				this.spotManager.removeSpot(spot);
				this.open(p, this.getPage(p));
				return;
			}
			spot.getEditor().open(p, 1);
		};
	}

	@Override
	public boolean cancelClick(@NotNull SlotType slotType, int slot) {
		return true;
	}
}
