package su.nightexpress.ama.arena.editor.arena;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.editor.EditorUtils;
import su.nexmedia.engine.api.menu.*;
import su.nexmedia.engine.utils.ItemUT;
import su.nightexpress.ama.AMA;
import su.nightexpress.ama.api.arena.config.IArenaConfig;
import su.nightexpress.ama.editor.ArenaEditorHandler;
import su.nightexpress.ama.editor.ArenaEditorType;

import java.util.function.UnaryOperator;

public class EditorArenaMain extends AbstractMenu<AMA> {

	private final IArenaConfig config;
	
	public EditorArenaMain(@NotNull AMA plugin, @NotNull IArenaConfig config) {
		super(plugin, ArenaEditorHandler.YML_ARENA_MAIN, "");
		this.config = config;
		
		IMenuClick click = (player, type, e) -> {
			if (type == null) return;

			if (type instanceof MenuItemType type2) {
				if (type2 == MenuItemType.RETURN) {
					plugin.getEditorHub().getArenaEditor().open(player, 1);
				}
			}
			else if (type instanceof ArenaEditorType type2) {
				switch (type2) {
					case ARENA_CHANGE_ACTIVE -> {
						config.setActive(!config.isActive());
						config.save();
						this.open(player, this.getPage(player));
					}
					case ARENA_CHANGE_REQUIREMENT_PERMISSION -> {
						config.setPermissionRequired(!config.isPermissionRequired());
						config.save();
						this.open(player, this.getPage(player));
					}
					case ARENA_CHANGE_REQUIREMENT_MONEY -> {
						plugin.getEditorHandlerNew().startEdit(player, config, type2);
						EditorUtils.tipCustom(player, plugin.lang().Editor_Arena_Enter_JoinCost.getMsg());
						player.closeInventory();
					}
					case ARENA_SETUP_KIT -> {
						plugin.getArenaSetupManager().getConfigSetupManager().startSetup(player, config);
						player.closeInventory();
					}
					case ARENA_OPEN_REGION_MANAGER -> config.getRegionManager().getEditor().open(player, 1);
					case ARENA_OPEN_SPOT_MANAGER -> config.getSpotManager().getEditor().open(player, 1);
					case ARENA_OPEN_GAMEPLAY_MANAGER -> config.getGameplayManager().getEditor().open(player, 1);
					case ARENA_OPEN_WAVE_MANAGER -> config.getWaveManager().getEditor().open(player, 1);
					case ARENA_OPEN_SHOP_MANAGER -> config.getShopManager().getEditor().open(player, 1);
					case ARENA_OPEN_REWARD_MANAGER -> config.getRewardManager().getEditor().open(player, 1);
					default -> {}
				}
			}
		};
		
		for (String sId : cfg.getSection("Content")) {
			IMenuItem guiItem = cfg.getMenuItem("Content." + sId, MenuItemType.class);
			
			if (guiItem.getType() != null) {
				guiItem.setClick(click);
			}
			this.addItem(guiItem);
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
	public void onPrepare(@NotNull Player player, @NotNull Inventory inventory) {

	}

	@Override
	public void onReady(@NotNull Player player, @NotNull Inventory inventory) {

	}

	@Override
	public boolean cancelClick(@NotNull SlotType slotType, int slot) {
		return true;
	}

	@Override
	@Nullable
	public MenuItemDisplay onItemDisplayPrepare(@NotNull Player player, @NotNull IMenuItem menuItem) {
		if (menuItem.getType() instanceof ArenaEditorType type2) {
			if (type2 == ArenaEditorType.ARENA_CHANGE_ACTIVE) {
				return menuItem.getDisplay(String.valueOf(config.isActive() ? (config.hasProblems() ? 2 : 1) : 0));
			}
		}
		return super.onItemDisplayPrepare(player, menuItem);
	}

	@Override
	public void onItemPrepare(@NotNull Player player, @NotNull IMenuItem menuItem, @NotNull ItemStack item) {
		super.onItemPrepare(player, menuItem, item);

		Enum<?> type = menuItem.getType();
		if (!(type instanceof ArenaEditorType type2)) return;

		UnaryOperator<String> replacer;
		if (type2 == ArenaEditorType.ARENA_OPEN_GAMEPLAY_MANAGER) {
			replacer = this.config.getGameplayManager().replacePlaceholders();
		}
		else if (type2 == ArenaEditorType.ARENA_OPEN_REGION_MANAGER) {
			replacer = this.config.getRegionManager().replacePlaceholders();
		}
		else if (type2 == ArenaEditorType.ARENA_OPEN_SHOP_MANAGER) {
			replacer = this.config.getShopManager().replacePlaceholders();
		}
		else if (type2 == ArenaEditorType.ARENA_OPEN_SPOT_MANAGER) {
			replacer = this.config.getSpotManager().replacePlaceholders();
		}
		else if (type2 == ArenaEditorType.ARENA_OPEN_WAVE_MANAGER) {
			replacer = this.config.getWaveManager().replacePlaceholders();
		}
		else if (type2 == ArenaEditorType.ARENA_OPEN_REWARD_MANAGER) {
			replacer = this.config.getRewardManager().replacePlaceholders();
		}
		else replacer = this.config.replacePlaceholders();

		ItemUT.replace(item, replacer);
	}
}
