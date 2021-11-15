package su.nightexpress.ama.arena.editor.regions;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
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
import su.nexmedia.engine.utils.LocUT;
import su.nexmedia.engine.utils.StringUT;
import su.nightexpress.ama.AMA;
import su.nightexpress.ama.api.arena.region.IArenaRegion;
import su.nightexpress.ama.api.arena.region.IArenaRegionManager;
import su.nightexpress.ama.editor.ArenaEditorHandler;
import su.nightexpress.ama.editor.ArenaEditorType;

import java.util.ArrayList;
import java.util.List;

public class EditorRegionList extends AbstractMenuAuto<AMA, IArenaRegion> {
	
	private final IArenaRegionManager regionManager;

	private final String       objectName;
	private final List<String> objectLore;
	
	public EditorRegionList(@NotNull IArenaRegionManager regionManager) {
		super(regionManager.plugin(), ArenaEditorHandler.YML_ARENA_REGION_LIST, "");
		
		this.regionManager = regionManager;
		this.objectName = StringUT.color(cfg.getString("Object.Name", IArenaRegion.PLACEHOLDER_ID));
		this.objectLore = StringUT.color(cfg.getStringList("Object.Lore"));
		
		IMenuClick click = (player, type, e) -> {
			if (type == null) return;

			if (type instanceof MenuItemType type2) {
				if (type2 == MenuItemType.RETURN) {
					regionManager.getArenaConfig().getEditor().open(player, 1);
				}
				else super.onItemClickDefault(player, type2);
			}
			else if (type instanceof ArenaEditorType type2) {
				if (type2 == ArenaEditorType.REGION_CREATE) {
					plugin.getEditorHandlerNew().startEdit(player, regionManager, type2);
					EditorUtils.tipCustom(player, plugin.lang().Editor_Region_Enter_Id.getMsg());
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
	protected List<IArenaRegion> getObjects(@NotNull Player player) {
		return new ArrayList<>(this.regionManager.getRegions());
	}

	@Override
	@NotNull
	protected ItemStack getObjectStack(@NotNull Player player, @NotNull IArenaRegion region) {
		Material material = Material.GRASS_BLOCK;
		if (region.getSpawnLocation() != null) {
			material = LocUT.getFirstGroundBlock(region.getSpawnLocation()).getBlock().getRelative(BlockFace.UP).getType();
		}
		if (material.isAir()) material = Material.GRASS_BLOCK;

		ItemStack icon = new ItemStack(material);
		ItemMeta meta = icon.getItemMeta();
		if (meta == null) return icon;

		meta.setLore(this.objectLore);
		meta.setDisplayName(this.objectName);
		icon.setItemMeta(meta);
		ItemUT.replace(icon, region.replacePlaceholders());

		return icon;
	}

	@Override
	@NotNull
	protected IMenuClick getObjectClick(@NotNull Player player, @NotNull IArenaRegion region) {
		return (p, type, e) -> {
			if (e.isShiftClick() && e.isRightClick()) {
				if (this.regionManager.removeRegion(region)) {
					this.open(p, 1);
				}
				return;
			}
			region.getEditor().open(p, 1);
		};
	}

	@Override
	public boolean cancelClick(@NotNull SlotType slotType, int slot) {
		return true;
	}
}
