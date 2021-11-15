package su.nightexpress.ama.arena.editor.shop;

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
import su.nightexpress.ama.api.arena.shop.IArenaShopManager;
import su.nightexpress.ama.api.arena.shop.IArenaShopProduct;
import su.nightexpress.ama.editor.ArenaEditorHandler;
import su.nightexpress.ama.editor.ArenaEditorType;

import java.util.ArrayList;
import java.util.List;

public class EditorShopProductList extends AbstractMenuAuto<AMA, IArenaShopProduct> {

	private final IArenaShopManager shopManager;

	private final String       objectName;
	private final List<String> objectLore;
	
	public EditorShopProductList(@NotNull IArenaShopManager shopManager) {
		super(shopManager.plugin(), ArenaEditorHandler.YML_ARENA_SHOP_PRODUCT_LIST, "");
		this.shopManager = shopManager;

		this.objectName = StringUT.color(cfg.getString("Object.Name", IArenaShopProduct.PLACEHOLDER_NAME));
		this.objectLore = StringUT.color(cfg.getStringList("Object.Lore"));
		
		IMenuClick click = (player, type, e) -> {
			if (type == null) return;

			if (type instanceof MenuItemType type2) {
				if (type2 == MenuItemType.RETURN) {
					shopManager.getEditor().open(player, 1);
				}
				else this.onItemClickDefault(player, type2);
			}
			else if (type instanceof ArenaEditorType type2) {
				if (type2 == ArenaEditorType.SHOP_PRODUCT_CREATE) {
					plugin.getEditorHandlerNew().startEdit(player, shopManager, type2);
					EditorUtils.tipCustom(player, plugin.lang().Editor_Arena_Shop_Enter_Product_Create.getMsg());
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
	protected List<IArenaShopProduct> getObjects(@NotNull Player player) {
		return new ArrayList<>(this.shopManager.getProducts());
	}

	@Override
	@NotNull
	protected ItemStack getObjectStack(@NotNull Player player, @NotNull IArenaShopProduct shopProduct) {
		ItemStack item = new ItemStack(shopProduct.getPreview());
		ItemMeta meta = item.getItemMeta();
		if (meta == null) return item;

		meta.setDisplayName(this.objectName);
		meta.setLore(this.objectLore);
		item.setItemMeta(meta);
		ItemUT.replace(item, shopProduct.replacePlaceholders());

		return item;
	}

	@Override
	@NotNull
	protected IMenuClick getObjectClick(@NotNull Player player, @NotNull IArenaShopProduct shopProduct) {
		return (p2, type, e) -> {
			if (e.isShiftClick() && e.isRightClick()) {
				shopProduct.clear();
				this.shopManager.getProducts().remove(shopProduct);
				this.shopManager.save();
				this.open(p2, 1);
				return;
			}
			shopProduct.getEditor().open(p2, 1);
		};
	}

	@Override
	public boolean cancelClick(@NotNull SlotType slotType, int slot) {
		return false;
	}

	@Override
	public void onReady(@NotNull Player player, @NotNull Inventory inventory) {

	}
}
