package su.nightexpress.ama.kits.editor;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.editor.EditorUtils;
import su.nexmedia.engine.api.menu.*;
import su.nexmedia.engine.utils.ItemUT;
import su.nightexpress.ama.AMA;
import su.nightexpress.ama.api.kits.IArenaKit;
import su.nightexpress.ama.editor.ArenaEditorHandler;
import su.nightexpress.ama.editor.ArenaEditorType;

import java.util.stream.Stream;

public class EditorKitMain extends AbstractMenu<AMA> {

	private final IArenaKit kit;
	
	public EditorKitMain(@NotNull IArenaKit kit) {
		super(kit.plugin(), ArenaEditorHandler.KIT_MAIN, "");
		this.kit = kit;

		IMenuClick click = (player, type, e) -> {
			if (type instanceof MenuItemType type2) {
				if (type2 == MenuItemType.RETURN) {
					plugin.getEditorHub().getKitEditor().open(player, 1);
				}
			}
			else if (type instanceof ArenaEditorType type2) {
				switch (type2) {
					case KIT_CHANGE_NAME -> {
						plugin.getEditorHandlerNew().startEdit(player, kit, type2);
						EditorUtils.tipCustom(player, plugin.lang().Editor_Kit_Enter_Name.getMsg());
						player.closeInventory();
						return;
					}
					case KIT_CHANGE_ICON -> {
						if (e.getClick() == ClickType.MIDDLE) {
							ItemUT.addItem(player, kit.getIcon());
							return;
						}
						ItemStack cursor = e.getCursor();
						if (cursor == null || ItemUT.isAir(cursor)) return;

						kit.setIcon(cursor);
						e.getView().setCursor(null);
					}
					case KIT_CHANGE_COMMANDS -> {
						if (e.isLeftClick()) {
							plugin.getEditorHandlerNew().startEdit(player, kit, type2);
							EditorUtils.tipCustom(player, plugin.lang().Editor_Kit_Enter_Command.getMsg());
							EditorUtils.sendCommandTips(player);
							player.closeInventory();
							return;
						}
						else if (e.isRightClick()) {
							kit.getCommands().clear();
						}
					}
					case KIT_CHANGE_POTIONS -> {
						if (e.isLeftClick()) {
							plugin.getEditorHandlerNew().startEdit(player, kit, type2);
							EditorUtils.tipCustom(player, plugin.lang().Editor_Kit_Enter_Effect.getMsg());
							EditorUtils.sendSuggestTips(player, Stream.of(PotionEffectType.values()).map(PotionEffectType::getName).toList());
							player.closeInventory();
							return;
						}
						else if (e.isRightClick()) {
							kit.getPotionEffects().clear();
						}
					}
					case KIT_CHANGE_ARMOR -> {
						new ContentEditor(kit, 9).open(player, 1);
						return;
					}
					case KIT_CHANGE_INVENTORY -> {
						new ContentEditor(kit, 27).open(player, 1);
						return;
					}
					case KIT_CHANGE_PERMISSION -> kit.setPermissionRequired(!kit.isPermissionRequired());
					case KIT_CHANGE_DEFAULT -> kit.setDefault(!kit.isDefault());
					case KIT_CHANGE_COST -> {
						plugin.getEditorHandlerNew().startEdit(player, kit, type2);
						EditorUtils.tipCustom(player, plugin.lang().Editor_Kit_Enter_Cost.getMsg());
						player.closeInventory();
						return;
					}
					default -> {return;}
				}
				this.kit.save();
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
	@Nullable
	public MenuItemDisplay onItemDisplayPrepare(@NotNull Player player, @NotNull IMenuItem menuItem) {
		if (menuItem.getType() instanceof ArenaEditorType type2) {
			if (type2 == ArenaEditorType.KIT_CHANGE_DEFAULT) {
				return menuItem.getDisplay(String.valueOf(kit.isDefault() ? 1 : 0));
			}
		}
		return super.onItemDisplayPrepare(player, menuItem);
	}

	@Override
	public void onItemPrepare(@NotNull Player player, @NotNull IMenuItem menuItem, @NotNull ItemStack item) {
		super.onItemPrepare(player, menuItem, item);
		if (menuItem.getType() instanceof ArenaEditorType type2) {
			if (type2 == ArenaEditorType.KIT_CHANGE_ICON) {
				item.setType(kit.getIcon().getType());
			}
		}
		ItemUT.replace(item, this.kit.replacePlaceholders());
	}

	@Override
	public boolean cancelClick(@NotNull SlotType slotType, int slot) {
		return slotType != SlotType.PLAYER && slotType != SlotType.EMPTY_PLAYER;
	}

	static class ContentEditor extends AbstractMenu<AMA> {

		private final IArenaKit kit;
		private final boolean isArmor;

		public ContentEditor(@NotNull IArenaKit kit, int size) {
			super(kit.plugin(), "Kit Content", size);
			this.kit = kit;
			this.isArmor = size == 9;
		}

		@Override
		public void onPrepare(@NotNull Player player, @NotNull Inventory inventory) {
			inventory.setContents(this.isArmor ? this.kit.getArmor() : this.kit.getItems());
		}

		@Override
		public void onReady(@NotNull Player player, @NotNull Inventory inventory) {

		}

		@Override
		public void onClose(@NotNull Player player, @NotNull InventoryCloseEvent e) {
			Inventory inventory = e.getInventory();
			ItemStack[] items = new ItemStack[this.isArmor ? 4 : 27];

			for (int slot = 0; slot < items.length; slot++) {
				ItemStack item = inventory.getItem(slot);
				if (item == null) continue;

				items[slot] = item;
			}

			if (this.isArmor) this.kit.setArmor(items);
			else this.kit.setItems(items);

			this.kit.save();
			super.onClose(player, e);

			plugin.runTask(c -> this.kit.getEditor().open(player, 1), false);
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
