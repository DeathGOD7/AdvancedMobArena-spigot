package su.nightexpress.ama.arena.editor.shop;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.menu.AbstractMenu;
import su.nexmedia.engine.api.menu.IMenuClick;
import su.nexmedia.engine.api.menu.IMenuItem;
import su.nexmedia.engine.api.menu.MenuItemType;
import su.nexmedia.engine.utils.ItemUT;
import su.nightexpress.ama.AMA;
import su.nightexpress.ama.api.arena.shop.IArenaShopManager;
import su.nightexpress.ama.editor.ArenaEditorHandler;
import su.nightexpress.ama.editor.ArenaEditorType;

public class EditorShopManager extends AbstractMenu<AMA> {

	private final IArenaShopManager shopManager;

	private EditorShopProductList itemList;
	
	public EditorShopManager(@NotNull IArenaShopManager shopManager) {
		super(shopManager.plugin(), ArenaEditorHandler.YML_ARENA_SHOP_MANAGER, "");
		this.shopManager = shopManager;
		
		IMenuClick click = (p, type, e) -> {
			if (type == null) return;

			if (type instanceof MenuItemType type2) {
				if (type2 == MenuItemType.RETURN) {
					shopManager.getArenaConfig().getEditor().open(p, 1);
				}
				return;
			}
			
			if (type instanceof ArenaEditorType type2) {
				switch (type2) {
					case SHOP_PRODUCTS -> {
						this.getItemList().open(p, 1);
						return;
					}
					case SHOP_CHANGE_LOCK_IN_WAVE -> shopManager.setLockedWhileMobsAlive(!shopManager.isLockedWhileMobsAlive());
					case SHOP_CHANGE_HIDE_OTHER_KIT_ITEMS -> shopManager.setHideOtherKitProducts(!shopManager.isHideOtherKitProducts());
					default -> {
						return;
					}
				}
				shopManager.save();
				this.open(p, 1);
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
		if (this.itemList != null) {
			this.itemList.clear();
			this.itemList = null;
		}
		super.clear();
	}

	@NotNull
	public EditorShopProductList getItemList() {
		if (this.itemList == null) {
			this.itemList = new EditorShopProductList(this.shopManager);
		}
		return this.itemList;
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

		ItemUT.replace(item, line -> line
				.replace("%lock_while_mobs_alive%", plugin.lang().getBool(shopManager.isLockedWhileMobsAlive()))
				.replace("%hide_other_kit_products%", plugin.lang().getBool(shopManager.isHideOtherKitProducts()))
		);
	}

	@Override
	public boolean cancelClick(@NotNull SlotType slotType, int slot) {
		return true;
	}
}
