package su.nightexpress.ama.arena;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.menu.AbstractMenu;
import su.nexmedia.engine.api.menu.IMenuClick;
import su.nexmedia.engine.api.menu.IMenuItem;
import su.nexmedia.engine.api.menu.MenuItemType;
import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.utils.ItemUT;
import su.nightexpress.ama.AMA;
import su.nightexpress.ama.api.arena.IArena;

public class ArenaListMenu extends AbstractMenu<AMA> {
	
	public ArenaListMenu(@NotNull AMA plugin) {
		super(plugin, JYML.loadOrExtract(plugin, ArenaManager.DIR_ARENAS + "gui.arenas.yml"), "");

		IMenuClick click = (player, type, e) -> {
			if (type instanceof MenuItemType type2) {
				this.onItemClickDefault(player, type2);
			}
		};
		
		for (String sId : cfg.getSection("Content")) {
			IMenuItem menuItem = cfg.getMenuItem("Content." + sId, MenuItemType.class);
			
			if (menuItem.getType() != null) {
				menuItem.setClick(click);
			}
			this.addItem(menuItem);
		}
		
		for (String sId : cfg.getSection("Arenas")) {
			IMenuItem menuItem = cfg.getMenuItem("Arenas." + sId);
			
			IArena arena = plugin.getArenaManager().getArenaById(menuItem.getId());
			if (arena == null) {
				plugin.error("Invalid arena '" + sId + "' in Arenas GUI in 'config.yml' !");
				continue;
			}
			
			menuItem.setClick((p, type, e) -> {
				plugin.getArenaManager().joinLobby(p, arena);
			});
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

		IArena arena = plugin.getArenaManager().getArenaById(menuItem.getId());
		if (arena == null) return;

		ItemUT.replace(item, arena.replacePlaceholders());
	}

	@Override
	public boolean cancelClick(@NotNull SlotType slotType, int slot) {
		return true;
	}
}
