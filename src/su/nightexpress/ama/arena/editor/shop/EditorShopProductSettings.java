package su.nightexpress.ama.arena.editor.shop;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
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
import su.nightexpress.ama.api.arena.shop.IArenaShopProduct;
import su.nightexpress.ama.api.arena.type.ArenaLockState;
import su.nightexpress.ama.editor.ArenaEditorHandler;
import su.nightexpress.ama.editor.ArenaEditorType;

import java.util.ArrayList;
import java.util.List;

public class EditorShopProductSettings extends AbstractMenu<AMA> {

	private final IArenaShopProduct shopItem;
	
	public EditorShopProductSettings(@NotNull IArenaShopProduct shopProduct) {
		super(shopProduct.getShopManager().plugin(), ArenaEditorHandler.YML_ARENA_SHOP_PRODUCT_SETTINGS, "");
		this.shopItem = shopProduct;
		
		IMenuClick click = (player, type, e) -> {
			if (type == null) return;

			if (type instanceof MenuItemType type2) {
				if (type2 == MenuItemType.RETURN) {
					if (!(shopProduct.getShopManager().getEditor() instanceof EditorShopManager shopEditor)) return;
					shopEditor.getItemList().open(player, 1);
				}
			}
			else if (type instanceof ArenaEditorType type2) {
				switch (type2) {
					case SHOP_PRODUCT_CHANGE_PRICE -> {
						plugin.getEditorHandlerNew().startEdit(player, shopProduct, type2);
						//EditorManager.startEdit(player, shopProduct, type2);
						EditorUtils.tipCustom(player, plugin.lang().Editor_Arena_Shop_Enter_Product_Price.getMsg());
						player.closeInventory();
						return;
					}
					case SHOP_PRODUCT_CHANGE_PREVIEW -> {
						ItemStack cursor = e.getCursor();
						if (cursor == null || ItemUT.isAir(cursor)) return;

						shopProduct.setPreview(cursor);
						e.getView().setCursor(null);
					}
					case SHOP_PRODUCT_CHANGE_COMMANDS -> {
						if (e.isRightClick()) {
							shopProduct.getCommands().clear();
							break;
						}
						plugin.getEditorHandlerNew().startEdit(player, shopProduct, type2);
						EditorUtils.tipCustom(player, plugin.lang().Editor_Arena_Shop_Enter_Product_Command.getMsg());
						EditorUtils.sendCommandTips(player);
						player.closeInventory();
						return;
					}
					case SHOP_PRODUCT_CHANGE_REQUIRED_KITS -> {
						if (e.isRightClick()) {
							shopProduct.getApplicableKits().clear();
							break;
						}
						plugin.getEditorHandlerNew().startEdit(player, shopProduct, type2);
						EditorUtils.tipCustom(player, plugin.lang().Editor_Arena_Shop_Enter_Product_RequiredKit.getMsg());
						EditorUtils.sendClickableTips(player, plugin.getKitManager().getKitIds());
						player.closeInventory();
						return;
					}
					case SHOP_PRODUCT_CHANGE_ITEMS -> {
						new ProductItems(shopProduct).open(player, 1);
						return;
					}
					case SHOP_PRODUCT_CHANGE_TRIGGERS_LOCKED, SHOP_PRODUCT_CHANGE_TRIGGERS_UNLOCKED -> {
						if (e.isRightClick()) {
							ArenaLockState state = type2 == ArenaEditorType.SHOP_PRODUCT_CHANGE_TRIGGERS_LOCKED ? ArenaLockState.LOCKED : ArenaLockState.UNLOCKED;
							shopProduct.getStateTriggers(state).clear();
							break;
						}
						plugin.getEditorHandlerNew().startEdit(player, shopProduct, type2);
						EditorUtils.tipCustom(player, plugin.lang().Editor_Enter_Triggers.getMsg());
						plugin.lang().Editor_Tip_Triggers.send(player);
						EditorUtils.sendSuggestTips(player, CollectionsUT.getEnumsList(ArenaGameEventType.class));
						player.closeInventory();
						return;
					}
					default -> {
						return;
					}
				}
				shopProduct.getShopManager().save();
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
		ItemUT.replace(item, this.shopItem.replacePlaceholders());
	}

	@Override
	public boolean cancelClick(@NotNull SlotType slotType, int slot) {
		return slotType != SlotType.PLAYER && slotType != SlotType.EMPTY_PLAYER;
	}

	static class ProductItems extends AbstractMenu<AMA> {

		private final IArenaShopProduct shopProduct;

		public ProductItems(@NotNull IArenaShopProduct shopProduct) {
			super(shopProduct.getShopManager().plugin(), "Product Items", 27);
			this.shopProduct = shopProduct;
		}

		@Override
		public void onPrepare(@NotNull Player player, @NotNull Inventory inventory) {
			inventory.setContents(this.shopProduct.getItems().toArray(new ItemStack[this.getSize()]));
		}

		@Override
		public void onReady(@NotNull Player player, @NotNull Inventory inventory) {

		}

		@Override
		public void onClose(@NotNull Player player, @NotNull InventoryCloseEvent e) {
			Inventory inventory = e.getInventory();
			List<ItemStack> items = new ArrayList<>();

			for (int slot = 0; slot < this.getSize(); slot++) {
				ItemStack item = inventory.getItem(slot);
				if (item == null) continue;

				items.add(item);
			}
			this.shopProduct.setItems(items);
			this.shopProduct.getShopManager().save();
			super.onClose(player, e);

			plugin.runTask(c -> this.shopProduct.getEditor().open(player, 1), false);
		}

		@Override
		public boolean destroyWhenNoViewers() {
			return true;
		}

		@Override
		public boolean cancelClick(@NotNull SlotType slotType, int slot) {
			return false;
		}
	}
}
