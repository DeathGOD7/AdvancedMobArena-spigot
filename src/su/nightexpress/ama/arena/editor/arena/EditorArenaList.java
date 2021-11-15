package su.nightexpress.ama.arena.editor.arena;

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
import su.nightexpress.ama.api.arena.IArena;
import su.nightexpress.ama.api.arena.config.IArenaConfig;
import su.nightexpress.ama.editor.ArenaEditorHandler;
import su.nightexpress.ama.editor.ArenaEditorType;

import java.util.ArrayList;
import java.util.List;

public class EditorArenaList extends AbstractMenuAuto<AMA, IArena> {

	private final String objectName;
	private final List<String> objectLore;
	
	public EditorArenaList(@NotNull AMA plugin) {
		super(plugin, ArenaEditorHandler.YML_ARENA_LIST, "");

		this.objectName = StringUT.color(cfg.getString("Object.Name", IArenaConfig.PLACEHOLDER_ID));
		this.objectLore = StringUT.color(cfg.getStringList("Object.Lore"));
		
		IMenuClick click = (player, type, e) -> {
			if (type == null) return;

			if (type instanceof MenuItemType type2) {
				if (type2 == MenuItemType.RETURN) {
					plugin.getEditor().open(player, 1);
				}
				else super.onItemClickDefault(player, type2);
			}
			else if (type instanceof ArenaEditorType type2) {
				if (type2 == ArenaEditorType.ARENA_CREATE) {
					plugin.getEditorHandlerNew().startEdit(player, plugin, type2);
					EditorUtils.tipCustom(player, plugin.lang().Editor_Arena_Tip_Create.getMsg());
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
	protected List<IArena> getObjects(@NotNull Player player) {
		return new ArrayList<>(this.plugin.getArenaManager().getArenas());
	}

	@Override
	@NotNull
	protected ItemStack getObjectStack(@NotNull Player player, @NotNull IArena arena) {
		ItemStack item = new ItemStack(Material.SPAWNER);
		ItemMeta meta = item.getItemMeta();
		if (meta == null) return item;

		meta.setDisplayName(this.objectName);
		meta.setLore(this.objectLore);
		item.setItemMeta(meta);
		ItemUT.replace(item, arena.getConfig().replacePlaceholders());

		return item;
	}

	@Override
	@NotNull
	protected IMenuClick getObjectClick(@NotNull Player player, @NotNull IArena arena) {
		return (player2, type, e) -> {
			if (e.isShiftClick() && e.isRightClick()) {
				this.plugin.getArenaManager().delete(arena);
				this.open(player2, this.getPage(player2));
				return;
			}
			arena.getConfig().getEditor().open(player2, 1);
		};
	}

	@Override
	public boolean cancelClick(@NotNull SlotType slotType, int slot) {
		return true;
	}
}
