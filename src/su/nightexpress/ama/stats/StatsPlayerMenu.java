package su.nightexpress.ama.stats;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.menu.AbstractMenu;
import su.nexmedia.engine.api.menu.IMenuClick;
import su.nexmedia.engine.api.menu.IMenuItem;
import su.nexmedia.engine.api.menu.MenuItemType;
import su.nexmedia.engine.config.api.JYML;
import su.nightexpress.ama.AMA;
import su.nightexpress.ama.api.arena.IArena;
import su.nightexpress.ama.api.arena.config.IArenaConfig;
import su.nightexpress.ama.data.ArenaUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StatsPlayerMenu extends AbstractMenu<AMA> {
	
	public StatsPlayerMenu(@NotNull AMA plugin, @NotNull JYML cfg) {
		super(plugin, cfg, "");
		
		int slot = 0;
		for (StatType statType : StatType.values()) {
			String path2 = "Stat_Icons." + statType.name() + ".";
			cfg.addMissing(path2 + "Display.default.Item.material", Material.MAP.name());
			cfg.addMissing(path2 + "Display.default.Item.name", plugin.lang().getEnum(statType));
			cfg.addMissing(path2 + "Display.default.Item.lore", Arrays.asList(IArenaConfig.PLACEHOLDER_NAME + ": %score%", "%total%"));
			cfg.addMissing(path2 + "Slots", slot++);
			cfg.addMissing(path2 + "Type", statType.name());
		}
		cfg.saveChanges();
		
		IMenuClick click = (player, type, e) -> {
			if (type instanceof MenuItemType type2) this.onItemClickDefault(player, type2);
		};
		
		for (String sId : cfg.getSection("Content")) {
			IMenuItem menuItem = cfg.getMenuItem("Content." + sId, MenuItemType.class);
			
			if (menuItem.getType() != null) {
				menuItem.setClick(click);
			}
			
			this.addItem(menuItem);
		}
		
		for (String sId : cfg.getSection("Stat_Icons")) {
			IMenuItem menuItem = cfg.getMenuItem("Stat_Icons." + sId, StatType.class);
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

		ItemMeta meta = item.getItemMeta();
		if (meta == null) return;

		List<String> lore = meta.getLore();
		if (lore == null) return;

		Enum<?> type = menuItem.getType();
		if (type == null || !type.getClass().equals(StatType.class)) return;

		StatType statType = (StatType) type;
		ArenaUser user = plugin.getUserManager().getOrLoadUser(player);
		if (user == null) return;

		List<String> lore2 = new ArrayList<>();
		for (String line : lore) {
			if (line.contains(IArenaConfig.PLACEHOLDER_NAME)) {
				for (IArena arena : plugin.getArenaManager().getArenas()) {
					String score = String.valueOf(user.getStats(statType, arena.getId()));
					lore2.add(arena.getConfig().replacePlaceholders().apply(line).replace("%score%", score));
				}
				continue;
			}
			lore2.add(line.replace("%total%", String.valueOf(user.getStats(statType))));
		}
		meta.setLore(lore2);
		item.setItemMeta(meta);
	}

	@Override
	public boolean cancelClick(@NotNull SlotType slotType, int slot) {
		return true;
	}
}
