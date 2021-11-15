package su.nightexpress.ama.kits.menu;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.menu.AbstractMenuAuto;
import su.nexmedia.engine.api.menu.IMenuClick;
import su.nexmedia.engine.api.menu.IMenuItem;
import su.nexmedia.engine.api.menu.MenuItemType;
import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.utils.ItemUT;
import su.nexmedia.engine.utils.StringUT;
import su.nightexpress.ama.AMA;
import su.nightexpress.ama.api.kits.IArenaKit;
import su.nightexpress.ama.arena.ArenaPlayer;
import su.nightexpress.ama.data.ArenaUser;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public abstract class AbstractKitListMenu extends AbstractMenuAuto<AMA, IArenaKit> {

	private final String       objectName;
	private final List<String> objectLore;
	
	public AbstractKitListMenu(@NotNull AMA plugin, @NotNull JYML cfg, @NotNull String path) {
		super(plugin, cfg, path);

		this.objectName = StringUT.color(cfg.getString("Object.Name", IArenaKit.PLACEHOLDER_NAME));
		this.objectLore = StringUT.color(cfg.getStringList(path + "Object.Lore"));

		IMenuClick click = (player, type, e) -> {
			if (type instanceof MenuItemType type2) {
				this.onItemClickDefault(player, type2);
			}
		};
		
		for (String sId : cfg.getSection(path + "Content")) {
			IMenuItem guiItem = cfg.getMenuItem(path + "Content." + sId, MenuItemType.class);
			
			if (guiItem.getType() != null) {
				guiItem.setClick(click);
			}
			this.addItem(guiItem);
		}
	}

	@Override
	public void onReady(@NotNull Player player, @NotNull Inventory inventory) {

	}

	@NotNull
	public abstract Predicate<IArenaKit> getFilter(@Nullable ArenaUser user);

	@Override
	@NotNull
	protected List<IArenaKit> getObjects(@NotNull Player player) {
		ArenaUser user = plugin.getUserManager().getOrLoadUser(player);
		if (user == null) return Collections.emptyList();

		ArenaPlayer arenaPlayer = plugin.getArenaManager().getArenaPlayer(player);
		if (arenaPlayer == null) return Collections.emptyList();

		Predicate<IArenaKit> isValidMenu = this.getFilter(user);
		Predicate<IArenaKit> isAllowed = kit -> arenaPlayer.getArena().getConfig().getGameplayManager().isKitAllowed(kit.getId());

		return plugin.getKitManager().getKits().stream().filter(kit -> isValidMenu.and(isAllowed).test(kit)).toList();
	}

	@Override
	@NotNull
	protected ItemStack getObjectStack(@NotNull Player player, @NotNull IArenaKit kit) {
		ItemStack item = new ItemStack(kit.getIcon());
		ItemMeta meta = item.getItemMeta();
		if (meta == null) return item;

		ArenaPlayer arenaPlayer = plugin.getArenaManager().getArenaPlayer(player);
		if (arenaPlayer == null) return item;

		meta.setDisplayName(this.objectName);
		meta.setLore(this.objectLore);
		item.setItemMeta(meta);
		ItemUT.replace(item, str -> kit.replacePlaceholders().apply(str
				.replace(IArenaKit.PLACEHOLDER_IS_AVAILABLE, plugin.lang().getBool(kit.isAvailable(arenaPlayer, false)))
		));

		return item;
	}

	@Override
	public boolean cancelClick(@NotNull SlotType slotType, int slot) {
		return true;
	}
}
