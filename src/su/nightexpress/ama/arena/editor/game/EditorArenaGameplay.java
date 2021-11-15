package su.nightexpress.ama.arena.editor.game;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.editor.EditorUtils;
import su.nexmedia.engine.api.menu.AbstractMenu;
import su.nexmedia.engine.api.menu.IMenuClick;
import su.nexmedia.engine.api.menu.IMenuItem;
import su.nexmedia.engine.api.menu.MenuItemType;
import su.nexmedia.engine.utils.CollectionsUT;
import su.nightexpress.ama.AMA;
import su.nightexpress.ama.api.arena.game.IArenaGameplayManager;
import su.nightexpress.ama.editor.ArenaEditorHandler;
import su.nightexpress.ama.editor.ArenaEditorType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EditorArenaGameplay extends AbstractMenu<AMA> {

	private final IArenaGameplayManager gameplayManager;

	private EditorGameCommandList commandList;

	public EditorArenaGameplay(@NotNull IArenaGameplayManager gameplayManager) {
		super(gameplayManager.plugin(), ArenaEditorHandler.YML_ARENA_GAMEPLAY, "");
		this.gameplayManager = gameplayManager;
		
		IMenuClick click = (player, type, e) -> {
			if (type == null) return;

			if (type instanceof MenuItemType type2) {
				if (type2 == MenuItemType.RETURN) {
					this.gameplayManager.getArenaConfig().getEditor().open(player, 1);
				}
				return;
			}
			
			if (type instanceof ArenaEditorType type2) {
				switch (type2) {
					case GAMEPLAY_CHANGE_TIMELEFT -> {
						if (e.isRightClick()) {
							gameplayManager.setTimeleft(-1);
							break;
						}
						plugin.getEditorHandlerNew().startEdit(player, gameplayManager, type2);
						EditorUtils.tipCustom(player, plugin.lang().Editor_Arena_Gameplay_Enter_Timeleft.getMsg());
						player.closeInventory();
						return;
					}
					case GAMEPLAY_CHANGE_LOBBY_TIME -> {
						plugin.getEditorHandlerNew().startEdit(player, gameplayManager, type2);
						EditorUtils.tipCustom(player, plugin.lang().Editor_Arena_Gameplay_Enter_LobbyTime.getMsg());
						player.closeInventory();
						return;
					}
					/*case GAMEPLAY_CHANGE_COINS_MULTIPLIER -> {
						if (e.isRightClick()) {
							game.setCoinsMultiplier(1D);
							break;
						}
						EditorManager.startEdit(player, game, type2);
						EditorManager.tipCustom(player, plugin.lang().Editor_Arena_Gameplay_Tip_LobbyTime.getMsg());
						player.closeInventory();
						return;
					}*/
					case GAMEPLAY_CHANGE_ANNOUNCES -> gameplayManager.setAnnouncesEnabled(!gameplayManager.isAnnouncesEnabled());
					case GAMEPLAY_CHANGE_SCOREBOARD -> gameplayManager.setScoreboardEnabled(!gameplayManager.isScoreboardEnabled());
					case GAMEPLAY_CHANGE_SHOP -> gameplayManager.setShopEnabled(!gameplayManager.isShopEnabled());
					case GAMEPLAY_CHANGE_HUNGER -> gameplayManager.setHungerEnabled(!gameplayManager.isHungerEnabled());
					case GAMEPLAY_CHANGE_REGENERATION -> gameplayManager.setRegenerationEnabled(!gameplayManager.isRegenerationEnabled());
					case GAMEPLAY_CHANGE_ITEM_DROP -> gameplayManager.setItemDropEnabled(!gameplayManager.isItemDropEnabled());
					case GAMEPLAY_CHANGE_ITEM_PICKUP -> gameplayManager.setItemPickupEnabled(!gameplayManager.isItemPickupEnabled());
					case GAMEPLAY_CHANGE_ITEM_DURABILITY -> gameplayManager.setItemDurabilityEnabled(!gameplayManager.isItemDurabilityEnabled());
					case GAMEPLAY_CHANGE_SLIME_SPLIT -> gameplayManager.setSlimeSplitEnabled(!gameplayManager.isSlimeSplitEnabled());
					case GAMEPLAY_CHANGE_MOB_DROP_EXP -> gameplayManager.setMobDropExpEnabled(!gameplayManager.isMobDropExpEnabled());
					case GAMEPLAY_CHANGE_MOB_DROP_ITEMS -> gameplayManager.setMobDropLootEnabled(!gameplayManager.isMobDropLootEnabled());
					case GAMEPLAY_CHANGE_PLAYERS -> {
						if (e.getClick() == ClickType.MIDDLE) {
							gameplayManager.setPlayerExpSavingEnabled(!gameplayManager.isPlayerExpSavingEnabled());
							break;
						}

						if (e.isShiftClick()) {
							if (e.isLeftClick()) {
								plugin.getEditorHandlerNew().startEdit(player, gameplayManager, ArenaEditorType.GAMEPLAY_CHANGE_PLAYERS_LIVES);
								EditorUtils.tipCustom(player, plugin.lang().Editor_Arena_Gameplay_Enter_Players_Lives.getMsg());
								player.closeInventory();
							}
							else if (e.isRightClick()) {
								gameplayManager.setPlayerDropItemsOnDeathEnabled(!gameplayManager.isPlayerDropItemsOnDeathEnabled());
								break;
							}
							return;
						}
						else {
							if (e.isLeftClick()) {
								plugin.getEditorHandlerNew().startEdit(player, gameplayManager, ArenaEditorType.GAMEPLAY_CHANGE_PLAYERS_MIN);
							}
							else if (e.isRightClick()) {
								plugin.getEditorHandlerNew().startEdit(player, gameplayManager, ArenaEditorType.GAMEPLAY_CHANGE_PLAYERS_MAX);
							}
							EditorUtils.tipCustom(player, plugin.lang().Editor_Arena_Gameplay_Enter_Players_MinMax.getMsg());
							player.closeInventory();
							return;
						}
					}
					case GAMEPLAY_CHANGE_SPECTATE -> {
						if (e.isLeftClick()) {
							gameplayManager.setSpectateEnabled(!gameplayManager.isSpectateEnabled());
						}
						else if (e.isRightClick()) {
							gameplayManager.setSpectateOnDeathEnabled(!gameplayManager.isSpectateOnDeathEnabled());
						}
					}
					case GAMEPLAY_CHANGE_BANNED_ITEMS -> {
						if (e.isRightClick()) {
							gameplayManager.getBannedItems().clear();
							break;
						}

						plugin.getEditorHandlerNew().startEdit(player, gameplayManager, type2);
						EditorUtils.tipCustom(player, plugin.lang().Editor_Arena_Gameplay_Enter_BannedItems.getMsg());
						player.closeInventory();
						return;
					}
					case GAMEPLAY_CHANGE_ALLOWED_SPAWN_REASONS -> {
						if (e.isRightClick()) {
							gameplayManager.getAllowedSpawnReasons().clear();
							break;
						}

						plugin.getEditorHandlerNew().startEdit(player, gameplayManager, type2);
						EditorUtils.tipCustom(player, plugin.lang().Editor_Arena_Gameplay_Enter_AllowedSpawnReason.getMsg());
						EditorUtils.sendClickableTips(player, CollectionsUT.getEnumsList(CreatureSpawnEvent.SpawnReason.class));
						player.closeInventory();
						return;
					}
					case GAMEPLAY_CHANGE_COMMANDS -> {
						if (e.isShiftClick()) {
							if (e.isLeftClick()) {
								plugin.getEditorHandlerNew().startEdit(player, gameplayManager, ArenaEditorType.GAMEPLAY_CHANGE_COMMANDS_ADD_WHITE);
								EditorUtils.tipCustom(player, plugin.lang().Editor_Arena_Gameplay_Enter_Commands_AddWhite.getMsg());
								player.closeInventory();
								return;
							}
							else {
								gameplayManager.getPlayerCommandsAllowed().clear();
								break;
							}
						}

						if (e.isLeftClick()) {
							gameplayManager.setPlayerCommandsEnabled(!gameplayManager.isPlayerCommandsEnabled());
						}
					}
					case GAMEPLAY_CHANGE_KITS -> {
						if (e.getClick() == ClickType.MIDDLE) {
							gameplayManager.setKitsEnabled(!gameplayManager.isKitsEnabled());
							break;
						}

						if (e.isShiftClick()) {
							if (e.isLeftClick()) {
								plugin.getEditorHandlerNew().startEdit(player, gameplayManager, ArenaEditorType.GAMEPLAY_CHANGE_KITS_ADD_LIMIT);
								EditorUtils.tipCustom(player, plugin.lang().Editor_Arena_Gameplay_Enter_Kits_AddLimit.getMsg());
								EditorUtils.sendClickableTips(player, plugin.getKitManager().getKitIds());
								player.closeInventory();
								return;
							}
							else {
								gameplayManager.getKitsLimits().clear();
							}
						}
						else {
							if (e.isLeftClick()) {
								plugin.getEditorHandlerNew().startEdit(player, gameplayManager, ArenaEditorType.GAMEPLAY_CHANGE_KITS_ADD_ALLOWED);
								EditorUtils.tipCustom(player, plugin.lang().Editor_Arena_Gameplay_Enter_Kits_AddLimit.getMsg());
								EditorUtils.sendClickableTips(player, plugin.getKitManager().getKitIds());
								player.closeInventory();
								return;
							}
							else {
								gameplayManager.getKitsAllowed().clear();
							}
						}
					}
					case GAMEPLAY_CHANGE_COMPAT_PETS -> gameplayManager.setExternalPetsEnabled(!gameplayManager.isExternalPetsEnabled());
					case GAMEPLAY_CHANGE_COMPAT_MCMMO -> gameplayManager.setExternalMcmmoEnabled(!gameplayManager.isExternalMcmmoEnabled());
					case GAMEPLAY_CHANGE_AUTO_COMMANDS -> {
						this.getCommandList().open(player, 1);
						return;
					}
					default -> {
						return;
					}
				}
				this.gameplayManager.save();
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
	public void clear() {
		if (this.commandList != null) {
			this.commandList.clear();
			this.commandList = null;
		}
		super.clear();
	}

	@NotNull
	public EditorGameCommandList getCommandList() {
		if (this.commandList == null) {
			this.commandList = new EditorGameCommandList(this.gameplayManager);
		}
		return commandList;
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

		lore.replaceAll(line -> line
				.replace("%timeleft%", gameplayManager.getTimeleft() > 0 ? String.valueOf(gameplayManager.getTimeleft()) : "-")
				.replace("%lobby-time%", String.valueOf(gameplayManager.getLobbyTime()))
				//.replace("%coins-multiplier%", NumberUT.format(game.getCoinsMultiplier()))
				.replace("%announces-enabled%", plugin.lang().getBool(gameplayManager.isAnnouncesEnabled()))
				.replace("%scoreboard-enabled%", plugin.lang().getBool(gameplayManager.isScoreboardEnabled()))
				.replace("%shop-enabled%", plugin.lang().getBool(gameplayManager.isShopEnabled()))
				.replace("%hunger-enabled%", plugin.lang().getBool(gameplayManager.isHungerEnabled()))
				.replace("%regeneration-enabled%", plugin.lang().getBool(gameplayManager.isRegenerationEnabled()))
				.replace("%item-drop-enabled%", plugin.lang().getBool(gameplayManager.isItemDropEnabled()))
				.replace("%item-pickup-enabled%", plugin.lang().getBool(gameplayManager.isItemPickupEnabled()))
				.replace("%item-durability-enabled%", plugin.lang().getBool(gameplayManager.isItemDurabilityEnabled()))
				.replace("%slime-split-enabled%", plugin.lang().getBool(gameplayManager.isSlimeSplitEnabled()))
				.replace("%mob-drop-exp-enabled%", plugin.lang().getBool(gameplayManager.isMobDropExpEnabled()))
				.replace("%mob-drop-items-enabled%", plugin.lang().getBool(gameplayManager.isMobDropLootEnabled()))
				.replace("%players-min%", String.valueOf(gameplayManager.getPlayerMinAmount()))
				.replace("%players-max%", String.valueOf(gameplayManager.getPlayerMaxAmount()))
				.replace("%players-lives%", String.valueOf(gameplayManager.getPlayerLivesAmount()))
				.replace("%players-save-exp%", plugin.lang().getBool(gameplayManager.isPlayerExpSavingEnabled()))
				.replace("%players_drop_items_on_death%", plugin.lang().getBool(gameplayManager.isPlayerDropItemsOnDeathEnabled()))
				.replace("%spectate-enabled%", plugin.lang().getBool(gameplayManager.isSpectateEnabled()))
				.replace("%spectate-after-death%", plugin.lang().getBool(gameplayManager.isSpectateOnDeathEnabled()))
				.replace("%commands-enabled%", plugin.lang().getBool(gameplayManager.isPlayerCommandsEnabled()))
				.replace("%kits-enabled%", plugin.lang().getBool(gameplayManager.isKitsEnabled()))
				.replace("%compat-pets-enabled%", plugin.lang().getBool(gameplayManager.isExternalPetsEnabled()))
				.replace("%compat-mcmmo-enabled%", plugin.lang().getBool(gameplayManager.isExternalMcmmoEnabled()))
		);

		List<String> lore2 = new ArrayList<>();

		for (String line : lore) {
			if (line.contains("%commands-whitelist%")) {
				for (String cmd : gameplayManager.getPlayerCommandsAllowed()) {
					lore2.add(line.replace("%commands-whitelist%", cmd));
				}
				continue;
			}
			if (line.contains("%banned_items%")) {
				lore2.addAll(gameplayManager.getBannedItems().stream().map(Enum::name).toList());
				continue;
			}
			if (line.contains("%allowed_spawn_reasons%")) {
				lore2.addAll(gameplayManager.getAllowedSpawnReasons().stream().map(Enum::name).toList());
				continue;
			}
			if (line.contains("%kits-allowed%")) {
				for (String kit : gameplayManager.getKitsAllowed()) {
					lore2.add(line.replace("%kits-allowed%", kit));
				}
				continue;
			}
			if (line.contains("%kits-limits-name%")) {
				for (Map.Entry<String, Integer> en : gameplayManager.getKitsLimits().entrySet()) {
					lore2.add(line
							.replace("%kits-limits-name%", en.getKey())
							.replace("%kits-limits-amount%", String.valueOf(en.getValue()))
					);
				}
				continue;
			}
			lore2.add(line);
		}

		meta.setLore(lore2);
		item.setItemMeta(meta);
	}

	@Override
	public boolean cancelClick(@NotNull SlotType slotType, int slot) {
		return true;
	}
}
