package su.nightexpress.ama.kits.editor;

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
import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.utils.ItemUT;
import su.nexmedia.engine.utils.StringUT;
import su.nightexpress.ama.AMA;
import su.nightexpress.ama.api.kits.IArenaKit;
import su.nightexpress.ama.editor.ArenaEditorType;
import su.nightexpress.ama.kits.KitManager;

import java.util.ArrayList;
import java.util.List;

public class EditorKitList extends AbstractMenuAuto<AMA, IArenaKit> {

	private final KitManager kitManager;

	private final String objectName;
	private final List<String> objectLore;
	
	public EditorKitList(@NotNull KitManager kitManager, @NotNull JYML cfg) {
		super(kitManager.plugin, cfg, "");
		
		this.kitManager = kitManager;
		this.objectName = StringUT.color(cfg.getString("Object.Name", IArenaKit.PLACEHOLDER_ID));
		this.objectLore = StringUT.color(cfg.getStringList("Object.Lore"));

		IMenuClick click = (player, type, e) -> {
			if (type instanceof MenuItemType type2) {
				if (type2 == MenuItemType.RETURN) {
					plugin.getEditor().open(player, 1);
				}
				else this.onItemClickDefault(player, type2);
			}
			else if (type instanceof ArenaEditorType type2) {
				if (type2 == ArenaEditorType.KIT_CREATE) {
					plugin.getEditorHandlerNew().startEdit(player, kitManager, type2);
					EditorUtils.tipCustom(player, plugin.lang().Editor_Kit_Enter_Create.getMsg());
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
	public void onReady(@NotNull Player player, @NotNull Inventory inventory) {

	}

	@Override
	@NotNull
	protected IMenuClick getObjectClick(@NotNull Player player, @NotNull IArenaKit kit) {
		return (player1, type, e) -> {
			if (e.isShiftClick() && e.isRightClick()) {
				if (!kit.getFile().delete()) return;
				kit.clear();
				this.kitManager.getKitsMap().remove(kit.getId());
				this.open(player1, this.getPage(player1));
				return;
			}
			kit.getEditor().open(player1, 1);
		};
	}

	@Override
	@NotNull
	protected ItemStack getObjectStack(@NotNull Player player, @NotNull IArenaKit kit) {
		ItemStack item = new ItemStack(kit.getIcon());
		ItemMeta meta = item.getItemMeta();
		if (meta == null) return item;

		meta.setDisplayName(this.objectName);
		meta.setLore(this.objectLore);
		item.setItemMeta(meta);
		ItemUT.replace(item, kit.replacePlaceholders());

		return item;
	}

	@Override
	@NotNull
	protected List<IArenaKit> getObjects(@NotNull Player player) {
		return new ArrayList<>(this.kitManager.getKits());
	}

	@Override
	public boolean cancelClick(@NotNull SlotType slotType, int slot) {
		return true;
	}
}
