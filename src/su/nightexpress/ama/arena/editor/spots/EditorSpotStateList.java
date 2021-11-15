package su.nightexpress.ama.arena.editor.spots;

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
import su.nexmedia.engine.utils.CollectionsUT;
import su.nexmedia.engine.utils.ItemUT;
import su.nexmedia.engine.utils.StringUT;
import su.nightexpress.ama.AMA;
import su.nightexpress.ama.api.arena.game.ArenaGameEventType;
import su.nightexpress.ama.api.arena.spot.IArenaSpot;
import su.nightexpress.ama.api.arena.spot.IArenaSpotState;
import su.nightexpress.ama.editor.ArenaEditorHandler;
import su.nightexpress.ama.editor.ArenaEditorType;

import java.util.ArrayList;
import java.util.List;

class EditorSpotStateList extends AbstractMenuAuto<AMA, IArenaSpotState> {

	private final IArenaSpot   spot;

	private final String       objectName;
	private final List<String> objectLore;
	
	public EditorSpotStateList(@NotNull IArenaSpot spot) {
		super(spot.plugin(), ArenaEditorHandler.ARENA_SPOT_STATE_LIST, "");
		this.spot = spot;

		this.objectName = StringUT.color(cfg.getString("Object.Name", IArenaSpotState.PLACEHOLDER_ID));
		this.objectLore = StringUT.color(cfg.getStringList("Object.Lore"));
		
		IMenuClick click = (player, type, e) -> {
			if (type == null) return;

			if (type instanceof MenuItemType type2) {
				if (type2 == MenuItemType.RETURN) {
					spot.getEditor().open(player, 1);
				}
				else this.onItemClickDefault(player, type2);
			}
			else if (type instanceof ArenaEditorType type2) {
				if (type2 == ArenaEditorType.SPOT_STATE_CREATE) {
					plugin.getEditorHandlerNew().startEdit(player, spot, type2);
					EditorUtils.tipCustom(player, plugin.lang().Editor_Spot_State_Enter_Id.getMsg());
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
	protected List<IArenaSpotState> getObjects(@NotNull Player player) {
		return new ArrayList<>(this.spot.getStates().values());
	}

	@Override
	@NotNull
	protected ItemStack getObjectStack(@NotNull Player player, @NotNull IArenaSpotState state) {
		ItemStack item = new ItemStack(Material.ITEM_FRAME);
		ItemMeta meta = item.getItemMeta();
		if (meta == null) return item;

		meta.setDisplayName(this.objectName);
		meta.setLore(this.objectLore);
		item.setItemMeta(meta);
		ItemUT.replace(item, state.replacePlaceholders());

		return item;
	}

	@Override
	@NotNull
	protected IMenuClick getObjectClick(@NotNull Player player, @NotNull IArenaSpotState state) {
		return (p, type, e) -> {
			if (e.isShiftClick()) {
				if (e.isRightClick()) {
					this.spot.getStates().remove(state.getId());
					this.spot.save();
					this.open(p, this.getPage(p));
					return;
				}
				p.closeInventory();
				plugin.getArenaSetupManager().getSpotStateSetupManager().startSetup(player, state);
				return;
			}

			if (e.isRightClick()) {
				state.getTriggers().clear();
				this.spot.save();
				this.open(p, this.getPage(p));
				return;
			}
			plugin.getEditorHandlerNew().startEdit(p, state, ArenaEditorType.SPOT_STATE_CHANGE_TRIGGERS);
			EditorUtils.tipCustom(p, plugin.lang().Editor_Enter_Triggers.getMsg());
			plugin.lang().Editor_Tip_Triggers.send(p);
			EditorUtils.sendSuggestTips(p, CollectionsUT.getEnumsList(ArenaGameEventType.class));
			p.closeInventory();
		};
	}

	@Override
	public boolean cancelClick(@NotNull SlotType slotType, int slot) {
		return true;
	}
}
